/* -------------------------------------------------------------------------- */

package mm.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mm.common.Config;
import mm.common.data.Hero;
import mm.common.data.LeftMatchmakingCause;
import mm.common.data.LobbyCauseOfDeath;
import mm.common.data.LoginError;
import mm.common.data.MatchSummary;
import mm.common.data.ServerStats;
import mm.common.data.Team;
import mm.common.data.TeamInfo;
import mm.server.Arguments;
import mm.server.auth.Account;
import mm.server.auth.AccountManager;
import mm.server.error.LoginErrorException;
import mm.server.error.SignUpErrorException;
import mm.server.game.Lobby;
import mm.server.game.MatchFound;
import mm.server.game.Matchmaker;

/* -------------------------------------------------------------------------- */

/**
* This class is responsible for answering the clients and making the overall
* management of all the lobbies, the clients and the state of the server.
*
* @author Alberto Faria
* @author Fábio Fontes
*/
public class NetServer
{
    /**
     * Indicates the state of the game in which the client is now, this
     * state determines if the action that the player is trying to execute is
     * allowed.
     */
    private static enum ClientState
    {
        DISCONNECTED,

        CONNECTED,   // Connected to the server, but not logged in.

        IDLE,        // Logged in, but not in matchmaking nor in any lobby.

        IN_MATCHMAKING,   // In matchmaking.

        MATCH_FOUND, // In matchmaking, match was found.

        IN_LOBBY     // In a match lobby.
    }

    /* ---------------------------------------------------------------------- */

    private class Client extends ClientMessenger
    {
        private ClientState state;

        // null iff state = DISCONNECTED || state = CONNECTED
        private Account account;

        // null iff state != MATCH_FOUND
        private MatchFound matchFound;

        // null iff state != IN_LOBBY
        private Lobby lobby;

        // null iff state != IN_LOBBY
        private Team team;

        /* ------------------------------------------------------------------ */

        private void assertState(ClientState... allowedStates)
        {
            if (!Arrays.asList(allowedStates).contains(state))
            {
                String[] expected =
                    Stream
                    .of(allowedStates)
                    .map(Object::toString)
                    .toArray(String[]::new);

                throw new IllegalStateException(String.format(
                    "Invalid client state for method: state is %s, expected"
                    + " %s.",
                    state, String.join(", ", expected)
                    ));
            }
        }

        private void onFailedToAcceptMatch(MatchFound matchFound)
        {
            synchronized (NetServer.this)
            {
                if (matchFound.timerWasCanceled())
                    return;

                // kick offending players out of matchmaking

                matchFound
                    .getPlayersThatDidNotAccept()
                    .stream()
                    .map(authenticatedClients::get)
                    .forEach(c -> {
                        c.state = ClientState.IDLE;
                        c.matchFound = null;

                        c.sendLeftMatchmaking(
                            LeftMatchmakingCause.FAILED_TO_ACCEPT_MATCH
                            );
                    });

                // return other players to matchmaking

                matchFound
                    .getPlayersThatAccepted()
                    .stream()
                    .map(authenticatedClients::get)
                    .forEach(c -> {
                        c.state = ClientState.IN_MATCHMAKING;
                        c.matchFound = null;

                        c.sendMatchCanceled();

                        c.addThisPlayerToMatchmaking();
                    });
            }
        }

        private void onFailedToSelectHero(Lobby lobby)
        {
            synchronized (NetServer.this)
            {
                // inform all players that the lobby died

                lobby.getAllPlayerUsernames()
                    .stream()
                    .map(authenticatedClients::get)
                    .forEach(c -> {
                        c.state = ClientState.IDLE;
                        c.lobby = null;
                        c.team  = null;

                        c.sendLobbyDied(
                            LobbyCauseOfDeath.PLAYER_DIDNT_SELECT_HERO
                            );
                    });
            }
        }

