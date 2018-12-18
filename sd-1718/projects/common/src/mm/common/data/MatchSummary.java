/* ------------------------------------------------------------------------- */

package mm.common.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mm.common.util.Validation;

/* ------------------------------------------------------------------------- */

/**
 * Holds information about a finished match.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class MatchSummary
{
    /**
     * Holds information about a player in a finished match.
     *
     * @author Alberto Faria
     * @author Fábio Fontes
     */
    public static class Player
    {
        private final String username;

        private final ConcreteHero hero;

        private final int kills;
        private final int assists;
        private final int deaths;

        public Player(
            String username,
            ConcreteHero hero,
            int kills, int assists, int deaths
            )
        {
            this.username = Validation.validateUsername(username);

            this.hero = Objects.requireNonNull(hero);

            this.kills   = Validation.validateCount(kills);
            this.assists = Validation.validateCount(assists);
            this.deaths  = Validation.validateCount(deaths);
        }

        public String getUsername()
        {
            return username;
        }

        public ConcreteHero getHero()
        {
            return hero;
        }

        public int getKills()
        {
            return kills;
        }

        public int getAssists()
        {
            return assists;
        }

        public int getDeaths()
        {
            return deaths;
        }
    }

    /* --------------------------------------------------------------------- */

    private final Team winner;

    private final Map< Team, List< Player > > players;

    /* --------------------------------------------------------------------- */

    /**
     * Creates a new instance of MatchSummary.
     *
     * @param winner the team that won
     * @param bluePlayers the players that make up the blue team
     * @param redPlayers the players that make up the red team
     *
     * @throws NullPointerException f any of the arguments is null
     * @throws IllegalArgumentException if any of the arguments is invalid
     */
    public MatchSummary(
        Team winner,
        List< Player > bluePlayers, List< Player > redPlayers
        )
    {
        this.winner = winner;

        players = new HashMap<>();

        players.put(
            Team.BLUE,
            Collections.unmodifiableList(new ArrayList<>(bluePlayers))
            );

        players.put(
            Team.RED,
            Collections.unmodifiableList(new ArrayList<>(redPlayers))
            );
    }

    /* --------------------------------------------------------------------- */

    /**
     * Returns the team that won.
     *
     * @return the team that won
     */
    public Team getWinner()
    {
        return winner;
    }

    /**
     * Returns the players that make up the specified team.
     *
     * @param team the team
     * @return the players that make up the specified team
     */
    public List< Player > getPlayers(Team team)
    {
        return players.get(Objects.requireNonNull(team));
    }
}

/* ------------------------------------------------------------------------- */
