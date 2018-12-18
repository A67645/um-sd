/* -------------------------------------------------------------------------- */

package mm.server.error;

import java.util.Objects;

import mm.common.data.LoginError;

/* -------------------------------------------------------------------------- */

@SuppressWarnings("serial")
public class LoginErrorException extends RuntimeException
{
    private final LoginError error;

    /* ---------------------------------------------------------------------- */

    public LoginErrorException(LoginError error)
    {
        this.error = Objects.requireNonNull(error);
    }

    /* ---------------------------------------------------------------------- */

    public LoginError getError()
    {
        return error;
    }
}

/* -------------------------------------------------------------------------- */