        private void addThisPlayerToMatchmaking()
        {
            Set< String > matchUsernames = matchmaker.addPlayer(
                account.getUsername(),
                account.getIntegerRank()
                );

            ++numPlayersInMatchmaking;

            // if match was found

            if (matchUsernames != null)
            {
                numPlayersInMatchmaking -= Config.LOBBY_SIZE;

                List< Client > matchClients =
                    matchUsernames
                    .stream()
                    .map(authenticatedClients::get)
                    .collect(Collectors.toList());

                MatchFound matchFound = new MatchFound(
                    matchUsernames,
                    timeToAcceptMatch,
                    this::onFailedToAcceptMatch
                    );

                for (Client c : matchClients)
                {
                    c.state = ClientState.MATCH_FOUND;
                    c.matchFound = matchFound;

                    c.sendMatchFound(timeToAcceptMatch);
                }

                matchFound.startTimer();
            }
        }

        /* ------------------------------------------------------------------ */

        public Client(Socket socket)
        {
            super(socket);

            this.state = ClientState.CONNECTED;

            this.account = null;

            this.matchFound = null;

            this.lobby = null;
            this.team  = null;
        }

        /* ------------------------------------------------------------------ */

        @Override
        protected void onDisconnect(Throwable cause)
        {
            synchronized (NetServer.this)
            {
                switch (state)
                {
                case IN_LOBBY:
                    {
                        lobby.cancelTimer();

                        lobby
                            .getAllPlayerUsernames()
                            .stream()
                            .map(authenticatedClients::get)
                            .filter(c -> c != this)
                            .forEach(c -> {
                                c.state = ClientState.IDLE;
                                c.lobby = null;
                                c.team  = null;

                                c.sendLobbyDied(LobbyCauseOfDeath.PLAYER_LEFT);
                            });
                    }
                    break;

                case MATCH_FOUND:
                    {
                        matchFound.cancelTimer();

                        // return other players to matchmaking

                        matchFound
                            .getPlayerUsernames()
                            .stream()
                            .map(authenticatedClients::get)
                            .filter(c -> c != this)
                            .forEach(c -> {
                                c.state = ClientState.IN_MATCHMAKING;
                                c.matchFound = null;

                                c.sendMatchCanceled();

                                c.addThisPlayerToMatchmaking();
                            });
                    }
                    break;

                case IN_MATCHMAKING:
                    {
                        matchmaker.removePlayer(
                            account.getUsername(),
                            account.getIntegerRank()
                            );

                        --numPlayersInMatchmaking;
                    }
                    break;

                default:
                    break;
                }

                if (state != ClientState.DISCONNECTED && state != ClientState.CONNECTED)
                    authenticatedClients.remove(account.getUsername());

                state = ClientState.DISCONNECTED;
                account = null;
                matchFound = null;
                lobby = null;
                team = null;

                connectedClients.remove(this);

                // print disconnection cause

                if (cause != null)
                    cause.printStackTrace();
            }
        }

        @Override
        protected void onLoginReceived(
            String username, String password
            )
        {
            synchronized (NetServer.this)
            {
                assertState(ClientState.CONNECTED);

                try
                {
                    account = accountManager.login(username, password);

                    if (authenticatedClients.putIfAbsent(username, this) != null)
                        throw new LoginErrorException(LoginError.ALREADY_LOGGED_IN);
                }
                catch (LoginErrorException e)
                {
                    sendLoginFailed(e.getError());
                    return;
                }

                state = ClientState.IDLE;

                sendLoginSucceeded(account.getAccountInfo(), getStats());
            }
        }

        @Override
        protected void onSignUpReceived(
            String username, String password
            )
        {
            synchronized (NetServer.this)
            {
                assertState(ClientState.CONNECTED);

                try
                {
                    account = accountManager.register(username, password);

                    authenticatedClients.put(username, this);
                }
                catch (SignUpErrorException e)
                {
                    sendSignUpFailed(e.getError());
                    return;
                }

                state = ClientState.IDLE;

                sendLoginSucceeded(account.getAccountInfo(), getStats());
            }
        }

        @Override
        protected void onLogoutReceived()
        {
            synchronized (NetServer.this)
            {
                assertState(ClientState.IDLE);

                authenticatedClients.remove(account.getUsername());

                state = ClientState.CONNECTED;
                account = null;

                sendLogoutSucceeded();
            }
        }

        @Override
        protected void onJoinMatchmakingReceived()
        {
            synchronized (NetServer.this)
            {
                assertState(
                    ClientState.IDLE,
                    ClientState.IN_MATCHMAKING,
                    ClientState.MATCH_FOUND
                    );

                if (state == ClientState.IDLE)
                {
                    state = ClientState.IN_MATCHMAKING;

                    sendJoinedMatchmaking();

                    addThisPlayerToMatchmaking();
                }
            }
        }

