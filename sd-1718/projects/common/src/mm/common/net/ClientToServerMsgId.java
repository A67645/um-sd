/* -------------------------------------------------------------------------- */

package mm.common.net;

/* -------------------------------------------------------------------------- */

/**
 * This enumeration is used to identify the types of messages sent from a client
 * to the server.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public enum ClientToServerMsgId
{
    /**
     * Identifies a message requesting to login.
     */
    LOGIN,

    /**
     * Identifies a message requesting to sign up.
     */
    SIGN_UP,

    /**
     * Identifies a message requesting to logout.
     */
    LOGOUT,

    /**
     * Identifies a message requesting to join matchmaking.
     */
    JOIN_MATCHMAKING,

    /**
     * Identifies a message requesting to leave matchmaking.
     */
    LEAVE_MATCHMAKING,

    /**
     * Identifies a message accepting a match.
     */
    ACCEPT_MATCH,

    /**
     * Identifies a message declining a match.
     */
    DECLINE_MATCH,

    /**
     * Identifies a message requesting to select a hero.
     */
    SELECT_HERO,

    /**
     * Identifies a message consisting of a chat message.
     */
    CHAT_MESSAGE,

    /**
     * Identifies a message requesting to leave a lobby.
     */
    LEAVE_LOBBY
}

/* -------------------------------------------------------------------------- */
