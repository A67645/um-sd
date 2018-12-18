/* -------------------------------------------------------------------------- */

package mm.common.util;

import java.util.Objects;
import java.util.regex.Pattern;

import mm.common.Config;

/* -------------------------------------------------------------------------- */

public class Validation
{
    public static boolean isValidUsername(String username)
    {
        return Pattern.matches(
            Config.USERNAME_PATTERN,
            Objects.requireNonNull(username)
            );
    }

    public static boolean isValidPassword(String password)
    {
        return Pattern.matches(
            Config.PASSWORD_PATTERN,
            Objects.requireNonNull(password)
            );
    }

    public static boolean isValidChatMessage(String message)
    {
        return Pattern.matches(
            Config.CHAT_MESSAGE_PATTERN,
            Objects.requireNonNull(message)
            );
    }

    /* ---------------------------------------------------------------------- */

    public static String validateUsername(String username)
    {
        if (!isValidUsername(username))
            throw new IllegalArgumentException("invalid username: " + username);

        return username;
    }

    public static String validatePassword(String password)
    {
        if (!isValidPassword(password))
            throw new IllegalArgumentException("invalid password: " + password);

        return password;
    }

    public static String validateChatMessage(String chatMessage)
    {
        if (!isValidChatMessage(chatMessage))
        {
            throw new IllegalArgumentException(
                "invalid chat message: " + chatMessage
                );
        }

        return chatMessage;
    }

    public static int validateCount(int count)
    {
        if (count < 0)
            throw new IllegalArgumentException("invalid count: " + count);

        return count;
    }

    public static double validateDuration(double duration)
    {
        if (duration < 0)
            throw new IllegalArgumentException("invalid duration: " + duration);

        return duration;
    }

    public static int validateIntegerRank(int rank)
    {
        if (rank < Config.MIN_RANK || rank > Config.MAX_RANK)
            throw new IllegalArgumentException("invalid rank: " + rank);

        return rank;
    }

    public static double validateDoubleRank(double rank)
    {
        if (rank < Config.MIN_RANK || rank > Config.MAX_RANK)
            throw new IllegalArgumentException("invalid rank: " + rank);

        return rank;
    }

    public static int validatePlayerIndex(int playerIndex)
    {
        if (playerIndex < 0 || playerIndex >= Config.TEAM_SIZE)
        {
            throw new IllegalArgumentException(
                "invalid player index: " + playerIndex
                );
        }

        return playerIndex;
    }
}

/* -------------------------------------------------------------------------- */
