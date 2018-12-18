/* -------------------------------------------------------------------------- */

package mm.common.util;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

/* -------------------------------------------------------------------------- */

/**
 * A simple thread-safe queue with support for blocking pop.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 *
 * @param <T> The type of the queue's elements.
 */
public class BlockingQueue<T>
{
    private final Queue<T> queue;

    private boolean popBlocks;

    /* ---------------------------------------------------------------------- */

    /**
     * Creates an empty BlockingQueue.
     */
    public BlockingQueue()
    {
        queue = new ArrayDeque<>();

        popBlocks = true;
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Checks whether this queue's {@link #pop()} method blocks.
     *
     * @return true if whether this queue's pop method blocks.
     */
    public synchronized boolean getPopBlocks()
    {
        return popBlocks;
    }

    /**
     * Sets whether this queue's {@link #pop()} method should block.
     * <p>
     * If popBlocks if false, all ongoing blocked invocations of pop() are
     * unblocked.
     *
     * @param popBlocks true if this queue's pop method should block
     */
    public synchronized void setPopBlocks(boolean popBlocks)
    {
        this.popBlocks = popBlocks;

        if (!popBlocks)
            this.notifyAll();
    }

    /**
     * Pushes an item into this queue.
     *
     * @param item the item to be pushed to this queue
     *
     * @throws NullPointerException if item is null
     */
    public synchronized void push(T item)
    {
        queue.add(Objects.requireNonNull(item));

        this.notify();
    }

    /**
     * Pops an item from this queue.
     * <p>
     * If there is at least one item in this queue, the oldest item is removed
     * and returned.
     * <p>
     * If the queue is empty and {@link #getPopBlocks()} returns true, this
     * method blocks until an item is available to be popped.
     * <p>
     * If the queue is empty and {@link #getPopBlocks()} return false, this
     * method returns null.
     *
     * @return an item or null
     */
    public synchronized T pop()
    {
        while (true)
        {
            T item = queue.poll();

            if (item != null)
                return item;

            if (popBlocks)
            {
                try
                {
                    this.wait();
                }
                catch (InterruptedException e)
                {
                }
            }
            else
            {
                return null;
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
