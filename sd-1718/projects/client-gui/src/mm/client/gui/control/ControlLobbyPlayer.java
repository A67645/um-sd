/* -------------------------------------------------------------------------- */

package mm.client.gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mm.client.gui.base.Control;
import mm.client.gui.util.Config;
import mm.common.data.ConcreteHero;
import mm.common.data.Hero;
import mm.common.data.RandomHero;
import mm.common.data.Team;
import mm.common.data.TeamInfo;

/* -------------------------------------------------------------------------- */

public class ControlLobbyPlayer extends VBox
{
    @FXML private HBox  hboxHero;
    @FXML private Label labelHero;

    @FXML private Label labelUsername;
    @FXML private Label labelRank;

    /* ---------------------------------------------------------------------- */

    public ControlLobbyPlayer(Team team, TeamInfo.Player player)
    {
        Control.load("fxml/ControlLobbyPlayer.fxml", this, true);

        hboxHero.setBorder(new Border(new BorderStroke(
            Config.getTeamColor(team),
            BorderStrokeStyle.SOLID,
            new CornerRadii(4),
            new BorderWidths(2)
            )));

        labelHero.setText("");

        labelUsername.setText(player.getUsername());
        labelRank.setText("Rank: " + player.getRank());
    }

    /* ---------------------------------------------------------------------- */

    public void setHero(Hero hero)
    {
        if (hero == null)
        {
            labelHero.setText("");
        }
        else if (hero instanceof ConcreteHero)
        {
            char letter = ((ConcreteHero)hero).getLetter();

            labelHero.setText(String.valueOf(letter));
        }
        else if (hero instanceof RandomHero)
        {
            labelHero.setText("?");
        }
        else
        {
            throw new IllegalArgumentException("unknown hero type");
        }
    }
}

/* -------------------------------------------------------------------------- */