        @Override
        protected void onLeaveMatchmakingReceived()
        {
            synchronized (NetServer.this)
            {
                assertState(
                    ClientState.IDLE,
                    ClientState.IN_MATCHMAKING,
                    ClientState.MATCH_FOUND
                    );

                if (state == ClientState.IN_MATCHMAKING)
                {
                    state = ClientState.IDLE;

                    matchmaker.removePlayer(
                         account.getUsername(),
                         account.getIntegerRank()
                         );

                    --numPlayersInMatchmaking;

                    sendLeftMatchmaking(LeftMatchmakingCause.VOLUNTARY);
                }
            }
        }

        @Override
        protected void onAcceptMatchReceived()
        {
            synchronized (NetServer.this)
            {
                assertState(
                    ClientState.IDLE,
                    ClientState.IN_MATCHMAKING,
                    ClientState.MATCH_FOUND,
                    ClientState.IN_LOBBY
                    );

                if (state == ClientState.MATCH_FOUND)
                {
                    if (matchFound.playerAcceptedMatch(account.getUsername()))
                        return;

                    // notify all other players

                    matchFound
                        .getPlayerUsernames()
                        .stream()
                        .map(authenticatedClients::get)
                        .forEach(c -> {
                            c.sendPlayerAcceptedMatch();
                        });

                    // ---

                    if (matchFound.acceptMatch(account.getUsername()))
                    {
                        // all players accepted match

                        // cancel match timer

                        matchFound.cancelTimer();

                        List< Client > lobbyClients =
                            matchFound
                            .getPlayerUsernames()
                            .stream()
                            .map(authenticatedClients::get)
                            .collect(Collectors.toList());

                        List< Lobby.Player > lobbyPlayers =
                            lobbyClients
                            .stream()
                            .map(c -> new Lobby.Player(
                                c.account.getUsername(),
                                c.account.getIntegerRank()
                                ))
                            .collect(Collectors.toList());

                        // create lobby

                        Lobby lobby = new Lobby(
                            lobbyPlayers,
                            timeToSelectHero,
                            this::onFailedToSelectHero
                            );

                        // notify players

                        for (Team team : Team.values())
                        {
                            TeamInfo teamInfo = lobby.getTeamInfo(team);

                            List< Client > teamClients =
                                lobby
                                .getTeamPlayerUsernames(team)
                                .stream()
                                .map(authenticatedClients::get)
                                .collect(Collectors.toList());

                            for (Client c : teamClients)
                            {
                                c.state      = ClientState.IN_LOBBY;
                                c.matchFound = null;
                                c.lobby      = lobby;
                                c.team       = team;

                                c.sendJoinedLobby(teamInfo, timeToSelectHero);
                            }
                        }

                        // ---

                        lobby.startTimer();
                    }
                }
            }
        }

        @Override
        protected void onDeclineMatchReceived()
        {
            synchronized (NetServer.this)
            {
                assertState(
                    ClientState.IDLE,
                    ClientState.IN_MATCHMAKING,
                    ClientState.MATCH_FOUND,
                    ClientState.IN_LOBBY
                    );

                if (state == ClientState.MATCH_FOUND)
                {
                    matchFound.cancelTimer();

                    // return other players to matchmaking

                    matchFound
                        .getPlayerUsernames()
                        .stream()
                        .map(authenticatedClients::get)
                        .filter(c -> c != this)
                        .forEach(c -> {
                            c.state = ClientState.IN_MATCHMAKING;
                            c.matchFound = null;

                            c.sendMatchCanceled();

                            c.addThisPlayerToMatchmaking();
                        });

                    // kick this player out of matchmaking

                    state = ClientState.IDLE;
                    matchFound = null;

                    sendLeftMatchmaking(LeftMatchmakingCause.DECLINED_MATCH);
                }
            }
        }

