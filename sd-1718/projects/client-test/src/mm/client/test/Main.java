/* -------------------------------------------------------------------------- */

package mm.client.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* -------------------------------------------------------------------------- */

public class Main
{
    private static final Object STDERR_MONITOR = new Object();

    private static void printStackTrace(Throwable t)
    {
        synchronized (STDERR_MONITOR)
        {
            t.printStackTrace();
        }
    }

    /* ---------------------------------------------------------------------- */

    public static void main(String[] args) throws IOException
    {
        // parse arguments

        Arguments arguments = Arguments.parse(args);

        // create clients

        List< AutoClient > clients = new ArrayList<>();

        for (int i = 0; i < arguments.getNumClients(); ++i)
        {
            clients.add(new AutoClient(
                i,
                arguments.getServerEndpoint(),
                arguments.getJoinMatchmakingDelay(),
                arguments.getAcceptMatchDelay(),
                arguments.getSelectHeroDelay(),
                Main::printStackTrace
                ));
        }

        try
        {
            // start clients

            System.out.println("Starting " + clients.size() + " clients...");

            for (AutoClient c : clients)
                c.start();

            System.out.println("Clients started. Press Ctrl+C to terminate...");
        }
        catch (Throwable t)
        {
            printStackTrace(t);

            // stop clients

            for (AutoClient c : clients)
                c.stop();
        }

        // wait for clients to stop

        for (AutoClient c : clients)
            c.waitUntilStopped();
    }
}

/* -------------------------------------------------------------------------- */
