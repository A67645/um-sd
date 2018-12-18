/* -------------------------------------------------------------------------- */

package mm.common.data;

/* -------------------------------------------------------------------------- */

/**
 * Enumerates the causes that may lead to a player leaving matchmaking.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public enum LeftMatchmakingCause
{
    /**
     * The player requested to leave matchmaking.
     */
    VOLUNTARY,

    /**
     * The player failed to accept a found match in time.
     */
    FAILED_TO_ACCEPT_MATCH,

    /**
     * The player declined a found match.
     */
    DECLINED_MATCH
}

/* -------------------------------------------------------------------------- */
