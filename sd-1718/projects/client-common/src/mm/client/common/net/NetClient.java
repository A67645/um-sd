/* -------------------------------------------------------------------------- */

package mm.client.common.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import mm.client.common.data.TeamState;
import mm.common.data.AccountInfo;
import mm.common.data.Hero;
import mm.common.data.LeftMatchmakingCause;
import mm.common.data.LobbyCauseOfDeath;
import mm.common.data.LoginError;
import mm.common.data.MatchSummary;
import mm.common.data.ServerStats;
import mm.common.data.SignUpError;
import mm.common.data.TeamInfo;
import mm.common.util.TriConsumer;
import mm.common.util.Util;

/* -------------------------------------------------------------------------- */

/**
 * Encapsulates all networking-related tasks for a client.
 * <p>
 * Instances of this class are able to establish and relinquish a connection to
 * some appropriate server, also providing facilities for asynchronously sending
 * messages to that same server, and to process incoming messages from the
 * server.
 * <p>
 * This class allows its users to define what should happen when messages are
 * received from the server. This is accomplished through a callback mechanism:
 * the user can set, for each type of message that may be received, a callback
 * that is later executed whenever the corresponding message is received.
 * <p>
 * It is worth noting that each NetClient instance never executes callbacks
 * concurrently.
 * <p>
 * Additionally, if exceptions are thrown out of user-defined callbacks, their
 * stack trace is printed to stderr and the application is terminated
 * abnormally.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class NetClient
{
    /**
     * Enumerates the possible states of a {@link #NetClient()}.
     *
     * @author Alberto Faria
     * @author Fábio Fontes
     */
    public static enum State
    {
        /**
         * Not connected to a server.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #CONNECTING}.
         */
        DISCONNECTED,

        /**
         * Attempting to establish a connection with a server.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED} or {@link #CONNECTED}.
         */
        CONNECTING,

        /**
         * Connected to a server, but not logged in.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED}, {@link #LOGGING_IN}, or {@link #SIGNING_UP}.
         */
        CONNECTED,

        /**
         * Connected to a server, attempting to log in.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED}, {@link #CONNECTED}, or {@link #IDLE}.
         */
        LOGGING_IN,

        /**
         * Connected to a server, attempting to sign up.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED}, {@link #CONNECTED}, or {@link #IDLE}.
         */
        SIGNING_UP,

        /**
         * Logged in, not in matchmaking nor in any lobby.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED}, {@link #LOGGING_OUT}, or
         * {@link #JOINING_MATCHMAKING}.
         */
        IDLE,

        /**
         * Logged in, attempting to logout.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED} or {@link #CONNECTED}.
         */
        LOGGING_OUT,

        /**
         * Logged in, attempting to join matchmaking.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED} or {@link #IN_MATCHMAKING}.
         */
        JOINING_MATCHMAKING,

        /**
         * In matchmaking.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED}, {@link #IDLE}, or {@link #MATCH_FOUND}.
         */
        IN_MATCHMAKING,

        /**
         * In matchmaking, match found.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED}, {@link #IDLE}, {@link #IN_MATCHMAKING}, or
         * {@link #IN_LOBBY}.
         */
        MATCH_FOUND,

        /**
         * In a match lobby.
         * <p>
         * A NetClient in this state may transition directly to state
         * {@link #DISCONNECTED} or {@link #IDLE}.
         */
        IN_LOBBY
    }

    /* ---------------------------------------------------------------------- */

    private class Messenger extends ServerMessenger
    {
        public Messenger(Socket socket)
        {
            super(socket);
        }

        @Override
        protected void onDisconnect(Throwable cause)
        {
            synchronized (NetClient.this)
            {
                accountInfo = null;

                state = State.DISCONNECTED;
                NetClient.this.notifyAll();

                Util.runCallback(onDisconnect, cause);
            }
        }

        @Override
        protected void onLoginSucceededReceived(
            AccountInfo accountInfo, ServerStats serverStats
            )
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.LOGGING_IN, State.SIGNING_UP);

                state = State.IDLE;

                NetClient.this.accountInfo = accountInfo;
                NetClient.this.serverStats = serverStats;

                Util.runCallback(onLoginSucceeded);
            }
        }

        @Override
        protected void onLoginFailedReceived(LoginError error)
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.LOGGING_IN);

                state = State.CONNECTED;

                Util.runCallback(onLoginFailed, error);
            }
        }

        @Override
        protected void onSignUpFailedReceived(SignUpError error)
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.SIGNING_UP);

                state = State.CONNECTED;

                Util.runCallback(onSignUpFailed, error);
            }
        }

        @Override
        protected void onLogoutSucceededReceived()
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.LOGGING_OUT);

                state = State.CONNECTED;

                accountInfo = null;
                serverStats = null;

                Util.runCallback(onLogoutSucceeded);
            }
        }

        @Override
        protected void onServerStatsReceived(ServerStats serverStats)
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(
                    State.IDLE,
                    State.LOGGING_OUT,
                    State.JOINING_MATCHMAKING,
                    State.IN_MATCHMAKING,
                    State.MATCH_FOUND
                    );

                NetClient.this.serverStats = serverStats;

                Util.runCallback(onServerStatsReceived);
            }
        }

        @Override
        protected void onJoinedMatchmakingReceived()
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(
                    State.JOINING_MATCHMAKING,
                    State.MATCH_FOUND
                    );

                state = State.IN_MATCHMAKING;

                Util.runCallback(onJoinedMatchmaking);
            }
        }

        @Override
        protected void onLeftMatchmakingReceived(LeftMatchmakingCause cause)
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.IN_MATCHMAKING, State.MATCH_FOUND);

                state = State.IDLE;

                Util.runCallback(onLeftMatchmaking, cause);
            }
        }

        @Override
        protected void onMatchFoundReceived(double timeToAcceptMatch)
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.IN_MATCHMAKING);

                state = State.MATCH_FOUND;

                Util.runCallback(onMatchFound, timeToAcceptMatch);
            }
        }

        @Override
        protected void onMatchCanceledReceived()
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.MATCH_FOUND);

                state = State.IN_MATCHMAKING;

                Util.runCallback(onMatchCanceled);
            }
        }

        @Override
        protected void onPlayerAcceptedMatchReceived()
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.MATCH_FOUND);

                Util.runCallback(onPlayerAcceptedMatch);
            }
        }

        @Override
        protected void onJoinedLobbyReceived(
            TeamInfo lobbyInfo, double timeToSelectHero
            )
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.MATCH_FOUND);

                state = State.IN_LOBBY;

                teamState = new TeamState(lobbyInfo);

                Util.runCallback(onJoinedLobby, timeToSelectHero);
            }
        }

        @Override
        protected void onLobbyDiedReceived(LobbyCauseOfDeath causeOfDeath)
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.IN_LOBBY);

                state = State.IDLE;

                teamState = null;

                Util.runCallback(onLobbyDied, causeOfDeath);
            }
        }

        @Override
        protected void onPlayerSelectedHeroReceived(
            int playerIndex, Hero hero
            )
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.IN_LOBBY);

                Hero oldHero = teamState.getSelectedHero(playerIndex);

                teamState.setSelectedHero(playerIndex, hero);

                Util.runCallback(
                    onPlayerSelectedHero,
                    playerIndex, oldHero, hero
                    );
            }
        }

        @Override
        protected void onChatMessageReceived(
            int playerIndex, String message
            )
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.IN_LOBBY);

                Util.runCallback(onChatMessageReceived, playerIndex, message);
            }
        }

        @Override
        protected void onMatchPlayedReceived(
            MatchSummary matchSummary, int newRank
            )
        {
            synchronized (NetClient.this)
            {
                assertStateReceive(State.IN_LOBBY);

                state = State.IDLE;

                int oldRank = accountInfo.getRank();

                boolean won =
                    teamState.getInfo().getTeam() == matchSummary.getWinner();

                accountInfo = new AccountInfo(
                    accountInfo.getUsername(),
                    accountInfo.getNumWonMatches() + (won ? 1 : 0),
                    accountInfo.getNumLostMatches() + (won ? 0 : 1),
                    newRank
                    );

                teamState = null;

                Util.runCallback(onMatchPlayed, matchSummary, oldRank, newRank);
            }
        }
    }

    /* ---------------------------------------------------------------------- */

    // Should only be observed when synchronized on this.
    // When setting to DISCONNECTED or when changing value from CONNECTING,
    // this.notifyAll() should be invoked.
    private State state;

    // Should only be observed when synchronized on this.
    // Initially null. Value is only meaningful when State != CONNECTING.
    private Messenger messenger;

    // Should only be observed when synchronized on this.
    // This is set to true when disconnect() is called when state is CONNECTING.
    // The connector thread checks this flag after successfuly establishing a
    // connection to the server. If true, the connection is closed.
    private boolean userRequestedDisconnect;

    private AccountInfo accountInfo;
    private ServerStats serverStats;
    private TeamState   teamState;

    // callbacks

    private AtomicReference< Runnable > onConnectSucceded;
    private AtomicReference< Consumer< Throwable > > onConnectFailed;
    private AtomicReference< Consumer< Throwable > > onDisconnect;

    private AtomicReference< Runnable > onLoginSucceeded;
    private AtomicReference< Consumer< LoginError > > onLoginFailed;
    private AtomicReference< Consumer< SignUpError > > onSignUpFailed;

    private AtomicReference< Runnable > onLogoutSucceeded;
    private AtomicReference< Runnable > onServerStatsReceived;

    private AtomicReference< Runnable > onJoinedMatchmaking;
    private AtomicReference< Consumer< LeftMatchmakingCause > > onLeftMatchmaking;

    private AtomicReference< Consumer< Double > > onMatchFound;
    private AtomicReference< Runnable > onMatchCanceled;
    private AtomicReference< Runnable > onPlayerAcceptedMatch;

    private AtomicReference< Consumer< Double > > onJoinedLobby;
    private AtomicReference< Consumer< LobbyCauseOfDeath > > onLobbyDied;
    private AtomicReference< TriConsumer< Integer, Hero, Hero > > onPlayerSelectedHero;
    private AtomicReference< BiConsumer< Integer, String > > onChatMessageReceived;

    private AtomicReference< TriConsumer< MatchSummary, Integer, Integer > > onMatchPlayed;

    /* ---------------------------------------------------------------------- */

    // Must only be called when synchronized on this.
    private boolean stateMatches(State... states)
    {
        return Arrays.asList(states).contains(state);
    }

    // Must only be called when synchronized on this.
    private void assertState(State... allowedStates)
    {
        if (!stateMatches(allowedStates))
        {
            String[] expected =
                Stream
                .of(allowedStates)
                .map(Object::toString)
                .toArray(String[]::new);

            throw new IllegalStateException(String.format(
                "Invalid state for method: state is %s, expected %s.",
                state, String.join(", ", expected)
                ));
        }
    }

    // Must only be called when synchronized on this.
    private void assertStateReceive(State... allowedStates)
    {
        if (!stateMatches(allowedStates))
        {
            String[] expected =
                Stream
                .of(allowedStates)
                .map(Object::toString)
                .toArray(String[]::new);

            throw new IllegalStateException(String.format(
                "Invalid state for message received from server:"
                + " state is %s, expected %s.",
                state, String.join(", ", expected)
                ));
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Creates a new instance of NetClient.
     */
    public NetClient()
    {
        state = State.DISCONNECTED;

        messenger = null;

        userRequestedDisconnect = false;

        accountInfo = null;
        serverStats = null;
        teamState   = null;

        // callbacks

        onConnectSucceded     = new AtomicReference<>();
        onConnectFailed       = new AtomicReference<>();
        onDisconnect          = new AtomicReference<>();

        onLoginSucceeded      = new AtomicReference<>();
        onLoginFailed         = new AtomicReference<>();
        onSignUpFailed        = new AtomicReference<>();

        onLogoutSucceeded     = new AtomicReference<>();
        onServerStatsReceived = new AtomicReference<>();

        onJoinedMatchmaking   = new AtomicReference<>();
        onLeftMatchmaking     = new AtomicReference<>();

        onMatchFound          = new AtomicReference<>();
        onMatchCanceled       = new AtomicReference<>();
        onPlayerAcceptedMatch = new AtomicReference<>();

        onJoinedLobby         = new AtomicReference<>();
        onLobbyDied           = new AtomicReference<>();
        onPlayerSelectedHero  = new AtomicReference<>();
        onChatMessageReceived = new AtomicReference<>();

        onMatchPlayed         = new AtomicReference<>();
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the current state of this NetClient.
     *
     * @return the current state of this NetClient
     */
    public synchronized State getState()
    {
        return state;
    }

    /**
     * Returns information about the account of the currently logged in player.
     * <p>
     * This method may only be called when this NetClient's state is IDLE,
     * LOGGING_OUT, JOINING_MATCHMAKING, IN_MATCHMAKING, MATCH_FOUND, or
     * IN_LOBBY.
     *
     * @return information about the account of the currently logged in player
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized AccountInfo getAccountInfo()
    {
        assertState(
            State.IDLE,
            State.LOGGING_OUT,
            State.JOINING_MATCHMAKING,
            State.IN_MATCHMAKING,
            State.MATCH_FOUND,
            State.IN_LOBBY
            );

        return accountInfo;
    }

    /**
     * Returns the latest received server statistics.
     * <p>
     * This method may only be called when this NetClient's state is IDLE,
     * LOGGING_OUT, JOINING_MATCHMAKING, IN_MATCHMAKING, MATCH_FOUND, or
     * IN_LOBBY.
     *
     * @return the latest received server statistics
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized ServerStats getServerStats()
    {
        assertState(
            State.IDLE,
            State.LOGGING_OUT,
            State.JOINING_MATCHMAKING,
            State.IN_MATCHMAKING,
            State.MATCH_FOUND,
            State.IN_LOBBY
            );

        return serverStats;
    }

    /**
     * Returns information about the player's team in the current lobby.
     * <p>
     * This method may only be called when this NetClient's state is IN_LOBBY.
     *
     * @return information about the player's team in the current lobby
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized TeamInfo getTeamInfo()
    {
        assertState(State.IN_LOBBY);

        return teamState.getInfo();
    }

    public synchronized List< Hero > getUnselectedHeroes()
    {
        assertState(State.IN_LOBBY);

        return teamState.getUnselectedHeroes();
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Asynchronously attempts to establish a connected with the server at the
     * specified endpoint.
     * <p>
     * If serverEndpoint is unresolved, an attempt is made to resolve it
     * asynchronously.
     * <p>
     * This method may only be called when this NetClient's state is
     * DISCONNECTED. Invoking this method changes this NetClient's state to
     * CONNECTING.
     *
     * @param serverEndpoint the endpoint of the server
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     * @throws NullPointerException if serverEndpoint is null
     */
    public synchronized void connect(InetSocketAddress serverEndpoint)
    {
        Objects.requireNonNull(serverEndpoint);

        assertState(State.DISCONNECTED);

        state = State.CONNECTING;

        Thread connectorThread = new Thread(() ->
        {
            Socket socket = null;

            try
            {
                // resolve server endpoint if not yet resolved

                InetSocketAddress endpoint;

                if (serverEndpoint.isUnresolved())
                {
                    InetAddress address = InetAddress.getByName(
                        serverEndpoint.getHostString()
                        );

                    endpoint = new InetSocketAddress(
                        address, serverEndpoint.getPort()
                        );
                }
                else
                {
                    endpoint = serverEndpoint;
                }

                // create socket

                socket = new Socket(endpoint.getAddress(), endpoint.getPort());

                // create messenger

                synchronized (this)
                {
                    messenger = new Messenger(socket);
                }
            }
            catch (Throwable t)
            {
                if (socket != null)
                {
                    try
                    {
                        socket.close();
                    }
                    catch (Throwable t2)
                    {
                    }

                    socket = null;
                }

                synchronized (this)
                {
                    userRequestedDisconnect = false;

                    state = State.DISCONNECTED;
                    this.notifyAll();
                }

                Util.runCallback(onConnectFailed, t);

                return;
            }

            // change state and run callback

            synchronized (this)
            {
                state = State.CONNECTED;
            }

            Util.runCallback(onConnectSucceded);

            // start the messenger

            messenger.start();

            // check if user called disconnect() while connecting

            synchronized (this)
            {
                if (userRequestedDisconnect)
                {
                    userRequestedDisconnect = false;
                    disconnect();
                }
            }
        });

        connectorThread.start();
    }

    /**
     * Asynchronously closes the connection to the server.
     * <p>
     * If no connection with a server exists, no action is taken.
     * <p>
     * This method may be called at any time. Invoking this method does not
     * change this NetClient's state.
     */
    public synchronized void disconnect()
    {
        if (state == State.CONNECTING)
            userRequestedDisconnect = true;
        else if (state != State.DISCONNECTED)
            messenger.disconnect();
    }

    /**
     * Waits until this NetClient is not connected to any server.
     * <p>
     * If upon calling this method the NetClient is already not connect to any
     * server, this method return immediately.
     * <p>
     * This method may be called at any time. Invoking this method does not
     * change this NetClient's state. However, it is guaranteed that when this
     * method does return, this NetClient's state is DISCONNECTED.
     * <p>
     * No callbacks will be invoked by this NetClient from the moment this
     * method returns until a call to {@link #connect(InetSocketAddress)} is
     * performed.
     */
    public void waitUntilDisconnected()
    {
        Messenger m;

        // wait until state is DISCONNECTED

        synchronized (this)
        {
            while (state != State.DISCONNECTED)
            {
                try
                {
                    this.wait();
                }
                catch (InterruptedException e)
                {
                }
            }

            m = messenger;
        }

        // wait until onDisconnect finished execution and Messenger is fully
        // disconnected

        if (m != null)
            m.waitUntilDisconnected();
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Asynchronously sends a login request to the server.
     * <p>
     * This method may only be called when this NetClient's state is CONNECTED.
     * Invoking this method changes this NetClient's state to LOGGING_IN.
     *
     * @param username the account's username
     * @param password the account's password
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     * @throws NullPointerException if username or password are null
     * @throws IllegalArgumentException if username or password are invalid
     */
    public synchronized void login(String username, String password)
    {
        assertState(State.CONNECTED);

        messenger.sendLogin(username, password);

        state = State.LOGGING_IN;
    }

    /**
     * Asynchronously sends a sign up request to the server.
     * <p>
     * This method may only be called when this NetClient's state is CONNECTED.
     * Invoking this method changes this NetClient's state to SIGNING_UP.
     *
     * @param username the account's username
     * @param password the account's password
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     * @throws NullPointerException if username or password are null
     * @throws IllegalArgumentException if username or password are invalid
     */
    public synchronized void signUp(String username, String password)
    {
        assertState(State.CONNECTED);

        messenger.sendSignUp(username, password);

        state = State.SIGNING_UP;
    }

    /**
     * Asynchronously sends a logout request to the server.
     * <p>
     * This method may only be called when this NetClient's state is IDLE.
     * Invoking this method changes this NetClient's state to LOGGING_OUT.
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized void logout()
    {
        assertState(State.IDLE);

        messenger.sendLogout();

        state = State.LOGGING_OUT;
    }

    /**
     * Asynchronously sends a request to join matchmaking to the server.
     * <p>
     * This method may only be called when this NetClient's state is IDLE.
     * Invoking this method changes this NetClient's state to
     * JOINING_MATCHMAKING.
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized void joinMatchmaking()
    {
        assertState(State.IDLE);

        messenger.sendJoinMatchmaking();

        state = State.JOINING_MATCHMAKING;
    }

    /**
     * Asynchronously sends a request to leave matchmaking to the server.
     * <p>
     * This method may only be called when this NetClient's state is
     * IN_MATCHMAKING. Invoking this method does not change this NetClient's
     * state.
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized void leaveMatchmaking()
    {
        assertState(State.IN_MATCHMAKING);

        messenger.sendLeaveMatchmaking();
    }

    /**
     * Asynchronously sends a request to accept the current match to the server.
     * <p>
     * This method may only be called when this NetClient's state is
     * MATCH_FOUND. Invoking this method does not change this NetClient's state.
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized void acceptMatch()
    {
        assertState(State.MATCH_FOUND);

        messenger.sendAcceptMatch();
    }

    /**
     * Asynchronously sends a request to decline the current match to the
     * server.
     * <p>
     * This method may only be called when this NetClient's state is
     * MATCH_FOUND. Invoking this method does not change this NetClient's state.
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized void declineMatch()
    {
        assertState(State.MATCH_FOUND);

        messenger.sendDeclineMatch();
    }

    /**
     * Asynchronously sends a request to select the specified hero to the
     * server.
     * <p>
     * This method may only be called when this NetClient's state is IN_LOBBY.
     * Invoking this method does not change this NetClient's state.
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     * @throws NullPointerException if hero is null
     */
    public synchronized void selectHero(Hero hero)
    {
        assertState(State.IN_LOBBY);

        messenger.sendSelectHero(hero);
    }

    /**
     * Asynchronously sends a request to send the specified chat message to the
     * server.
     * <p>
     * This method may only be called when this NetClient's state is IN_LOBBY.
     * Invoking this method does not change this NetClient's state.
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     * @throws NullPointerException if chatMessage is null
     * @throws IllegalArgumentException if chatMessage is an invalid chat
     *         message
     */
    public synchronized void sendChatMessage(String chatMessage)
    {
        assertState(State.IN_LOBBY);

        messenger.sendChatMessage(chatMessage);
    }

    /**
     * Asynchronously sends a request to leave the current lobby to the server.
     * <p>
     * This method may only be called when this NetClient's state is IN_LOBBY.
     * Invoking this method does not change this NetClient's state.
     *
     * @throws IllegalStateException if this NetClient's state is inadequate
     */
    public synchronized void leaveLobby()
    {
        assertState(State.IN_LOBBY);

        messenger.sendLeaveLobby();
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Clears (unsets) all callbacks.
     */
    public void clearCallbacks()
    {
        onConnectSucceded.set(null);
        onConnectFailed.set(null);
        onDisconnect.set(null);

        onLoginSucceeded.set(null);
        onLoginFailed.set(null);
        onSignUpFailed.set(null);

        onLogoutSucceeded.set(null);
        onServerStatsReceived.set(null);

        onJoinedMatchmaking.set(null);
        onLeftMatchmaking.set(null);

        onMatchFound.set(null);
        onMatchCanceled.set(null);
        onPlayerAcceptedMatch.set(null);

        onJoinedLobby.set(null);
        onLobbyDied.set(null);
        onPlayerSelectedHero.set(null);
        onChatMessageReceived.set(null);

        onMatchPlayed.set(null);
    }

    /**
     * Sets the callback to be run upon successfully establishing a connection
     * to a server.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from CONNECTING to CONNECTED.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnConnectSucceded(Runnable callback)
    {
        onConnectSucceded.set(callback);
    }

    /**
     * Sets the callback to be run upon failure to establish a connection to a
     * server.
     * <p>
     * The callback's parameter specifies the reason why the attempt to
     * establish a connection failed.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from CONNECTING to DISCONNECTED.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnConnectFailed(Consumer< Throwable > callback)
    {
        onConnectFailed.set(callback);
    }

    /**
     * Sets the callback to be run upon loosing the connection to a server.
     * <p>
     * The callback's parameter specifies the reason why the connection was
     * lost, unless the connection was voluntarily relinquished by the user
     * through the {@link #disconnect()} function, in which case the callback's
     * parameter is null.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from some state other than DISCONNECTED and
     * DISCONNECTING to DISCONNECTED.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnDisconnect(Consumer< Throwable > callback)
    {
        onDisconnect.set(callback);
    }

    /**
     * Sets the callback to be run upon successfully logging in.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from LOGGING_IN or SIGNING_UP to IDLE.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnLoginSucceded(Runnable callback)
    {
        onLoginSucceeded.set(callback);
    }

    /**
     * Sets the callback to be run upon failing to login.
     * <p>
     * The callback's parameter specifies the reason why the login attempt
     * failed.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from LOGGING_IN to CONNECTED.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnLoginFailed(Consumer< LoginError > callback)
    {
        onLoginFailed.set(callback);
    }

    /**
     * Sets the callback to be run upon failing to sign up.
     * <p>
     * The callback's parameter specifies the reason why the sign up attempt
     * failed.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from SIGNING_UP to CONNECTED.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnSignUpFailed(Consumer< SignUpError > callback)
    {
        onSignUpFailed.set(callback);
    }

    /**
     * Sets the callback to be run upon successfully logging out.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from LOGGING_OUT to CONNECTED.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnLogoutSucceded(Runnable callback)
    {
        onLogoutSucceeded.set(callback);
    }

    /**
     * Sets the callback to be run upon receiving updated server statistics.
     * <p>
     * Server statistics may be received when this NetClient's state is IDLE,
     * LOGGING_OUT, IN_MATCHMAKING, or MATCH_FOUND.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnServerStatsReceived(Runnable callback)
    {
        onServerStatsReceived.set(callback);
    }

    /**
     * Sets the callback to be run upon joining matchmaking.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from JOINING_MATCHMAKING to IN_MATCHMAKING.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnJoinedMatchmaking(Runnable callback)
    {
        onJoinedMatchmaking.set(callback);
    }

    /**
     * Sets the callback to be run upon leaving matchmaking.
     * <p>
     * The callback's parameter specifies the reason why matchmaking was left.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from IN_MATCHMAKING or MATCH_FOUND to IDLE.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnLeftMatchmaking(Consumer< LeftMatchmakingCause > callback)
    {
        onLeftMatchmaking.set(callback);
    }

    /**
     * Sets the callback to be run upon finding a match.
     * <p>
     * The callback's parameter specifies the amount of time the user has to
     * accept the match, in seconds.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from IN_MATCHMAKING to MATCH_FOUND.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnMatchFound(Consumer< Double > callback)
    {
        onMatchFound.set(callback);
    }

    /**
     * Sets the callback to be run whenever a found match is canceled.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from MATCH_FOUND to IN_MATCHMAKING.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnMatchCanceled(Runnable callback)
    {
        onMatchCanceled.set(callback);
    }

    /**
     * Sets the callback to be run whenever a player accepts a found match.
     * <p>
     * Players may accept a found match when this NetClient's state is
     * MATCH_FOUND.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnPlayerAcceptedMatch(Runnable callback)
    {
        onPlayerAcceptedMatch.set(callback);
    }

    /**
     * Sets the callback to be run upon joining a lobby.
     * <p>
     * The callback's parameter specifies the amount of time the user has to
     * select a champion, in seconds.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from MATCH_FOUND to IN_LOBBY.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnJoinedLobby(Consumer< Double > callback)
    {
        onJoinedLobby.set(callback);
    }

    /**
     * Sets the callback to be run whenever the current lobby dies.
     * <p>
     * The callback's parameter specifies the reason why the lobby died.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from IN_LOBBY to IDLE.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnLobbyDied(Consumer< LobbyCauseOfDeath > callback)
    {
        onLobbyDied.set(callback);
    }

    /**
     * Sets the callback to be run whenever a player selects a hero.
     * <p>
     * The callback's first parameter specifies the index of the player in the
     * user's team who chose the hero. The second parameter specifies that
     * player's previously selected hero. The third parameter specifies the
     * newly selected hero.
     * <p>
     * Players may select a hero when this NetClient's state is IN_LOBBY.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnPlayerSelectedHero(
        TriConsumer< Integer, Hero, Hero > callback
        )
    {
        onPlayerSelectedHero.set(callback);
    }

    /**
     * Sets the callback to be run whenever a player sends a chat message.
     * <p>
     * The callback's first parameter specifies the index of the player in the
     * user's team who sent the chat message. The second parameter specifies
     * the chat message that was sent.
     * <p>
     * Players may send chat messages when this NetClient's state is IN_LOBBY.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnChatMessageReceived(
        BiConsumer< Integer, String > callback
        )
    {
        onChatMessageReceived.set(callback);
    }

    /**
     * Sets the callback to be run whenever a match is played.
     * <p>
     * The callback's first parameter gives information about the match. The
     * second parameter specifies the user's rank before the match. The third
     * parameter specifies the user's rank after the match.
     * <p>
     * This callback is always invoked as result of the transition of this
     * NetClient's state from IN_LOBBY to IDLE.
     *
     * @param callback the callback; null disables this callback
     */
    public void setOnMatchPlayed(
        TriConsumer< MatchSummary, Integer, Integer > callback
        )
    {
        onMatchPlayed.set(callback);
    }
}

/* -------------------------------------------------------------------------- */
