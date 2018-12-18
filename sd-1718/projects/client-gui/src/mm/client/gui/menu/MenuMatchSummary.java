/* -------------------------------------------------------------------------- */

package mm.client.gui.menu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import mm.client.common.net.NetClient;
import mm.client.gui.base.Menu;
import mm.client.gui.base.Window;
import mm.client.gui.control.ControlMatchSummaryPlayer;
import mm.client.gui.util.Config;
import mm.common.data.MatchSummary;
import mm.common.data.Team;

/* -------------------------------------------------------------------------- */

public class MenuMatchSummary extends Menu
{
    private final NetClient netClient;

    @FXML private GridPane gridPaneTeams;

    @FXML private Label labelWinner;
    @FXML private Label labelRankChange;
    @FXML private Label labelNewRank;

    /* ---------------------------------------------------------------------- */

    public MenuMatchSummary(
        Window window, NetClient netClient,
        MatchSummary matchSummary, int oldRank, int newRank
        )
    {
        super(window);

        this.netClient = netClient;

        // setup GUI

        Platform.runLater(() -> {
            Team winner = matchSummary.getWinner();

            for (Team team : Team.values())
            {
                for (int i = 0; i < 5; ++i)
                {
                    MatchSummary.Player player =
                        matchSummary.getPlayers(team).get(i);

                    gridPaneTeams.add(
                        new ControlMatchSummaryPlayer(team, player),
                        team.getIndex(), i
                        );
                }
            }

            if (winner == Team.BLUE)
                labelWinner.setText("Blue wins.");
            else
                labelWinner.setText("Red wins.");

            labelWinner.setTextFill(Config.getTeamColor(winner));

            if (oldRank == newRank)
            {
                labelRankChange.setText("");
                labelNewRank.setText("");
            }
            else
            {
                if (newRank > oldRank)
                    labelRankChange.setText("You ranked up!");
                else
                    labelRankChange.setText("You ranked down.");

                labelNewRank.setText("Your new rank: " + newRank);
            }
        });

        // setup networking callbacks

        netClient.clearCallbacks();

        netClient.setOnDisconnect(this::onDisconnect);
    }

    /* ---------------------------------------------------------------------- */

    // networking callbacks

    private void onDisconnect(Throwable cause)
    {
        if (cause != null)
            getWindow().openError(cause);

        new MenuConnect(getWindow(), netClient);
    }

    // GUI callbacks

    @FXML private void buttonCloseOnAction()
    {
        new MenuLoggedIn(getWindow(), netClient);
    }
}

/* -------------------------------------------------------------------------- */
