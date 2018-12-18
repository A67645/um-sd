/* -------------------------------------------------------------------------- */

package mm.client.test;

import java.net.InetSocketAddress;
import java.util.Objects;

import mm.common.util.Util;
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
            .newFor("client-test")
            .build()
            .defaultHelp(true)
            .description("Spawns several automated test clients.");

        PARSER
            .addArgument("server_endpoint")
            .help("Endpoint of the server, in the format host:port.");

        PARSER
            .addArgument("num_clients")
            .type(Integer.class)
            .help("Number of clients to spawn.");

        PARSER
            .addArgument("-j")
            .metavar("min-max")
            .setDefault("1-5")
            .help("Time delay range before joining matchmaking, in seconds.");

        PARSER
            .addArgument("-m")
            .metavar("min-max")
            .setDefault("1-5")
            .help("Time delay range before accepting a match, in seconds.");

        PARSER
            .addArgument("-s")
            .metavar("min-max")
            .setDefault("1-5")
            .help(
                "Time interval range between (re)selecting a hero, in"
                + " seconds. If 0-0, select the \"random\" hero immediately and"
                + " never reselect."
                );
    }

    /* ---------------------------------------------------------------------- */

    public static Arguments parse(String[] args)
    {
        try
        {
            Namespace ns = PARSER.parseArgs(args);

            return new Arguments(
                Util.parseEndpointUnresolved(ns.getString("server_endpoint")),
                ns.getInt("num_clients"),
                DurationRange.parse(ns.getString("j")),
                DurationRange.parse(ns.getString("m")),
                DurationRange.parse(ns.getString("s"))
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

    private final InetSocketAddress serverEndpoint;

    private final int numClients;

    private final DurationRange joinMatchmakingDelay;
    private final DurationRange acceptMatchDelay;
    private final DurationRange selectHeroDelay;

    /* ---------------------------------------------------------------------- */

    public Arguments(
        InetSocketAddress serverEndpoint,
        int numClients,
        DurationRange joinMatchmakingDelay,
        DurationRange acceptMatchDelay,
        DurationRange selectHeroDelay
        )
    {
        if (numClients < 1)
        {
            throw new IllegalArgumentException(
                "Number of clients must be positive."
                );
        }

        this.serverEndpoint = Objects.requireNonNull(serverEndpoint);

        this.numClients = numClients;

        this.joinMatchmakingDelay = Objects.requireNonNull(joinMatchmakingDelay);
        this.acceptMatchDelay     = Objects.requireNonNull(acceptMatchDelay);
        this.selectHeroDelay      = Objects.requireNonNull(selectHeroDelay);
    }

    /* ---------------------------------------------------------------------- */

    public InetSocketAddress getServerEndpoint()
    {
        return serverEndpoint;
    }

    public int getNumClients()
    {
        return numClients;
    }

    public DurationRange getJoinMatchmakingDelay()
    {
        return joinMatchmakingDelay;
    }

    public DurationRange getAcceptMatchDelay()
    {
        return acceptMatchDelay;
    }

    public DurationRange getSelectHeroDelay()
    {
        return selectHeroDelay;
    }
}

/* -------------------------------------------------------------------------- */
