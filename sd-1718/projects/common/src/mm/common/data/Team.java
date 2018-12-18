/* -------------------------------------------------------------------------- */

package mm.common.data;

/* -------------------------------------------------------------------------- */

/**
 * Enumerates the two teams that exist in the game.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public enum Team
{
    BLUE(0),
    RED(1);

    /* ---------------------------------------------------------------------- */

    private final int index;

    private Team(int index)
    {
        this.index = index;
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the index of the team.
     *
     * @return the index of the team
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Returns the team with the specified index.
     *
     * @param index the index of the team
     * @return the team with the specified index
     *
     * @throws IllegalArgumentException if index is not the index of any team
     */
    public static Team fromIndex(int index)
    {
        switch (index)
        {
        case 0: return Team.BLUE;
        case 1: return Team.RED;

        default: throw new IllegalArgumentException(
            "invalid team index: " + index
            );
        }
    }

    /**
     * Return the team opposite to the one specified.
     *
     * @param team the team whose opposite team to return
     * @return the team opposite to the specified team
     */
    public static Team other(Team team)
    {
        switch (team)
        {
        case BLUE: return Team.RED;
        case RED : return Team.BLUE;

        default: throw new IllegalArgumentException("invalid team: " + team);
        }
    }
}

/* -------------------------------------------------------------------------- */
