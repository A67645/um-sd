/* ------------------------------------------------------------------------- */

package mm.common.net;

import mm.common.data.AccountInfo;
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
 * Helper class for encoding messages.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class MsgWriter
{
    private final StringBuilder message;

    private boolean wroteTerminalItem;

    /* --------------------------------------------------------------------- */

    /**
     * Creates a new instance of MsgWriter for encoding a new message.
     */
    public MsgWriter()
    {
        message = new StringBuilder();

        wroteTerminalItem = false;
    }

    /* --------------------------------------------------------------------- */

    /**
     * Returns the currently encoded message.
     *
     * @return the currently encoded message
     */
    @Override
    public String toString()
    {
        return message.toString();
    }

    /* --------------------------------------------------------------------- */

    public void writeString(String str)
    {
        if (wroteTerminalItem)
            throw new IllegalStateException("terminal item written");

        if (message.length() > 0)
            message.append(':');

        message.append(str);
    }

    public void writeTerminalString(String str)
    {
        writeString(str);

        wroteTerminalItem = true;
    }

    public void writeUsername(String username)
    {
        writeString(Validation.validateUsername(username));
    }

    public void writePassword(String password)
    {
        writeTerminalString(Validation.validatePassword(password));
    }

    public void writeChatMessage(String chatMessage)
    {
        writeTerminalString(Validation.validateChatMessage(chatMessage));
    }

    public void writeDuration(double duration)
    {
        writeString(Double.toString(Validation.validateDuration(duration)));
    }

    public void writeIntegerRank(int rank)
    {
        writeString(Integer.toString(Validation.validateIntegerRank(rank)));
    }

    public void writePlayerIndex(int playerIndex)
    {
        writeString(Integer.toString(Validation.validatePlayerIndex(playerIndex)));
    }

    public void writeClientToServerMsgId(ClientToServerMsgId id)
    {
        writeString(id.toString());
    }

    public void writeServerToClientMsgId(ServerToClientMsgId id)
    {
        writeString(id.toString());
    }

    public void writeLoginError(LoginError error)
    {
        writeString(error.toString());
    }

    public void writeSignUpError(SignUpError error)
    {
        writeString(error.toString());
    }

    public void writeLeftMatchmakingCause(LeftMatchmakingCause error)
    {
        writeString(error.toString());
    }

    public void writeLobbyCauseOfDeath(LobbyCauseOfDeath error)
    {
        writeString(error.toString());
    }

    public void writeHero(Hero hero)
    {
        writeString(hero.toString());
    }

    public void writeServerStats(ServerStats serverStats)
    {
        writeString(Integer.toString(serverStats.getNumRegisteredPlayers()));
        writeString(Integer.toString(serverStats.getNumPlayersLoggedIn()));
        writeString(Integer.toString(serverStats.getNumPlayersInMatchmaking()));
        writeString(Integer.toString(serverStats.getNumLobbies()));
    }

    public void writeAccountInfo(AccountInfo accountInfo)
    {
        writeString(accountInfo.getUsername());
        writeString(Integer.toString(accountInfo.getNumWonMatches()));
        writeString(Integer.toString(accountInfo.getNumLostMatches()));
        writeString(Integer.toString(accountInfo.getRank()));
    }

    public void writeTeamInfo(TeamInfo teamInfo)
    {
        writeString(teamInfo.getTeam().toString());

        for (TeamInfo.Player p : teamInfo.getPlayers())
        {
            writeString(p.getUsername());
            writeString(Integer.toString(p.getRank()));
        }
    }

    public void writeMatchSummary(MatchSummary matchSummary)
    {
        writeString(matchSummary.getWinner().toString());

        for (MatchSummary.Player p : matchSummary.getPlayers(Team.BLUE))
        {
            writeString(p.getUsername());
            writeString(Integer.toString(p.getHero().getIndex()));
            writeString(Integer.toString(p.getKills()));
            writeString(Integer.toString(p.getAssists()));
            writeString(Integer.toString(p.getDeaths()));
        }

        for (MatchSummary.Player p : matchSummary.getPlayers(Team.RED))
        {
            writeString(p.getUsername());
            writeString(Integer.toString(p.getHero().getIndex()));
            writeString(Integer.toString(p.getKills()));
            writeString(Integer.toString(p.getAssists()));
            writeString(Integer.toString(p.getDeaths()));
        }
    }
}

/* ------------------------------------------------------------------------- */
