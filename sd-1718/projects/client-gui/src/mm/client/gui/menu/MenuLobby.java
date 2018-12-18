/* -------------------------------------------------------------------------- */

package mm.client.gui.menu;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import mm.client.common.net.NetClient;
import mm.client.gui.base.Menu;
import mm.client.gui.base.Window;
import mm.client.gui.control.ControlChatBox;
import mm.client.gui.control.ControlLobbyPlayer;
import mm.client.gui.util.Util;
import mm.common.data.ConcreteHero;
import mm.common.data.Hero;
import mm.common.data.LobbyCauseOfDeath;
import mm.common.data.MatchSummary;
import mm.common.data.RandomHero;
import mm.common.data.TeamInfo;
import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

public class MenuLobby extends Menu
{
    private final NetClient netClient;

    private final ControlChatBox chatBox;
    private final List< ControlLobbyPlayer > playerControls;
    private final List< Button > heroButtons;

    @FXML private StackPane stackPane;

    @FXML private HBox hboxPlayers;

    @FXML private GridPane gridPaneHeroes;
    @FXML private Button buttonRandomHero;

    @FXML private Label labelCountdown;

    /* ---------------------------------------------------------------------- */

    public MenuLobby(
        Window window, NetClient netClient,
        double timeToSelectHero
        )
    {
        super(window);

        this.netClient = netClient;

        // setup GUI

        chatBox = new ControlChatBox(msg -> {
            if (Validation.isValidChatMessage(msg))
                netClient.sendChatMessage(msg);
        });

        playerControls = new ArrayList<>();
        heroButtons    = new ArrayList<>();

        Platform.runLater(() -> {
            stackPane.getChildren().add(chatBox);

            TeamInfo teamInfo = netClient.getTeamInfo();

            for (int i = 0; i < 5; ++i)
            {
                ControlLobbyPlayer control = new ControlLobbyPlayer(
                    teamInfo.getTeam(),
                    teamInfo.getPlayers().get(i)
                    );

                playerControls.add(control);

                hboxPlayers.getChildren().add(control);
            }

            for (int j = 0; j < 3; ++j)
            {
                for (int i = 0; i < 10; ++i)
                {
                    Hero hero = new ConcreteHero(i + 10 * j);

                    Button button = new Button(String.valueOf(hero.getLetter()));
                    button.setMinSize(30, 30);
                    button.setPrefSize(30, 30);
                    button.setMaxSize(30, 30);
                    button.setOnAction(e -> netClient.selectHero(hero));

                    heroButtons.add(button);

                    gridPaneHeroes.add(button, i, j);
                }
            }
        });

        Util.setupCountdownLabel(labelCountdown, timeToSelectHero);

        // setup networking callbacks

        netClient.clearCallbacks();

        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnLobbyDied(this::onLobbyDied);
        netClient.setOnPlayerSelectedHero(this::onPlayerSelectedHero);
        netClient.setOnChatMessageReceived(this::onChatMessageReceived);

        netClient.setOnMatchPlayed(this::onMatchPlayed);
    }

    /* ---------------------------------------------------------------------- */

    // networking callbacks

    private void onDisconnect(Throwable cause)
    {
        if (cause != null)
            getWindow().openError(cause);

        new MenuConnect(getWindow(), netClient);
    }

    private void onLobbyDied(LobbyCauseOfDeath causeOfDeath)
    {
        getWindow().openInfo(causeOfDeath.toString());

        new MenuLoggedIn(getWindow(), netClient);
    }

    private void onPlayerSelectedHero(
        int playerIndex, Hero oldHero, Hero newHero
        )
    {
        Platform.runLater(() -> {
            playerControls.get(playerIndex).setHero(newHero);

            if (newHero instanceof ConcreteHero)
            {
                int i = ((ConcreteHero)newHero).getIndex();
                heroButtons.get(i).setDisable(true);
            }

            if (oldHero instanceof ConcreteHero)
            {
                int i = ((ConcreteHero)oldHero).getIndex();
                heroButtons.get(i).setDisable(false);
            }
        });
    }

    private void onChatMessageReceived(int playerIndex, String chatMessage)
    {
        String username =
            netClient
            .getTeamInfo()
            .getPlayers()
            .get(playerIndex)
            .getUsername();

        chatBox.addMessage(username, chatMessage);
    }

    private void onMatchPlayed(
        MatchSummary matchSummary, int oldRank, int newRank
        )
    {
        new MenuMatchSummary(
            getWindow(), netClient,
            matchSummary, oldRank, newRank
            );
    }

    // GUI callbacks

    @FXML private void buttonRandomHeroOnAction()
    {
        netClient.selectHero(new RandomHero());
    }

    @FXML private void buttonLeaveLobbyOnAction()
    {
        netClient.leaveLobby();
    }
}

/* -------------------------------------------------------------------------- */
