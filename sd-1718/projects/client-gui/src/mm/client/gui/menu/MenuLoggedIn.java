/* -------------------------------------------------------------------------- */

package mm.client.gui.menu;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mm.client.common.net.NetClient;
import mm.client.gui.base.Menu;
import mm.client.gui.base.Window;
import mm.client.gui.util.Countdown;
import mm.client.gui.util.Util;
import mm.common.Config;
import mm.common.data.AccountInfo;
import mm.common.data.LeftMatchmakingCause;
import mm.common.data.ServerStats;

/* -------------------------------------------------------------------------- */

public class MenuLoggedIn extends Menu
{
    private final NetClient netClient;

    private final List< CheckBox > matchFoundCheckBoxes;
    private int matchFoundNumAccepted;
    private Countdown matchFoundCountdown;

    @FXML private Label labelUsername;
    @FXML private Label labelWonMatches;
    @FXML private Label labelLostMatches;
    @FXML private Label labelRank;

    @FXML private Label labelRegisteredPlayers;
    @FXML private Label labelPlayersLoggedIn;
    @FXML private Label labelPlayersInMatchmaking;
    @FXML private Label labelLobbies;

    @FXML private VBox vboxJoinMatchmaking;
    @FXML private Button buttonJoinMatchmaking;

    @FXML private VBox vboxInMatchmaking;

    @FXML private VBox vboxMatchFound;
    @FXML private HBox hboxMatchFoundCheckBoxes;
    @FXML private Button buttonAcceptMatch;
    @FXML private Label labelMatchFoundCountdown;

    @FXML private Button buttonLogout;

    /* ---------------------------------------------------------------------- */

    public MenuLoggedIn(Window window, NetClient netClient)
    {
        super(window);

        this.netClient = netClient;

        // setup GUI

        AccountInfo accountInfo = netClient.getAccountInfo();

        matchFoundCheckBoxes = new ArrayList<>();

        for (int i = 0; i < Config.LOBBY_SIZE; ++i)
            matchFoundCheckBoxes.add(new CheckBox());

        Platform.runLater(() -> {
            labelUsername.setText(accountInfo.getUsername());

            labelWonMatches.setText(
                Integer.toString(accountInfo.getNumWonMatches())
                );

            labelLostMatches.setText(
                Integer.toString(accountInfo.getNumLostMatches())
                );

            labelRank.setText(Integer.toString(accountInfo.getRank()));

            hboxMatchFoundCheckBoxes.getChildren().addAll(matchFoundCheckBoxes);
        });

        onServerStatsReceived();

        // setup networking callbacks

        netClient.clearCallbacks();

        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnLogoutSucceded(this::onLogoutSucceeded);

        netClient.setOnServerStatsReceived(this::onServerStatsReceived);

        netClient.setOnJoinedMatchmaking(this::onJoinedMatchmaking);
        netClient.setOnLeftMatchmaking(this::onLeftMatchmaking);

        netClient.setOnMatchFound(this::onMatchFound);
        netClient.setOnMatchCanceled(this::onMatchCanceled);
        netClient.setOnPlayerAcceptedMatch(this::onPlayerAcceptedMatch);

        netClient.setOnJoinedLobby(this::onJoinedLobby);
    }

    /* ---------------------------------------------------------------------- */

    private void switchToJoinMatchmaking()
    {
        vboxJoinMatchmaking.setVisible(true);
        vboxInMatchmaking.setVisible(false);
        vboxMatchFound.setVisible(false);

        buttonJoinMatchmaking.setDisable(false);

        buttonLogout.setDisable(false);
    }

    private void switchToInMatchmaking()
    {
        vboxJoinMatchmaking.setVisible(false);
        vboxInMatchmaking.setVisible(true);
        vboxMatchFound.setVisible(false);

        buttonLogout.setDisable(true);
    }

    private void switchToMatchFound(double timeToAcceptMatch)
    {
        matchFoundCheckBoxes.forEach(c -> c.setSelected(false));
        matchFoundNumAccepted = 0;

        if (matchFoundCountdown != null)
            matchFoundCountdown.stop();

        matchFoundCountdown = Util.setupCountdownLabel(
            labelMatchFoundCountdown,
            timeToAcceptMatch
            );

        buttonAcceptMatch.setDisable(false);
        labelMatchFoundCountdown.setVisible(true);

        vboxJoinMatchmaking.setVisible(false);
        vboxInMatchmaking.setVisible(false);
        vboxMatchFound.setVisible(true);

        buttonLogout.setDisable(true);
    }

    // networking callbacks

    private void onDisconnect(Throwable cause)
    {
        if (cause != null)
            getWindow().openError(cause);

        new MenuConnect(getWindow(), netClient);
    }

    private void onLogoutSucceeded()
    {
        new MenuLogin(getWindow(), netClient);
    }

    private void onServerStatsReceived()
    {
        ServerStats serverStats = netClient.getServerStats();

        Platform.runLater(() -> {
            labelRegisteredPlayers.setText(
                Integer.toString(serverStats.getNumRegisteredPlayers())
                );

            labelPlayersLoggedIn.setText(
                Integer.toString(serverStats.getNumPlayersLoggedIn())
                );

            labelPlayersInMatchmaking.setText(
                Integer.toString(serverStats.getNumPlayersInMatchmaking())
                );

            labelLobbies.setText(
                Integer.toString(serverStats.getNumLobbies())
                );
        });
    }

    private void onJoinedMatchmaking()
    {
        switchToInMatchmaking();
    }

    private void onLeftMatchmaking(LeftMatchmakingCause cause)
    {
        if (cause != LeftMatchmakingCause.VOLUNTARY)
            getWindow().openInfo(cause.toString());

        switchToJoinMatchmaking();
    }

    private void onMatchFound(double timeToAcceptMatch)
    {
        switchToMatchFound(timeToAcceptMatch);
    }

    private void onMatchCanceled()
    {
        switchToInMatchmaking();
    }

    private void onPlayerAcceptedMatch()
    {
        matchFoundCheckBoxes.get(matchFoundNumAccepted++).setSelected(true);
    }

    private void onJoinedLobby(double timeToSelectHero)
    {
        new MenuLobby(getWindow(), netClient, timeToSelectHero);
    }

    // GUI callbacks

    @FXML private void buttonJoinMatchmakingOnAction()
    {
        buttonJoinMatchmaking.setDisable(true);

        netClient.joinMatchmaking();
    }

    @FXML private void buttonLeaveMatchmakingOnAction()
    {
        netClient.leaveMatchmaking();
    }

    @FXML private void buttonAcceptMatchOnAction()
    {
        buttonAcceptMatch.setDisable(true);
        labelMatchFoundCountdown.setVisible(false);

        netClient.acceptMatch();
    }

    @FXML private void buttonDeclineMatchOnAction()
    {
        netClient.declineMatch();
    }

    @FXML private void buttonLogoutOnAction()
    {
        buttonJoinMatchmaking.setDisable(true);
        buttonLogout.setDisable(true);

        netClient.logout();
    }
}

/* -------------------------------------------------------------------------- */
