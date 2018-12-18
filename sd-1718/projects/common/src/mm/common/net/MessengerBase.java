/* -------------------------------------------------------------------------- */

package mm.common.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import mm.common.util.BlockingQueue;
import mm.common.util.Util;

/* -------------------------------------------------------------------------- */

/**
 * Base class for classes encapsulating the encoding and decoding of messages
 * sent to, and received from, a client or the server.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public abstract class MessengerBase
{
    private static interface Proc
    {
        void run() throws Exception;
    }

    /* ---------------------------------------------------------------------- */

    // null if disconnected
    private Socket socket;

    private boolean userRequestedDisconnect;
    private Throwable userDisconnectionCause;
    private Throwable internalDisconnectionCause;

    private final Thread readerThread;

    private final Thread writerThread;
    private final BlockingQueue< String > writerMsgQueue;

    /* ---------------------------------------------------------------------- */

    private void runProc(Proc proc)
    {
        try
        {
            proc.run();

            throw new RuntimeException(
                "MessengerBase reader or writer thread terminated"
                + " unexpectedly"
                );
        }
        catch (Throwable t)
        {
            try
            {
                socket.close();
            }
            catch (Throwable t2)
            {
            }

            synchronized (this)
            {
                writerMsgQueue.setPopBlocks(false);

                if (internalDisconnectionCause == null)
                {
                    internalDisconnectionCause = t;
                }
                else
                {
                    socket = null;

                    Throwable disconnectionCause =
                        userRequestedDisconnect ?
                            userDisconnectionCause :
                            internalDisconnectionCause;

                    Util.runCallback(this::onDisconnect, disconnectionCause);
                }
            }
        }
    }

    private void readerProc() throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            socket.getInputStream(),
            StandardCharsets.UTF_8
            ));

        for (String msg; (msg = reader.readLine()) != null; )
            onMessageReceived(msg);

        throw new RuntimeException("Connection lost.");
    }

    private void writerProc() throws IOException
    {
        OutputStreamWriter writer = new OutputStreamWriter(
            socket.getOutputStream(),
            StandardCharsets.UTF_8
            );

        for (String msg; (msg = writerMsgQueue.pop()) != null; )
        {
            writer.write(msg);
            writer.flush();
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Creates a new MessengerBase that uses the specified socket.
     *
     * @param socket the socket holding the connection to be used
     *
     * @throws NullPointerException if socket is null
     */
    public MessengerBase(Socket socket)
    {
        this.socket = Objects.requireNonNull(socket);

        userRequestedDisconnect    = false;
        userDisconnectionCause     = null;
        internalDisconnectionCause = null;

        readerThread = new Thread(() -> runProc(this::readerProc));

        writerThread   = new Thread(() -> runProc(this::writerProc));
        writerMsgQueue = new BlockingQueue<>();
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Determines whether this MessengerBase is currently connected.
     *
     * @return true if this MessengerBase is currently connected
     */
    public synchronized boolean isConnected()
    {
        return socket != null;
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Starts this MessengerBase's internal threads.
     * <p>
     * No callback is run before this function is invoked.
     * <p>
     * This startup procedure performed by this function is not implicitly
     * performed in the constructor so as to allow the user to perform actions
     * that require this MessengerBase to exist but that should be performed
     * before any callback is run.
     * <p>
     * Note that a MessengerBase may be disconnected even if this method is
     * never invoked on that instance.
     *
     * @throws IllegalThreadStateException if this method has already been
     *         invoked on this instance of MessengerBase
     */
    public void start()
    {
        readerThread.start();
        writerThread.start();
    }

    /**
     * Asynchronously closes the connection.
     * <p>
     * If this MessengerBase is already disconnected, no action is performed.
     * <p>
     * Messages already submitted but still not transmitted may be lost.
     * <p>
     * Note that this method may return before the connection is actually
     * closed.
     * <p>
     * Calling this method is equivalent to calling {@code disconnect(null)}.
     *
     * @see #disconnect(Throwable)
     * @see #onDisconnect(Throwable)
     * @see #waitUntilDisconnected()
     */
    public void disconnect()
    {
        disconnect(null);
    }

    /**
     * Asynchronously closes the connection.
     * <p>
     * If this MessengerBase is already disconnected, no action is performed.
     * <p>
     * Messages already submitted but still not transmitted may be lost.
     * <p>
     * Note that this method may return before the connection is actually
     * closed.
     *
     * @param cause the cause to be passed to the
     *        {@link #onDisconnect(Throwable)} callback
     *
     * @see #disconnect()
     * @see #onDisconnect(Throwable)
     * @see #waitUntilDisconnected()
     */
    public synchronized void disconnect(Throwable cause)
    {
        if (socket != null)
        {
            userRequestedDisconnect = true;
            userDisconnectionCause = cause;

            writerMsgQueue.setPopBlocks(false);

            try
            {
                socket.close();
            }
            catch (Throwable t)
            {
            }
        }
    }

    /**
     * Waits for this MessengerBase to be disconnected.
     * <p>
     * If this MessengerBase is already disconnected, this method returns
     * immediately.
     * <p>
     * No callback will ever be invoked on this instance of MessengerBase
     * after this method returns. If this MessengerBase is still connected as
     * of invoking this method, the {@link #onDisconnect(Throwable)} callback is
     * invoked and finishes execution before this method returns.
     *
     * @see #disconnect()
     * @see #disconnect(Throwable)
     * @see #onDisconnect(Throwable)
     */
    public void waitUntilDisconnected()
    {
        // join readerThread

        while (true)
        {
            try
            {
                readerThread.join();
                break;
            }
            catch (InterruptedException e)
            {
            }
        }

        // join writerThread

        while (true)
        {
            try
            {
                writerThread.join();
                break;
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Asynchronously sends a message.
     * <p>
     * If this MessengerBase is disconnected, this method has no effect.
     *
     * @param message the message to be sent
     *
     * @throws NullPointerException if message is null
     * @throws IllegalArgumentException if the message contains carriage return
     *         (\r) or line feed (\n) characters
     */
    protected void sendMessage(String message)
    {
        if (message.isEmpty())
            throw new IllegalArgumentException("message may not be empty");

        if (message.contains("\r") || message.contains("\r"))
        {
            throw new IllegalArgumentException(
                "message may not contain carriage return (\r) or line feed (\n)"
                + " characters"
                );
        }

        if (isConnected())
            writerMsgQueue.push(message + '\n');
    }

    /* ---------------------------------------------------------------------- */

    /**
     * This method is called when this MessengerBase is disconnected.
     * <p>
     * Note that this method should never be called by the user -- it functions
     * as a callback method that should only ever be invoked by this
     * MessengerBase instance.
     * <p>
     * From the instant this method is called, the
     * {@link #onMessageReceived(String)} method is never invoked again on this
     * instance of MessengerBase. This method is invoked at most once on the
     * same MessengerBase instance.
     * <p>
     * If the implementation of this method throws an exception, the application
     * is aborted.
     *
     * @param cause the cause that lead to loosing the connection; null if
     *        disconnect() has been called on this MessengerBase (i.e. if
     *        disconnection was voluntary)
     *
     * @see #disconnect()
     * @see #disconnect(Throwable)
     * @see #waitUntilDisconnected()
     */
    protected abstract void onDisconnect(Throwable cause);

    /**
     * This method is called whenever a message is received.
     * <p>
     * Note that this method should never be called by the user -- it functions
     * as a callback method that should only ever be invoked by this
     * MessengerBase instance.
     * <p>
     * If the implementation of this method throws an exception, the connection
     * will be closed and {@link #onDisconnect(Throwable)} will eventually be
     * called with the aforementioned exception as its argument.
     *
     * @param message the message that was received
     */
    protected abstract void onMessageReceived(String message);
}

/* -------------------------------------------------------------------------- */
