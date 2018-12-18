/* ------------------------------------------------------------------------- */

package mm.server;

import java.io.IOException;

import mm.server.net.NetServer;

/* ------------------------------------------------------------------------- */

public class Main
{
    private static void onServerStop(Throwable cause)
    {
        if (cause != null)
            cause.printStackTrace();

        System.out.println("Server stopped.");

        System.exit(cause == null ? 0 : 1);
    }

    public static void main(String[] args) throws IOException
    {
        NetServer server = null;

        try
        {
            // parse arguments

            Arguments arguments = Arguments.parse(args);

            // start server

            System.out.println(
                "Starting server on port " + arguments.getPort() + "..."
                );

            server = new NetServer(arguments, Main::onServerStop);

            // wait for user input

            System.out.println(
                "Server started. Press ENTER to stop the server..."
                );

            System.in.read();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            // stop server

            System.out.println("Stopping server...");

            if (server != null)
                server.stop();
        }
    }
}

/* -------------------------------------------------------------------------- */
