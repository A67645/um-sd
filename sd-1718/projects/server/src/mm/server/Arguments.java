/* -------------------------------------------------------------------------- */

package mm.server;

import java.nio.file.Path;
import java.nio.file.Paths;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/* -------------------------------------------------------------------------- */

public class Arguments
{
    private static final ArgumentParser PARSER;

    static
    {
        PARSER =
            ArgumentParsers
            .newFor("server")
            .build()
            .defaultHelp(true);

        PARSER
            .addArgument("port")
            .type(Integer.class)
            .help("The port for the server to listen on.");

        PARSER
            .addArgument("-a")
            .metavar("accounts_file")
            .help(
                "Path to a player accounts file. If not specified, player"
                + " accounts are not saved."
                );

        PARSER
            .addArgument("-m")
            .metavar("time_to_accept_match")
            .type(Double.class)
            .setDefault(10.)
            .help(
                "Amount of time players have to accept a found match, in"
                + " seconds."
                );

        PARSER
            .addArgument("-s")
            .metavar("time_to_select_hero")
            .type(Double.class)
            .setDefault(30.)
            .help("Amount of time players have to select a hero, in seconds.");

        PARSER
            .addArgument("-t")
            .metavar("server_stats_interval")
            .type(Double.class)
            .setDefault(5.)
            .help(
                "Interval between resending server statistics to clients, in"
                + " seconds"
                );
    }

    /* ---------------------------------------------------------------------- */

    public static Arguments parse(String[] args)
    {
        try
        {
            Namespace ns = PARSER.parseArgs(args);

            String accountsFile = ns.getString("a");

            return new Arguments(
                ns.getInt("port"),
                (accountsFile == null) ? null : Paths.get(accountsFile),
                ns.getDouble("m"),
                ns.getDouble("s"),
                ns.getDouble("t")
                );
        }
        catch (ArgumentParserException e)
        {
            PARSER.handleError(e);
            System.exit(1);
        }
        catch (Throwable t)
        {
            System.err.println(t.getMessage());
            System.exit(1);
        }

        // should never be reached
        return null;
    }

    /* ---------------------------------------------------------------------- */

    private final int port;

    private final Path playerAccountsFile;

    private final double timeToAcceptMatch;
    private final double timeToSelectHero;

    private final double serverStatsInterval;

    /* ---------------------------------------------------------------------- */

    public Arguments(
        int port,
        Path playerAccountsFile,
        double timeToAcceptMatch, double timeToSelectHero,
        double serverStatsInterval
        )
    {
        if (timeToAcceptMatch < 1)
        {
            throw new IllegalAccessError(
                "Time to accept matches must not be less than 1 second."
                );
        }

        if (timeToSelectHero < 1)
        {
            throw new IllegalAccessError(
                "Time to select heroes must not be less than 1 second."
                );
        }

        if (serverStatsInterval < 1)
        {
            throw new IllegalAccessError(
                "Server statistics interval must not be less than 1 second."
                );
        }

        this.port = port;

        this.playerAccountsFile = playerAccountsFile;

        this.timeToAcceptMatch = timeToAcceptMatch;
        this.timeToSelectHero  = timeToSelectHero;

        this.serverStatsInterval = serverStatsInterval;
    }

    /* ---------------------------------------------------------------------- */

    public int getPort()
    {
        return port;
    }

    public Path getPlayerAccountsFile()
    {
        return playerAccountsFile;
    }

    public double getTimeToAcceptMatch()
    {
        return timeToAcceptMatch;
    }

    public double getTimeToSelectHero()
    {
        return timeToSelectHero;
    }

    public double getServerStatsInterval()
    {
        return serverStatsInterval;
    }
}

/* -------------------------------------------------------------------------- */
