/* -------------------------------------------------------------------------- */

package mm.common.net;

/* -------------------------------------------------------------------------- */

/**
 * This enumeration is used to identify the types of messages sent from the
 * server to a client.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public enum ServerToClientMsgId
{
    /**
     * Identifies a message indicating that a previous login attempt was
     * successful.
     */
    LOGIN_SUCCEEDED,

    /**
     * Identifies a message indicating that a previous login attempt failed.
     */
    LOGIN_FAILED,

    /**
     * Identifies a message indicating that a previous sign up attempt failed.
     */
    SIGN_UP_FAILED,

    /**
     * Identifies a message indicating that a previous logout attempt was
     * successful.
     */
    LOGOUT_SUCCEEDED,

    /**
     * Identifies a message whose payload contains several server-related
     * statistics.
     */
    SERVER_STATS,

    /**
     * Identifies a message indicating that a previous attempt to join
     * matchmaking was successful.
     */
    JOINED_MATCHMAKING,

    /**
     * Identifies a message indicating that matchmaking was left.
     */
    LEFT_MATCHMAKING,

    /**
     * Identifies a message indicating that a match was found.
     */
    MATCH_FOUND,

    /**
     * Identifies a message indicating that a match was canceled.
     */
    MATCH_CANCELED,

    /**
     * Identifies a message indicating that a player accepted a match.
     */
    PLAYER_ACCEPTED_MATCH,

    /**
     * Identifies a message indicating that a lobby was joined.
     */
    JOINED_LOBBY,

    /**
     * Identifies a message indicating that a lobby died.
     */
    LOBBY_DIED,

    /**
     * Identifies a message indicating that a player selected a hero.
     */
    PLAYER_SELECTED_HERO,

    /**
     * Identifies a message containing a chat message sent by some player.
     */
    CHAT_MESSAGE,

    /**
     * Identifies a message indicating that a match was played.
     */
    MATCH_PLAYED
}

/* -------------------------------------------------------------------------- */
