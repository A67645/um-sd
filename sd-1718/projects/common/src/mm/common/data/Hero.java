/* -------------------------------------------------------------------------- */

package mm.common.data;

import java.util.Objects;

/* -------------------------------------------------------------------------- */

/**
 * Represents the selection of a hero.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public interface Hero
{
    /**
     * Create a Hero from a string.
     *
     * @param str the string to be converted
     * @return the hero
     *
     * @throws NullPointerException if str is null
     * @throws IllegalArgumentException if str is invalid
     */
    public static Hero fromString(String str)
    {
        Objects.requireNonNull(str);

        if (str.equals("random"))
            return new RandomHero();

        try
        {
            return new ConcreteHero(Integer.valueOf(str));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(
                String.format("invalid hero string format: %s", str)
                );
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns the letter that identifies this hero.
     *
     * @return the letter that identifies this hero
     */
    char getLetter();
}

/* -------------------------------------------------------------------------- */
