/* -------------------------------------------------------------------------- */

package mm.common.data;

import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

/**
 * Holds several server statistics.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class ServerStats
{
    private final int numRegisteredPlayers;
    private final int numPlayersLoggedIn;
    private final int numPlayersInMatchmaking;
    private final int numLobbies;

    /* ---------------------------------------------------------------------- */

    /**
     * Creates a new instance of ServerStats.
     *
     * @param numRegisteredPlayers the number of registered players
     * @param numPlayersLoggedIn the number of players currently logged in
     * @param numPlayersInMatchmaking the number of players currently in
     *        matchmaking
     * @param numLobbies the number of lobbies that currently exist
     *
     * @throws IllegalArgumentException if any of the arguments is invalid
     */
    public ServerStats(
        int numRegisteredPlayers,
        int numPlayersLoggedIn,
        int numPlayersInMatchmaking,
        int numLobbies
        )
    {
        this.numRegisteredPlayers    = Validation.validateCount(numRegisteredPlayers);
        this.numPlayersLoggedIn      = Validation.validateCount(numPlayersLoggedIn);
        this.numPlayersInMatchmaking = Validation.validateCount(numPlayersInMatchmaking);
        this.numLobbies              = Validation.validateCount(numLobbies);
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the number of registered players.
     *
     * @return the number of registered players
     */
    public int getNumRegisteredPlayers()
    {
        return numRegisteredPlayers;
    }

    /**
     * Returns the number of players currently logged in.
     *
     * @return the number of players currently logged in
     */
    public int getNumPlayersLoggedIn()
    {
        return numPlayersLoggedIn;
    }

    /**
     * Returns the number of players currently in matchmaking.
     *
     * @return the number of players currently in matchmaking
     */
    public int getNumPlayersInMatchmaking()
    {
        return numPlayersInMatchmaking;
    }

    /**
     * Returns the number of lobbies that currently exist.
     *
     * @return the number of lobbies that currently exist
     */
    public int getNumLobbies()
    {
        return numLobbies;
    }
}

/* -------------------------------------------------------------------------- */
