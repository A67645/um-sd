/* ------------------------------------------------------------------------- */

package mm.common.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mm.common.Config;
import mm.common.data.AccountInfo;
import mm.common.data.ConcreteHero;
import mm.common.data.Hero;
import mm.common.data.LeftMatchmakingCause;
import mm.common.data.LobbyCauseOfDeath;
import mm.common.data.LoginError;
import mm.common.data.MatchSummary;
import mm.common.data.ServerStats;
import mm.common.data.SignUpError;
import mm.common.data.Team;
import mm.common.data.TeamInfo;
import mm.common.util.Validation;

/* ------------------------------------------------------------------------- */

/**
 * Helper class for decoding messages.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class MsgReader
{
    private final StringBuilder remainingMessage;

    /* --------------------------------------------------------------------- */

    /**
     * Creates a new instance of MsgReader for decoding the specified message.
     *
     * @param message the message to be decoded
     *
     * @throws NullPointerException if message is null
     */
    public MsgReader(String message)
    {
        remainingMessage = new StringBuilder(Objects.requireNonNull(message));
    }

    /* --------------------------------------------------------------------- */

    /**
     * Checks whether the original message has been fully consumed.
     *
     * @return true if the original message has been fully consumed
     */
    public boolean isEmpty()
    {
        return remainingMessage.length() == 0;
    }

    /* --------------------------------------------------------------------- */

    public String readString()
    {
        if (remainingMessage.length() == 0)
            throw new IllegalStateException("message exhausted");

        int nextColon = remainingMessage.indexOf(":");

        if (nextColon == -1)
        {
            String str = remainingMessage.toString();

            remainingMessage.setLength(0);

            return str;
        }
        else
        {
            String str = remainingMessage.substring(0, nextColon);

            remainingMessage.delete(0, nextColon + 1);

            return str;
        }
    }

    public String readTerminalString()
    {
        if (remainingMessage.length() == 0)
            throw new IllegalStateException("message exhausted");

        String str = remainingMessage.toString();

        remainingMessage.setLength(0);

        return str;
    }

    public String readUsername()
    {
        return Validation.validateUsername(readString());
    }

    public String readPassword()
    {
        return Validation.validatePassword(readTerminalString());
    }

    public String readChatMessage()
    {
        return Validation.validateChatMessage(readTerminalString());
    }

    public double readDuration()
    {
        return Validation.validateDuration(Double.valueOf(readString()));
    }

    public int readIntegerRank()
    {
        return Validation.validateIntegerRank(Integer.valueOf(readString()));
    }

    public int readPlayerIndex()
    {
        return Validation.validatePlayerIndex(Integer.valueOf(readString()));
    }

    public ClientToServerMsgId readClientToServerMsgId()
    {
        return ClientToServerMsgId.valueOf(readString());
    }

    public ServerToClientMsgId readServerToClientMsgId()
    {
        return ServerToClientMsgId.valueOf(readString());
    }

    public LoginError readLoginError()
    {
        return LoginError.valueOf(readString());
    }

    public SignUpError readSignUpError()
    {
        return SignUpError.valueOf(readString());
    }

    public LeftMatchmakingCause readLeftMatchmakingCause()
    {
        return LeftMatchmakingCause.valueOf(readString());
    }

    public LobbyCauseOfDeath readLobbyCauseOfDeath()
    {
        return LobbyCauseOfDeath.valueOf(readString());
    }

    public Hero readHero()
    {
        return Hero.fromString(readString());
    }

    public ServerStats readServerStats()
    {
        return new ServerStats(
            Integer.valueOf(readString()),
            Integer.valueOf(readString()),
            Integer.valueOf(readString()),
            Integer.valueOf(readString())
            );
    }

    public AccountInfo readAccountInfo()
    {
        return new AccountInfo(
            readString(),
            Integer.valueOf(readString()),
            Integer.valueOf(readString()),
            Integer.valueOf(readString())
            );
    }

    public TeamInfo readTeamInfo()
    {
        Team team = Team.valueOf(readString());

        List< TeamInfo.Player > players = new ArrayList<>();

        for (int i = 0; i < Config.TEAM_SIZE; ++i)
        {
            players.add(new TeamInfo.Player(
                readString(),
                Integer.valueOf(readString())
                ));
        }

        return new TeamInfo(team, players);
    }

    public MatchSummary readMatchSummary()
    {
        Team winner = Team.valueOf(readString());

        List< List< MatchSummary.Player > > players = new ArrayList<>();

        for (int i = 0; i < 2; ++i)
        {
            players.add(new ArrayList<>());

            for (int j = 0; j < Config.TEAM_SIZE; ++j)
            {
                players.get(i).add(new MatchSummary.Player(
                    readString(),
                    new ConcreteHero(Integer.valueOf(readString())),
                    Integer.valueOf(readString()),
                    Integer.valueOf(readString()),
                    Integer.valueOf(readString())
                    ));
            }
        }

        return new MatchSummary(winner, players.get(0), players.get(1));
    }
}

/* ------------------------------------------------------------------------- */
