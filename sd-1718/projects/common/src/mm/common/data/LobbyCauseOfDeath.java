/* -------------------------------------------------------------------------- */

package mm.common.data;

/* -------------------------------------------------------------------------- */

/**
 * Enumerates the causes that may lead to a lobby dying.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public enum LobbyCauseOfDeath
{
    /**
     * One of the players left.
     */
    PLAYER_LEFT,

    /**
     * One of the players didn't select a hero in time.
     */
    PLAYER_DIDNT_SELECT_HERO
}

/* -------------------------------------------------------------------------- */
