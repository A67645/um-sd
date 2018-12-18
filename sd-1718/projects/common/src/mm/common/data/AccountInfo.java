/* -------------------------------------------------------------------------- */

package mm.common.data;

import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

/**
 * Holds information about a player account.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class AccountInfo
{
    private final String username;

    private final int numWonMatches;
    private final int numLostMatches;

    private final int rank;

    /* ---------------------------------------------------------------------- */

    /**
     * Creates a new instance of AccountInfo.
     *
     * @param username the player's username
     * @param numWonMatches the player's number of won matches
     * @param numLostMatches the player's number of lost matches
     * @param rank the player's rank
     *
     * @throws NullPointerException if username is null
     * @throws IllegalArgumentException if any of the arguments is invalid
     */
    public AccountInfo(
        String username,
        int numWonMatches, int numLostMatches,
        int rank
        )
    {
        this.username = Validation.validateUsername(username);

        this.numWonMatches  = Validation.validateCount(numWonMatches);
        this.numLostMatches = Validation.validateCount(numLostMatches);

        this.rank = Validation.validateIntegerRank(rank);
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the player's username.
     *
     * @return the player's username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Returns the player's number of won matches.
     *
     * @return the player's number of won matches
     */
    public int getNumWonMatches()
    {
        return numWonMatches;
    }

    /**
     * Returns the player's number of lost matches.
     *
     * @return the player's number of lost matches
     */
    public int getNumLostMatches()
    {
        return numLostMatches;
    }

    /**
     * Returns the player's rank.
     *
     * @return the player's rank
     */
    public int getRank()
    {
        return rank;
    }
}

/* -------------------------------------------------------------------------- */
