/* -------------------------------------------------------------------------- */

package mm.common.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/* -------------------------------------------------------------------------- */

public class Util
{
    private Util()
    {
    }

    /* ---------------------------------------------------------------------- */

    public static InetSocketAddress parseEndpoint(String endpoint)
        throws UnknownHostException
    {
        String[] split = endpoint.split(":");

        if (split.length != 2)
            throw new IllegalArgumentException("Invalid endpoint format.");

        InetAddress address = InetAddress.getByName(split[0]);

        int port;

        try
        {
            port = Integer.valueOf(split[1]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid port format.");
        }

        return new InetSocketAddress(address, port);
    }

    public static InetSocketAddress parseEndpointUnresolved(String endpoint)
    {
        String[] split = endpoint.split(":");

        if (split.length != 2)
            throw new IllegalArgumentException("Invalid endpoint format.");

        int port;

        try
        {
            port = Integer.valueOf(split[1]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid port format.");
        }

        return InetSocketAddress.createUnresolved(split[0], port);
    }

    /* ---------------------------------------------------------------------- */

    public static void runCallback(Runnable callback)
    {
        if (callback != null)
        {
            try
            {
                callback.run();
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static <T> void runCallback(Consumer<T> callback, T t)
    {
        if (callback != null)
        {
            try
            {
                callback.accept(t);
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static < T, U > void runCallback(
        BiConsumer< T, U > callback, T t, U u
        )
    {
        if (callback != null)
        {
            try
            {
                callback.accept(t, u);
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static < T, U, V > void runCallback(
        TriConsumer< T, U, V > callback, T t, U u, V v
        )
    {
        if (callback != null)
        {
            try
            {
                callback.accept(t, u, v);
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void runCallback(AtomicReference< Runnable > callback)
    {
        runCallback(callback.get());
    }

    public static <T> void runCallback(
        AtomicReference< Consumer<T> > callback, T t
        )
    {
        runCallback(callback.get(), t);
    }

    public static < T, U > void runCallback(
        AtomicReference< BiConsumer< T, U > > callback, T t, U u
        )
    {
        runCallback(callback.get(), t, u);
    }

    public static < T, U, V > void runCallback(
        AtomicReference< TriConsumer< T, U, V > > callback, T t, U u, V v
        )
    {
        runCallback(callback.get(), t, u, v);
    }
}

/* -------------------------------------------------------------------------- */
