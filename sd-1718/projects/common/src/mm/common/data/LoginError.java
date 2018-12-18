/* -------------------------------------------------------------------------- */

package mm.common.data;

/* -------------------------------------------------------------------------- */

/**
 * Enumerates possible login errors.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public enum LoginError
{
    /**
     * The username is invalid.
     */
    INVALID_USERNAME,

    /**
     * The password is invalid.
     */
    INVALID_PASSWORD,

    /**
     * The username doesn't exist.
     */
    USERNAME_DOESNT_EXIST,

    /**
     * The password is incorrect.
     */
    WRONG_PASSWORD,

    /**
     * The account is already in use.
     */
    ALREADY_LOGGED_IN
}

/* -------------------------------------------------------------------------- */