        @Override
        protected void onSelectHeroReceived(Hero hero)
        {
            synchronized (NetServer.this)
            {
                assertState(
                    ClientState.IDLE,
                    ClientState.IN_LOBBY
                    );

                if (state == ClientState.IN_LOBBY)
                {
                    // get index of player that sent

                    int playerIndex = lobby.getTeamPlayerIndex(
                        account.getUsername()
                        );

                    // try selecting the hero

                    if (lobby.trySelectHero(team, playerIndex, hero))
                    {
                        // hero was in fact selected, notify all team players

                        lobby
                            .getTeamPlayerUsernames(team)
                            .stream()
                            .map(authenticatedClients::get)
                            .forEach(c -> {
                                c.sendPlayerSelectedHero(playerIndex, hero);
                            });
                    }

                    // check if all players selected a hero

                    if (lobby.didEveryPlayerSelectAHero())
                    {
                        Random r = ThreadLocalRandom.current();

                        MatchSummary matchSummary = lobby.playMatch();
                        Team winner = matchSummary.getWinner();

                        lobby
                            .getTeamPlayerUsernames(winner)
                            .stream()
                            .map(authenticatedClients::get)
                            .forEach(c -> {
                                c.account.incrementNumWonMatches();

                                double newRank = Math.min(
                                    Config.MAX_RANK,
                                    c.account.getDoubleRank()
                                    + (0.8 + 0.4 * r.nextDouble())
                                    );

                                c.account.setDoubleRank(newRank);
                            });

                        lobby
                            .getTeamPlayerUsernames(Team.other(winner))
                            .stream()
                            .map(authenticatedClients::get)
                            .forEach(c -> {
                                c.account.incrementNumLostMatches();

                                double newRank = Math.max(
                                    Config.MIN_RANK,
                                    c.account.getDoubleRank() -
                                    (0.8 + 0.4 * r.nextDouble())
                                    );

                                c.account.setDoubleRank(newRank);
                            });

                        // cancel lobby timer

                        lobby.cancelTimer();

                        // match was played, notify all players and return them
                        // to idle state

                        lobby
                            .getAllPlayerUsernames()
                            .stream()
                            .map(authenticatedClients::get)
                            .forEach(c -> {
                                c.state = ClientState.IDLE;
                                c.lobby = null;
                                c.team  = null;

                                c.sendMatchPlayed(
                                    matchSummary,
                                    c.account.getIntegerRank()
                                    );
                            });
                    }
                }
            }
        }

        @Override
        protected void onChatMessageReceived(String chatMessage)
        {
            synchronized (NetServer.this)
            {
                assertState(
                    ClientState.IDLE,
                    ClientState.IN_LOBBY
                    );

                if (state == ClientState.IN_LOBBY)
                {
                    // get index of player that sent

                    int playerIndex = lobby.getTeamPlayerIndex(
                        account.getUsername()
                        );

                    // send message to all team players

                    lobby
                        .getTeamPlayerUsernames(team)
                        .stream()
                        .map(authenticatedClients::get)
                        .forEach(c -> {
                            c.sendChatMessage(playerIndex, chatMessage);
                        });
                }
            }
        }

        @Override
        protected void onLeaveLobbyReceived()
        {
            synchronized (NetServer.this)
            {
                assertState(
                    ClientState.IN_LOBBY,
                    ClientState.IDLE
                    );

                if (state == ClientState.IN_LOBBY)
                {
                    // cancel lobby timer

                    lobby.cancelTimer();

                    // inform all players that the lobby died and return them to
                    // idle state

                    lobby
                        .getAllPlayerUsernames()
                        .stream()
                        .map(authenticatedClients::get)
                        .forEach(c -> {
                            c.state = ClientState.IDLE;
                            c.lobby = null;
                            c.team  = null;

                            c.sendLobbyDied(LobbyCauseOfDeath.PLAYER_LEFT);
                        });
                }
            }
        }
    }

    /* ---------------------------------------------------------------------- */

    private final Consumer< Throwable > onStop;
    private final AtomicBoolean userRequestedStop;

    private final ServerSocket serverSocket;
    private final Thread serverSocketThread;

    // Accesses to accountManager must be synchronized on this.
    private final AccountManager accountManager;

    private final Matchmaker matchmaker;
    private int numPlayersInMatchmaking;

    private final double timeToAcceptMatch;
    private final double timeToSelectHero;

