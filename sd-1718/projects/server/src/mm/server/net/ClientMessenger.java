/* ------------------------------------------------------------------------- */

package mm.server.net;

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

/* ------------------------------------------------------------------------- */

/**
 * Encapsulates the encoding and decoding of messages sent to, and received
 * from, a client.
 * <p>
 * Note that on* method are never invoked concurrently on the same instance of
 * ClientMessenger.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public abstract class ClientMessenger extends MessengerBase
{
    /**
     * Creates a new ClientMessenger that uses the specified socket.
     *
     * @param socket the socket holding the connection to the client
     *
     * @throws NullPointerException if socket is null
     */
    public ClientMessenger(Socket socket)
    {
        super(socket);
    }

    /* --------------------------------------------------------------------- */

    @Override
    protected void onMessageReceived(String msg)
    {
        MsgReader reader = new MsgReader(msg);

        ClientToServerMsgId msgId = ClientToServerMsgId.valueOf(
            reader.readString()
            );

        switch (msgId)
        {
        case LOGIN:
            {
                String username = reader.readUsername();
                String password = reader.readPassword();

                onLoginReceived(username, password);
            }
            break;

        case SIGN_UP:
            {
                String username = reader.readUsername();
                String password = reader.readPassword();

                onSignUpReceived(username, password);
            }
            break;

        case LOGOUT:
            {
                onLogoutReceived();
            }
            break;

        case JOIN_MATCHMAKING:
            {
                onJoinMatchmakingReceived();
            }
            break;

        case LEAVE_MATCHMAKING:
            {
                onLeaveMatchmakingReceived();
            }
            break;

        case ACCEPT_MATCH:
            {
                onAcceptMatchReceived();
            }
            break;

        case DECLINE_MATCH:
            {
                onDeclineMatchReceived();
            }
            break;

        case SELECT_HERO:
            {
                Hero hero = reader.readHero();

                onSelectHeroReceived(hero);
            }
            break;

        case CHAT_MESSAGE:
            {
                String chatMessage = reader.readChatMessage();

                onChatMessageReceived(chatMessage);
            }
            break;

        case LEAVE_LOBBY:
            {
                onLeaveLobbyReceived();
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

    /* --------------------------------------------------------------------- */

    /**
     * Asynchronously sends a notification to the client stating that a previous
     * login (or sign up) attempt was successful.
     *
     * @param accountInfo information about the account
     * @param serverStats server statistics
     *
     * @throws NullPointerException if accountInfo or serverStats are null
     */
    public void sendLoginSucceeded(
        AccountInfo accountInfo, ServerStats serverStats
        )
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.LOGIN_SUCCEEDED);
        writer.writeAccountInfo(accountInfo);
        writer.writeServerStats(serverStats);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that a previous
     * login attempt failed.
     *
     * @param error why the login attempt failed
     *
     * @throws NullPointerException if error is null
     */
    public void sendLoginFailed(LoginError error)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.LOGIN_FAILED);
        writer.writeLoginError(error);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that a previous
     * sign up attempt failed.
     *
     * @param error why the sign up attempt failed
     *
     * @throws NullPointerException if error is null
     */
    public void sendSignUpFailed(SignUpError error)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.SIGN_UP_FAILED);
        writer.writeSignUpError(error);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that a previous
     * logout attempt was successful.
     */
    public void sendLogoutSucceeded()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.LOGOUT_SUCCEEDED);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends server statistics to the client.
     *
     * @param serverStats server statistics
     *
     * @throws NullPointerException if serverStats is null
     */
    public void sendServerStats(ServerStats serverStats)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.SERVER_STATS);
        writer.writeServerStats(serverStats);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that
     * matchmaking was joined.
     */
    public void sendJoinedMatchmaking()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.JOINED_MATCHMAKING);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that
     * matchmaking was left.
     *
     * @param cause why matchmaking was left
     *
     * @throws NullPointerException if cause is null
     */
    public void sendLeftMatchmaking(LeftMatchmakingCause cause)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.LEFT_MATCHMAKING);
        writer.writeLeftMatchmakingCause(cause);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that a match
     * was found.
     *
     * @param timeToAcceptMatch how much time the user has to accept the match,
     *        in seconds
     *
     * @throws IllegalArgumentException if timeToAcceptMatch is negative
     */
    public void sendMatchFound(double timeToAcceptMatch)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.MATCH_FOUND);
        writer.writeDuration(timeToAcceptMatch);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that a match
     * was canceled.
     */
    public void sendMatchCanceled()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.MATCH_CANCELED);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that some
     * player accepted a match.
     */
    public void sendPlayerAcceptedMatch()
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.PLAYER_ACCEPTED_MATCH);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that a lobby
     * was joined.
     *
     * @param teamInfo information about the team
     * @param timeToSelectHero how much time the user has to select a champion,
     *        in seconds
     *
     * @throws IllegalArgumentException if timeToSelectHero is negative
     */
    public void sendJoinedLobby(TeamInfo teamInfo, double timeToSelectHero)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.JOINED_LOBBY);
        writer.writeTeamInfo(teamInfo);
        writer.writeDuration(timeToSelectHero);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that the lobby
     * has died.
     *
     * @param causeOfDeath why the lobby died
     */
    public void sendLobbyDied(LobbyCauseOfDeath causeOfDeath)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.LOBBY_DIED);
        writer.writeLobbyCauseOfDeath(causeOfDeath);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that a player
     * selected a hero.
     *
     * @param playerIndex the index of the player in the team
     * @param hero the hero that the player selected
     *
     * @throws NullPointerException if hero is null
     * @throws IllegalArgumentException if playerIndex is invalid
     */
    public void sendPlayerSelectedHero(int playerIndex, Hero hero)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.PLAYER_SELECTED_HERO);
        writer.writePlayerIndex(playerIndex);
        writer.writeHero(hero);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a chat message to the client.
     *
     * @param playerIndex the index of the player in the team who wrote the
     *        message
     * @param chatMessage the message
     *
     * @throws NullPointerException if chatMessage is null
     * @throws IllegalArgumentException if playerIndex or chatMessage are
     *         invalid
     */
    public void sendChatMessage(int playerIndex, String chatMessage)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.CHAT_MESSAGE);
        writer.writePlayerIndex(playerIndex);
        writer.writeChatMessage(chatMessage);

        sendMessage(writer.toString());
    }

    /**
     * Asynchronously sends a notification to the client stating that a match
     * was played.
     *
     * @param matchSummary summary of the match
     * @param newRank the player's new rank after the match
     *
     * @throws NullPointerException if matchSummary is null
     * @throws IllegalArgumentException if newRank is invalid
     */
    public void sendMatchPlayed(MatchSummary matchSummary, int newRank)
    {
        MsgWriter writer = new MsgWriter();

        writer.writeServerToClientMsgId(ServerToClientMsgId.MATCH_PLAYED);
        writer.writeMatchSummary(matchSummary);
        writer.writeIntegerRank(newRank);

        sendMessage(writer.toString());
    }

    /* --------------------------------------------------------------------- */

    /**
     * Called upon receiving a login request from the client.
     *
     * @param username the account's username
     * @param password the account's password
     */
    protected abstract void onLoginReceived(String username, String password);

    /**
     * Called upon receiving a sign up request from the client.
     *
     * @param username the account's username
     * @param password the account's password
     */
    protected abstract void onSignUpReceived(String username, String password);

    /**
     * Called upon receiving a logout request from the client.
     */
    protected abstract void onLogoutReceived();

    /**
     * Called upon receiving a request to join matchmaking from the client.
     */
    protected abstract void onJoinMatchmakingReceived();

    /**
     * Called upon receiving a request to leave matchmaking from the client.
     */
    protected abstract void onLeaveMatchmakingReceived();

    /**
     * Called upon receiving a request to accept a match from the client.
     */
    protected abstract void onAcceptMatchReceived();

    /**
     * Called upon receiving a request to decline a match from the client.
     */
    protected abstract void onDeclineMatchReceived();

    /**
     * Called upon receiving a request to select a hero from the client.
     *
     * @param hero the hero to be selected
     */
    protected abstract void onSelectHeroReceived(Hero hero);

    /**
     * Called upon receiving a request to send a chat message from the client.
     *
     * @param chatMessage the message to be sent
     */
    protected abstract void onChatMessageReceived(String chatMessage);

    /**
     * Called upon receiving a request to leave a lobby from the client.
     */
    protected abstract void onLeaveLobbyReceived();
}

/* ------------------------------------------------------------------------- */
