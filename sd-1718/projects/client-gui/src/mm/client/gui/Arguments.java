/* -------------------------------------------------------------------------- */

package mm.client.gui;

import java.net.InetSocketAddress;

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
            .newFor("client-gui")
            .build()
            .defaultHelp(true);

        PARSER
            .addArgument("server_endpoint")
            .nargs("?")
            .help("Endpoint of the server, in the format host:port.");
    }

    /* ---------------------------------------------------------------------- */

    public static Arguments parse(String[] args)
    {
        try
        {
            Namespace ns = PARSER.parseArgs(args);

            String endpoint = ns.getString("server_endpoint");

            return new Arguments(
                endpoint == null ? null : Util.parseEndpointUnresolved(endpoint)
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

    /* ---------------------------------------------------------------------- */

    public Arguments(InetSocketAddress serverEndpoint)
    {
        this.serverEndpoint = serverEndpoint;
    }

    /* ---------------------------------------------------------------------- */

    public InetSocketAddress getServerEndpoint()
    {
        return serverEndpoint;
    }
}

/* -------------------------------------------------------------------------- */