    // Accesses to connectedClients must be synchronized on this.
    private final List< Client > connectedClients;

    // Accesses to loggedClients must be synchronized on this.
    private final Map< String, Client > authenticatedClients;

    // Accesses to lobbies must be synchronized on this.
    private final List< Lobby > lobbies;

    private final Timer statsTimer;

    /* ---------------------------------------------------------------------- */

    private void acceptConnections()
    {
        try
        {
            while (true)
            {
                // accept connection

                Socket clientSocket = serverSocket.accept();

                // create client

                Client client = new Client(clientSocket);

                // add client to connected clients list

                synchronized (connectedClients)
                {
                    connectedClients.add(client);
                }

                // start client

                client.start();
            }
        }
        catch (Throwable t)
        {
            List< Client > clients;

            synchronized (this)
            {
                // disconnect all clients

                for (Client c : connectedClients)
                    c.disconnect();

                // copy connected clients list

                clients = new ArrayList<>(connectedClients);
            }

            // wait for all clients to be disconnected

            for (Client c : clients)
                c.waitUntilDisconnected();

            // save player accounts

            accountManager.saveToAccountFile();

            // run onStop callback

            onStop.accept(userRequestedStop.get() ? null : t);
        }
    }

    private boolean stateMatches(ClientState state, ClientState... states)
    {
        return Arrays.asList(states).contains(state);
    }

    private synchronized void broadcastServerStats()
    {
        ServerStats serverStats = getStats();

        for (Client c : connectedClients)
        {
            boolean send = stateMatches(
                c.state,
                ClientState.IDLE,
                ClientState.IN_MATCHMAKING,
                ClientState.MATCH_FOUND
                );

            if (send)
                c.sendServerStats(serverStats);
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Creates and starts a new NetServer.
     * <p>
     * The onStop parameter specifies a callback that is to be run whenever the
     * server stops. The argument to that callback specified the cause that lead
     * to the server being stopped, or null if the server was stopped as result
     * of an invocation of {@link #stop()}.
     *
     * @param args the arguments to the server
     * @param onStop callback to be run when the server stops
     *
     * @throws NullPointerException if args or onStop are null
     */
    public NetServer(Arguments args, Consumer< Throwable > onStop)
        throws IOException
    {
        this.onStop            = Objects.requireNonNull(onStop);
        this.userRequestedStop = new AtomicBoolean(false);

        this.serverSocket       = new ServerSocket(args.getPort());
        this.serverSocketThread = new Thread(this::acceptConnections);

        this.accountManager = new AccountManager(args.getPlayerAccountsFile());

        this.matchmaker              = new Matchmaker();
        this.numPlayersInMatchmaking = 0;

        this.timeToAcceptMatch = args.getTimeToAcceptMatch();
        this.timeToSelectHero  = args.getTimeToSelectHero();

        this.connectedClients     = new ArrayList<>();
        this.authenticatedClients = new HashMap<>();
        this.lobbies              = new ArrayList<>();

        this.statsTimer = new Timer();

        // start server socket thread

        this.serverSocketThread.start();

        // schedule server statistics refresh timer

        long statsTimerPeriod = (long)(args.getServerStatsInterval() * 1000.);

        TimerTask statsTimerTask = new TimerTask() {
            @Override
            public void run()
            {
                broadcastServerStats();
            }
        };

        this.statsTimer.schedule(
            statsTimerTask,
            statsTimerPeriod, statsTimerPeriod
            );
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns statistics about the server.
     *
     * @return statistics about the server
     */
    public synchronized ServerStats getStats()
    {
        return new ServerStats(
            accountManager.getNumAccounts(),
            authenticatedClients.size(),
            numPlayersInMatchmaking,
            lobbies.size()
            );
    }

    /**
     * Synchronously stops the server.
     * <p>
     * Note that this method blocks until the server is fully stopped. If the
     * server is already not running, this method returns immediately.
     */
    public void stop()
    {
        userRequestedStop.set(true);

        // cancel server stats time

        statsTimer.cancel();

        // close server socket

        try
        {
            serverSocket.close();
        }
        catch (Throwable t)
        {
        }

        // join server socket thread

        while (true)
        {
            try
            {
                serverSocketThread.join();
                break;
            }
            catch (InterruptedException e)
            {
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
