/* -------------------------------------------------------------------------- */

package mm.server.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import mm.common.Config;

/* -------------------------------------------------------------------------- */

/**
 * Represents a match that is waiting for the acceptance of all the players
 * that will take part in it.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class MatchFound
{
    // Map of players to booleans indicating if the player has already
    // accepted the match.
    private final Map< String, Boolean > players;

    // The number of players that have accepted the match.
    private int numPlayersAccepted;

    private final Timer timer;
    private final long timerPeriod;
    private final AtomicBoolean timerCanceled;
    private final Consumer< MatchFound > onFailureToAcceptMatch;

    /* ---------------------------------------------------------------------- */

    public MatchFound(
        Set< String > playerUsernames,
        double timeToAcceptMatch,
        Consumer< MatchFound > onFailureToAcceptMatch
        )
    {
        if (playerUsernames.size() != Config.LOBBY_SIZE)
            throw new IllegalArgumentException();

        // ---

        this.players = new HashMap<>();

        for (String username : playerUsernames)
            this.players.put(username, false);

        this.numPlayersAccepted = 0;

        // ---

        this.timer = new Timer();
        this.timerPeriod = (long)(timeToAcceptMatch * 1000.);
        this.timerCanceled = new AtomicBoolean(false);
        this.onFailureToAcceptMatch = onFailureToAcceptMatch;
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the set of the usernames of all the players that are in this
     * lobby.
     *
     * @return the set of usernames of all the players
     */
    public Set< String > getPlayerUsernames()
    {
        return Collections.unmodifiableSet(players.keySet());
    }

    public Set< String > getPlayersThatAccepted()
    {
        return
            players
            .entrySet()
            .stream()
            .filter(e -> e.getValue())
            .map(e -> e.getKey())
            .collect(Collectors.toSet());
    }

    public Set< String > getPlayersThatDidNotAccept()
    {
        return
                players
                .entrySet()
                .stream()
                .filter(e -> !e.getValue())
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    public boolean timerWasCanceled()
    {
        return timerCanceled.get();
    }

    public synchronized boolean playerAcceptedMatch(String username)
    {
        return players.get(username);
    }

    // Returns true if every player has accepted the match.
    /**
     * Registers that the specified player is accepting the match,
     * given in return a boolean indicating if all the players have already
     * accepted the match.
     *
     * @param username  the username of the player that is accepting the match
     * @return a boolean indicanting if all the players hava accepted the match
     */
    public synchronized boolean acceptMatch(String username)
    {
        if (!players.get(username))
        {
            players.put(username, true);
            ++numPlayersAccepted;
        }

        return numPlayersAccepted == Config.LOBBY_SIZE;
    }

    public void startTimer()
    {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run()
            {
                onFailureToAcceptMatch.accept(MatchFound.this);
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
