/* -------------------------------------------------------------------------- */

package mm.client.gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import mm.client.gui.base.Control;
import mm.common.data.MatchSummary;
import mm.common.data.Team;

/* -------------------------------------------------------------------------- */

public class ControlMatchSummaryPlayer extends HBox
{
    @FXML private Label labelUsername;
    @FXML private Label labelSummary;
    @FXML private Label labelHero;

    /* ---------------------------------------------------------------------- */

    public ControlMatchSummaryPlayer(Team team, MatchSummary.Player player)
    {
        String pathFxml =
            (team == Team.BLUE) ?
            "fxml/ControlMatchSummaryPlayerBlue.fxml" :
            "fxml/ControlMatchSummaryPlayerRed.fxml";

        Control.load(pathFxml, this, true);

        labelUsername.setText(player.getUsername());

        labelSummary.setText(String.format(
            "%s K | %s A | %s D",
            player.getKills(), player.getAssists(), player.getDeaths()
            ));

        labelHero.setText(String.valueOf(player.getHero().getLetter()));
    }
}

/* -------------------------------------------------------------------------- */
