/* -------------------------------------------------------------------------- */

package mm.server.error;

import java.util.Objects;

import mm.common.data.SignUpError;

/* -------------------------------------------------------------------------- */

@SuppressWarnings("serial")
public class SignUpErrorException extends RuntimeException
{
    private final SignUpError error;

    /* ---------------------------------------------------------------------- */

    public SignUpErrorException(SignUpError error)
    {
        this.error = Objects.requireNonNull(error);
    }

    /* ---------------------------------------------------------------------- */

    public SignUpError getError()
    {
        return error;
    }
}

/* -------------------------------------------------------------------------- */
