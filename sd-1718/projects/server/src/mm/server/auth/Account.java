/* -------------------------------------------------------------------------- */

package mm.server.auth;

import java.util.Locale;

import mm.common.Config;
import mm.common.data.AccountInfo;
import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

public class Account
{
    private final String username;
    private final String password;

    private int numWonMatches;
    private int numLostMatches;

    private double rank;

    /* ---------------------------------------------------------------------- */

    public Account(String username, String password)
    {
        this(username, password, 0, 0, Config.INITIAL_RANK);
    }

    public Account(
        String username, String password,
        int numWonMatches, int numLostMatches,
        double rank
        )
    {
        this.username = Validation.validateUsername(username);
        this.password = Validation.validatePassword(password);

        this.numWonMatches  = Validation.validateCount(numWonMatches);
        this.numLostMatches = Validation.validateCount(numLostMatches);

        this.rank = Validation.validateDoubleRank(rank);
    }

    /* ---------------------------------------------------------------------- */

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public int getNumWonMatches()
    {
        return numWonMatches;
    }

    public int getNumLostMatches()
    {
        return numLostMatches;
    }

    public double getDoubleRank()
    {
        return rank;
    }

    public int getIntegerRank()
    {
        return (int)Math.round(rank);
    }

    public AccountInfo getAccountInfo()
    {
        return new AccountInfo(
            username,
            numWonMatches,
            numLostMatches,
            getIntegerRank()
            );
    }

    /* ---------------------------------------------------------------------- */

    public void incrementNumWonMatches()
    {
        ++numWonMatches;
    }

    public void incrementNumLostMatches()
    {
        ++numLostMatches;
    }

    public void setDoubleRank(double rank)
    {
        this.rank = Validation.validateDoubleRank(rank);
    }

    /* ---------------------------------------------------------------------- */

    public static Account fromString(String str)
    {
        // split string

        String[] parts = str.split(":");

        if (parts.length != 5)
            throw new IllegalArgumentException("invalid format");

        // gather data

        String username = parts[0];
        String password = parts[1];

        int numWonMatches  = Integer.parseInt(parts[2]);
        int numLostMatches = Integer.parseInt(parts[3]);

        double rank = Double.parseDouble(parts[4]);

        // validate data and return account

        return new Account(
            username, password,
            numWonMatches, numLostMatches,
            rank
            );
    }

    @Override
    public String toString()
    {
        return String.format(
            Locale.ENGLISH,
            "%s:%s:%d:%d:%f",
            username, password,
            numWonMatches, numLostMatches,
            rank
            );
    }
}

/* -------------------------------------------------------------------------- */
