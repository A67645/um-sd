/* -------------------------------------------------------------------------- */

package mm.common;

/* -------------------------------------------------------------------------- */

public class Config
{
    private Config()
    {
    }

    /* ---------------------------------------------------------------------- */

    // credentials and chat

    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{4,20}$";
    public static final String PASSWORD_PATTERN = "^[^\r\n]{6,40}$";

    public static final String CHAT_MESSAGE_PATTERN = "^[^\r\n]+$";

    // ranks

    public static final int MIN_RANK = 0;
    public static final int MAX_RANK = 9;

    public static final double INITIAL_RANK = 0;

    // lobbies

    public static final int TEAM_SIZE  = 5;
    public static final int LOBBY_SIZE = 2 * TEAM_SIZE;

    public static final int NUM_HEROES = 30;
}

/* -------------------------------------------------------------------------- */
