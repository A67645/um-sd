/* -------------------------------------------------------------------------- */

package mm.common.data;

/* -------------------------------------------------------------------------- */

/**
 * Represents the selection of a random hero.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class RandomHero implements Hero
{
    @Override
    public char getLetter()
    {
        return '?';
    }

    @Override
    public String toString()
    {
        return "random";
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && getClass() == obj.getClass();
    }
}

/* -------------------------------------------------------------------------- */
