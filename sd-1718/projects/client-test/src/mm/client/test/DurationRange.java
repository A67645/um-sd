/* -------------------------------------------------------------------------- */

package mm.client.test;

import java.util.concurrent.ThreadLocalRandom;

/* -------------------------------------------------------------------------- */

public class DurationRange
{
    private final double min;
    private final double max;

    /* ---------------------------------------------------------------------- */

    public DurationRange(double min, double max)
    {
        if (min < 0 || max < 0)
        {
            throw new IllegalArgumentException(
                "Durations must be non-negative"
                );
        }

        if (min > max)
        {
            throw new IllegalArgumentException(
                "Minimum duration must not be greater than maximum duration."
                );
        }

        this.min = min;
        this.max = max;
    }

    /* ---------------------------------------------------------------------- */

    public double getMin()
    {
        return min;
    }

    public double getMax()
    {
        return max;
    }

    public double getRandUniform()
    {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public long getMinAsMilliseconds()
    {
        return (long)(min * 1000.);
    }

    public long getMaxAsMilliseconds()
    {
        return (long)(max * 1000.);
    }

    public long getRandUniformAsMilliseconds()
    {
        return (long)(getRandUniform() * 1000.);
    }

    /* ---------------------------------------------------------------------- */

    public static DurationRange parse(String str)
    {
        String[] split = str.split("-");

        if (split.length != 2)
        {
            throw new IllegalArgumentException(
                "Invalid duration range format."
                );
        }

        try
        {
            return new DurationRange(
                Double.valueOf(split[0]),
                Double.valueOf(split[1])
                );
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(
                "Invalid duration range format."
                );
        }
    }
}

/* -------------------------------------------------------------------------- */
