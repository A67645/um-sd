/* -------------------------------------------------------------------------- */

package mm.common.data;

import mm.common.Config;

/* -------------------------------------------------------------------------- */

/**
 * Represents the selection of an actual hero.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class ConcreteHero implements Hero
{
    private static String LETTERS = "αβγΔδεζηθικΛλμνΞξοΠπρΣστυφχψΩω";

    /* ---------------------------------------------------------------------- */

    private final int index;

    /* ---------------------------------------------------------------------- */

    /**
     * Creates an instance of ConcreteHero whose hero is identified by the
     * specified index.
     *
     * @param index index of the hero
     *
     * @throws IllegalArgumentException if index is invalid
     */
    public ConcreteHero(int index)
    {
        if (index < 0 || index >= Config.NUM_HEROES)
        {
            throw new IllegalArgumentException(
                String.format("invalid hero index: %d", index)
                );
        }

        this.index = index;
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the index of the hero.
     *
     * @return the index of the hero
     */
    public int getIndex()
    {
        return index;
    }

    @Override
    public char getLetter()
    {
        return LETTERS.charAt(index);
    }

    @Override
    public String toString()
    {
        return Integer.toString(index);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass())
            return false;

        ConcreteHero h = (ConcreteHero)obj;

        return getIndex() == h.getIndex();
    }
}

/* -------------------------------------------------------------------------- */
