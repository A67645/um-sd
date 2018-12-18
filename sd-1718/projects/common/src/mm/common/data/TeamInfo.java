/* -------------------------------------------------------------------------- */

package mm.common.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import mm.common.Config;
import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

/**
 * Holds information about one team in a lobby.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class TeamInfo
{
    /**
     * Holds information about a player in a team in a lobby.
     *
     * @author Alberto Faria
     * @author Fábio Fontes
     */
    public static class Player
    {
        private final String username;
        private final int rank;

        public Player(String username, int rank)
        {
            this.username = Validation.validateUsername(username);
            this.rank     = Validation.validateIntegerRank(rank);
        }

        public String getUsername()
        {
            return username;
        }

        public int getRank()
        {
            return rank;
        }
    }

    /* ---------------------------------------------------------------------- */

    private final Team team;
    private final List< Player > players;

    /* ---------------------------------------------------------------------- */

    /**
     * Creates a new instance of TeamInfo.
     *
     * @param team the team
     * @param players the players of the team
     */
    public TeamInfo(Team team, List< Player > players)
    {
        Objects.requireNonNull(team);

        if (players.size() != Config.TEAM_SIZE)
        {
            throw new IllegalArgumentException(String.format(
                "teams must have %d players",
                Config.TEAM_SIZE
                ));
        }

        // ---

        this.team    = team;
        this.players = Collections.unmodifiableList(new ArrayList<>(players));
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the team.
     *
     * @return the team
     */
    public Team getTeam()
    {
        return team;
    }

    /**
     * Returns the players in the team.
     *
     * @return the players in the team
     */
    public List< Player > getPlayers()
    {
        return players;
    }
}

/* -------------------------------------------------------------------------- */
