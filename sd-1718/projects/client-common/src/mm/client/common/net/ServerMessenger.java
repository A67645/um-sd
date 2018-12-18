/* -------------------------------------------------------------------------- */

package mm.client.common.net;

import java.net.Socket;

import mm.common.data.AccountInfo;
import mm.common.data.Hero;
import mm.common.data.LeftMatchmakingCause;
import mm.common.data.LobbyCauseOfDeath;
import mm.common.data.LoginError;
import mm.common.data.MatchSummary;
import mm.common.data.ServerStats;
import mm.common.data.SignUpError;
import mm.common.data.TeamInfo;
import mm.common.net.ClientToServerMsgId;
import mm.common.net.MessengerBase;
import mm.common.net.MsgReader;
import mm.common.net.MsgWriter;
import mm.common.net.ServerToClientMsgId;

/* -------------------------------------------------------------------------- */

/**
 * Encapsulates the encoding and decoding of messages sent to, and received
 * from, the server.
 * <p>
 * Note that on* method are never invoked concurrently on the same instance of
 * ServerMessenger.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public abstract class ServerMessenger extends MessengerBase
{
    /**
     * Creates a new ServerMessenger that uses the specified socket.
     *
     * @param socket the socket holding the connection to the server
     *
     * @throws NullPointerException if socket is null
     */
    public ServerMessenger(Socket socket)
    {
        super(socket);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    protected void onMessageReceived(String msg)
    {
        MsgReader reader = new MsgReader(msg);

        ServerToClientMsgId msgId = reader.readServerToClientMsgId();

        switch (msgId)
        {
        case LOGIN_SUCCEEDED:
            {
                AccountInfo accountInfo = reader.readAccountInfo();
                ServerStats serverStats = reader.readServerStats();

                onLoginSucceededReceived(accountInfo, serverStats);
            }
            break;

        case LOGIN_FAILED:
            {
                LoginError error = reader.readLoginError();

                onLoginFailedReceived(error);
            }
            break;

        case SIGN_UP_FAILED:
            {
                SignUpError error = reader.readSignUpError();

                onSignUpFailedReceived(error);
            }
            break;


        case LOGOUT_SUCCEEDED:
            {
                onLogoutSucceededReceived();
            }
            break;

        case SERVER_STATS:
            {
                ServerStats serverStats = reader.readServerStats();

                onServerStatsReceived(serverStats);
            }
            break;

        case JOINED_MATCHMAKING:
            {
                onJoinedMatchmakingReceived();
            }
            break;

        case LEFT_MATCHMAKING:
            {
                LeftMatchmakingCause cause = reader.readLeftMatchmakingCause();

                onLeftMatchmakingReceived(cause);
            }
            break;

        case MATCH_FOUND:
            {
                double timeToAcceptMatch = reader.readDuration();

                onMatchFoundReceived(timeToAcceptMatch);
            }
            break;

        case MATCH_CANCELED:
            {
                onMatchCanceledReceived();
            }
            break;

        case PLAYER_ACCEPTED_MATCH:
            {
                onPlayerAcceptedMatchReceived();
            }
            break;

        case JOINED_LOBBY:
            {
                TeamInfo teamInfo         = reader.readTeamInfo();
                double   timeToSelectHero = reader.readDuration();

                onJoinedLobbyReceived(teamInfo, timeToSelectHero);
            }
            break;

        case LOBBY_DIED:
            {
                LobbyCauseOfDeath causeOfDeath = reader.readLobbyCauseOfDeath();

                onLobbyDiedReceived(causeOfDeath);
            }
            break;

        case PLAYER_SELECTED_HERO:
            {
                int playerIndex = reader.readPlayerIndex();

                Hero hero = reader.readHero();

                onPlayerSelectedHeroReceived(playerIndex, hero);
            }
            break;

        case CHAT_MESSAGE:
            {
                int    playerIndex = reader.readPlayerIndex();
                String chatMessage = reader.readTerminalString();

                onChatMessageReceived(playerIndex, chatMessage);
            }
            break;

        case MATCH_PLAYED:
            {
                MatchSummary matchSummary = reader.readMatchSummary();
                int          newRank      = reader.readIntegerRank();

                onMatchPlayedReceived(matchSummary, newRank);
            }
            break;
        }

        if (!reader.isEmpty())
        {
            throw new RuntimeException(
                "did not fully consume received message"
                );
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Asynchronously sends a login request to the server.
     *
     * @param username the account's username
     * @param password the account's password
     *
     * @throws NullPointerException if username or password are null
     * @throws IllegalArgumentException if username or password are invalid
     */
    public void sendLogin(String username, String password)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.LOGIN);
        writer.writeUsername(username);
        writer.writePassword(password);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a sign up request to the server.
     *
     * @param username the account's username
     * @param password the account's password
     *
     * @throws NullPointerException if username or password are null
     * @throws IllegalArgumentException if username or password are invalid
     */
    public void sendSignUp(String username, String password)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.SIGN_UP);
        writer.writeUsername(username);
        writer.writePassword(password);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a logout request to the server.
     */
    public void sendLogout()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.LOGOUT);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a request to join matchmaking to the server.
     */
    public void sendJoinMatchmaking()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.JOIN_MATCHMAKING);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a request to leave matchmaking to the server.
     */
    public void sendLeaveMatchmaking()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.LEAVE_MATCHMAKING);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a request to accept a match to the server.
     */
    public void sendAcceptMatch()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.ACCEPT_MATCH);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a request to decline a match to the server.
     */
    public void sendDeclineMatch()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.DECLINE_MATCH);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a request to select a hero to the server.
     *
     * @param hero the hero to be selected
     *
     * @throws NullPointerException if hero is null
     */
    public void sendSelectHero(Hero hero)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.SELECT_HERO);
        writer.writeHero(hero);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a request to send a chat message to the server.
     *
     * @param chatMessage the message to be sent
     *
     * @throws NullPointerException if chatMessage is null
     * @throws IllegalArgumentException if chatMessage is an invalid chat
     *         message
     */
    public void sendChatMessage(String chatMessage)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.CHAT_MESSAGE);
        writer.writeChatMessage(chatMessage);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a request to leave a lobby to the server.
     */
    public void sendLeaveLobby()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeClientToServerMsgId(ClientToServerMsgId.LEAVE_LOBBY);

        sendMessage(writer.toString());
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Called upon receiving a notification from the server stating that a
     * previous login (or sign up) attempt was successful.
     *
     * @param accountInfo information about the account
     * @param serverStats server statistics
     */
    protected abstract void onLoginSucceededReceived(
        AccountInfo accountInfo, ServerStats serverStats
        );

    /**
     * Called upon receiving a notification from the server stating that a
     * previous login attempt failed.
     *
     * @param error why the login attempt failed
     */
    protected abstract void onLoginFailedReceived(LoginError error);

    /**
     * Called upon receiving a notification from the server stating that a
     * previous sign up attempt failed.
     *
     * @param error why the sign up attempt failed
     */
    protected abstract void onSignUpFailedReceived(SignUpError error);

    /**
     * Called upon receiving a notification from the server stating that a
     * previous logout attempt was successful.
     */
    protected abstract void onLogoutSucceededReceived();

    /**
     * Called upon receiving server statistics.
     *
     * @param serverStats server statistics
     */
    protected abstract void onServerStatsReceived(ServerStats serverStats);

    /**
     * Called upon receiving a notification from the server stating that
     * matchmaking was joined.
     */
    protected abstract void onJoinedMatchmakingReceived();

    /**
     * Called upon receiving a notification from the server stating that
     * matchmaking was left.
     *
     * @param cause why matchmaking was left
     */
    protected abstract void onLeftMatchmakingReceived(
        LeftMatchmakingCause cause
        );

    /**
     * Called upon receiving a notification from the server stating that a match
     * was found.
     *
     * @param timeToAcceptMatch how much time the user has to accept the match,
     *        in seconds
     */
    protected abstract void onMatchFoundReceived(double timeToAcceptMatch);

    /**
     * Called upon receiving a notification from the server stating that a match
     * was canceled.
     */
    protected abstract void onMatchCanceledReceived();

    /**
     * Called upon receiving a notification from the server stating that some
     * player accepted a match.
     */
    protected abstract void onPlayerAcceptedMatchReceived();

    /**
     * Called upon receiving a notification from the server stating that a lobby
     * was joined.
     *
     * @param teamInfo information about the team
     * @param timeToSelectHero how much time the user has to select a champion,
     *        in seconds
     */
    protected abstract void onJoinedLobbyReceived(
        TeamInfo teamInfo, double timeToSelectHero
        );

    /**
     * Called upon receiving a notification from the server stating that the
     * lobby has died.
     *
     * @param causeOfDeath why the lobby died
     */
    protected abstract void onLobbyDiedReceived(LobbyCauseOfDeath causeOfDeath);

    /**
     * Called upon receiving a notification from the server stating that a
     * player selected a hero.
     *
     * @param playerIndex the index of the player in the team
     * @param hero the hero that the player selected
     */
    protected abstract void onPlayerSelectedHeroReceived(
        int playerIndex, Hero hero
        );

    /**
     * Called upon receiving a chat message from the server.
     *
     * @param playerIndex the index of the player in the team who wrote the
     *        message
     * @param chatMessage the message
     */
    protected abstract void onChatMessageReceived(
        int playerIndex, String chatMessage
        );

    /**
     * Called upon receiving a notification from the server stating that a
     * match was played.
     *
     * @param matchSummary summary of the match
     * @param newRank the player's new rank after the match
     */
    protected abstract void onMatchPlayedReceived(
        MatchSummary matchSummary, int newRank
        );
}

/* -------------------------------------------------------------------------- */
