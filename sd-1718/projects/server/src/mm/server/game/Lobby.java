/* -------------------------------------------------------------------------- */

package mm.server.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mm.common.Config;
import mm.common.data.ConcreteHero;
import mm.common.data.Hero;
import mm.common.data.MatchSummary;
import mm.common.data.Team;
import mm.common.data.TeamInfo;
import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

public class Lobby
{
    /**
     * Holds information about a player in a lobby.
     *
     * @author Alberto Faria
     * @author Fábio Fontes
     */
    public static class Player
    {
        private final String username;
        private final int rank;

        private Hero selectedHero;

        private TeamInfo.Player toTeamInfoPlayer()
        {
            return new TeamInfo.Player(username, rank);
        }

        private Hero getSelectedHero()
        {
            return selectedHero;
        }

        private void setSelectedHero(Hero hero)
        {
            this.selectedHero = hero;
        }

        public Player(String username, int rank)
        {
            this.username = Validation.validateUsername(username);
            this.rank     = Validation.validateIntegerRank(rank);

            this.selectedHero = null;
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

    private static void createBalancedTeams(
        Collection< Player > players,
        List< Player > teamBlue, List< Player > teamRed
        )
    {
        Stream< Player > sortedPlayers =
            players
            .stream()
            .sorted((p1, p2) -> Integer.compare(p1.getRank(), p2.getRank()));

        boolean pTeamBlue = false;

        for(Player player : (Iterable< Player >)sortedPlayers::iterator)
        {
            if (pTeamBlue)
                teamBlue.add(player);
            else
                teamRed.add(player);

            pTeamBlue = !pTeamBlue;
        }
    }

    /* ---------------------------------------------------------------------- */

    private final Map< Team, List< Player > > teamPlayers;

    private final Set< Integer > selectedChampionIndices;

    private final Timer timer;
    private final long timerPeriod;
    private final AtomicBoolean timerCanceled;
    private final Consumer< Lobby > onFailureToSelectHero;

    /* ---------------------------------------------------------------------- */

    public Lobby(
        Collection< Player > players,
        double timeToAcceptMatch,
        Consumer< Lobby > onFailureToSelectHero
        )
    {
        List< Player > teamBluePlayers = new ArrayList<>();
        List< Player > teamRedPlayers  = new ArrayList<>();

        createBalancedTeams(players, teamBluePlayers, teamRedPlayers);

        teamPlayers = new HashMap<>();
        teamPlayers.put(Team.BLUE, teamBluePlayers);
        teamPlayers.put(Team.RED , teamRedPlayers );

        selectedChampionIndices = new HashSet<>();

        // ---

        this.timer = new Timer();
        this.timerPeriod = (long)(timeToAcceptMatch * 1000.);
        this.timerCanceled = new AtomicBoolean(false);
        this.onFailureToSelectHero = onFailureToSelectHero;
    }

    /* ---------------------------------------------------------------------- */

    public Set< String > getAllPlayerUsernames()
    {
        return
            Arrays
            .stream(Team.values())
            .flatMap(t -> getTeamPlayerUsernames(t).stream())
            .collect(Collectors.toSet());
    }

    public List< String > getTeamPlayerUsernames(Team team)
    {
        return
            teamPlayers
            .get(team)
            .stream()
            .map(Player::getUsername)
            .collect(Collectors.toList());
    }

    public TeamInfo getTeamInfo(Team team)
    {
        List< TeamInfo.Player > players =
            teamPlayers
            .get(team)
            .stream()
            .map(Player::toTeamInfoPlayer)
            .collect(Collectors.toList());

        return new TeamInfo(team, players);
    }

    // Returns the index of the player in his team.
    public int getTeamPlayerIndex(String username)
    {
        int i = getTeamPlayerUsernames(Team.BLUE).indexOf(username);

        if (i == -1)
            i = getTeamPlayerUsernames(Team.RED).indexOf(username);

        if (i == -1)
            throw new IllegalArgumentException("player not in lobby");

        return i;
    }

    public boolean timerWasCanceled()
    {
        return timerCanceled.get();
    }

    public boolean didEveryPlayerSelectAHero()
    {
        for (Team team : Team.values())
            for (int i = 0; i < Config.TEAM_SIZE; ++i)
                if (teamPlayers.get(team).get(i).getSelectedHero() == null)
                    return false;

        return true;
    }

    public MatchSummary playMatch()
    {
        Random r = ThreadLocalRandom.current();

        Function< Player, MatchSummary.Player > getPlayer = p -> {

            ConcreteHero hero;

            if (p.getSelectedHero() instanceof ConcreteHero)
                hero = (ConcreteHero)p.getSelectedHero();
            else
                hero = new ConcreteHero(r.nextInt(Config.NUM_HEROES));

            return new MatchSummary.Player(
                p.getUsername(),
                hero,
                r.nextInt(31),
                r.nextInt(11),
                r.nextInt(31)
                );
        };

        Team winner = Team.fromIndex(r.nextInt(2));

        List< MatchSummary.Player > bluePlayers =
            teamPlayers
            .get(Team.BLUE)
            .stream()
            .map(getPlayer)
            .collect(Collectors.toList());

        List< MatchSummary.Player > redPlayers =
            teamPlayers
            .get(Team.RED)
            .stream()
            .map(getPlayer)
            .collect(Collectors.toList());

        return new MatchSummary(winner, bluePlayers, redPlayers);
    }

    // Returns true if hero was actually selected.
    public boolean trySelectHero(Team team, int teamPlayerIndex, Hero hero)
    {
        Validation.validatePlayerIndex(teamPlayerIndex);
        Objects.requireNonNull(hero);

        Hero oldHero = teamPlayers.get(team).get(
            teamPlayerIndex
            ).getSelectedHero();

        if (oldHero instanceof ConcreteHero)
        {
            int i = ((ConcreteHero)oldHero).getIndex();

            selectedChampionIndices.remove(i);
        }

        if (hero instanceof ConcreteHero)
        {
            int i = ((ConcreteHero)hero).getIndex();

            if (selectedChampionIndices.contains(i))
                return false;

            selectedChampionIndices.add(i);
        }

        teamPlayers.get(team).get(teamPlayerIndex).setSelectedHero(hero);

        return true;
    }

    public void startTimer()
    {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run()
            {
                onFailureToSelectHero.accept(Lobby.this);
            }
        };

        timer.schedule(timerTask, timerPeriod);
    }

    public void cancelTimer()
    {
        timer.cancel();
        timerCanceled.set(true);
    }
}

/* -------------------------------------------------------------------------- */
