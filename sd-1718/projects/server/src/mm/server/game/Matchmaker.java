/* -------------------------------------------------------------------------- */

package mm.server.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mm.common.Config;
import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

/**
 * This class implements the matchmaking algorithm.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class Matchmaker
{
    private static final int NUM_BUCKETS = Config.MAX_RANK - Config.MIN_RANK;

    private static List< Integer > getAppropriateBucketIndices(int rank)
    {
        if (rank == Config.MIN_RANK)
        {
            return Arrays.asList(0);
        }
        else if (rank == Config.MAX_RANK)
        {
            return Arrays.asList(NUM_BUCKETS - 1);
        }
        else
        {
            int i = rank - Config.MIN_RANK;

            return Arrays.asList(i - 1, i);
        }
    }

    /* ---------------------------------------------------------------------- */

    private final List< Set< String > > buckets;

    private int playerCount;

    /* ---------------------------------------------------------------------- */

    public Matchmaker()
    {
        buckets = new ArrayList<>();

        for (int i = 0; i < NUM_BUCKETS; ++i)
            buckets.add(new HashSet<>());

        this.playerCount = 0;
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the number of players currently in matchmaking.
     *
     * @return the number of players currently in matchmaking
     */
    public synchronized int getPlayerCount()
    {
        return playerCount;
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Adds a player to matchmaking. If a lobby could be formed, returns the
     * usernames of the players in that lobby.
     *
     * @param username the player's username
     * @param rank the player's rank
     * @return the usernames of the players in that lobby, if a lobby could be
     *         formed; otherwise, null
     *
     * @throws NullPointerException if username is null
     * @throws IllegalArgumentException if any of the arguments is invalid
     * @throws IllegalStateException if the player is already in matchmaking
     */
    public synchronized Set< String > addPlayer(String username, int rank)
    {
        Validation.validateUsername(username);
        Validation.validateIntegerRank(rank);

        // get appropriate buckets

        List< Integer > appropriateBuckets = getAppropriateBucketIndices(rank);

        // check if player isn't already in matchmaking
        for (int i : appropriateBuckets)
        {
            if (buckets.get(i).contains(username))
            {
                throw new IllegalStateException(
                    "the player is already in matchmaking"
                    );
            }
        }

        // increment player count

        ++playerCount;

        // for all appropriate buckets
        for (int i : appropriateBuckets)
        {
            Set< String > bucket = buckets.get(i);

            // add player to this bucket

            bucket.add(username);

            // if this bucket if now full

            if (bucket.size() == Config.LOBBY_SIZE)
            {
                // remove every player in this bucket from adjacent buckets

                if (i - 1 >= 0)
                    buckets.get(i - 1).removeAll(bucket);

                if (i + 1 < NUM_BUCKETS)
                    buckets.get(i + 1).removeAll(bucket);

                // copy bucket

                Set< String > usernames = new HashSet<>(bucket);

                // clear this bucket

                bucket.clear();

                // update player count

                playerCount -= Config.LOBBY_SIZE;

                // return bucket copy

                return usernames;
            }
        }

        // no bucket was filled

        return null;
    }

    /**
     * Removes a player from matchmaking.
     *
     * @param username the player's username
     * @param rank the player's rank
     *
     * @throws NullPointerException if username is null
     * @throws IllegalArgumentException if any of the arguments is invalid
     * @throws IllegalStateException if the player is not in matchmaking
     */
    public synchronized void removePlayer(String username, int rank)
    {
        Validation.validateUsername(username);
        Validation.validateIntegerRank(rank);

        // remove player from appropriate buckets

        boolean removedPlayers = false;

        for (int i : getAppropriateBucketIndices(rank))
            removedPlayers |= buckets.get(i).remove(username);

        // check if player was indeed in matchmaking

        if (!removedPlayers)
            throw new IllegalStateException("the player is not in matchmaking");

        // decrement player count

        --playerCount;
    }
}

/* -------------------------------------------------------------------------- */
