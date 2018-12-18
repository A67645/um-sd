/* -------------------------------------------------------------------------- */

package mm.client.gui.menu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import mm.client.common.net.NetClient;
import mm.client.gui.base.Menu;
import mm.client.gui.base.Window;
import mm.common.util.Util;

/* -------------------------------------------------------------------------- */

public class MenuConnect extends Menu
{
    private final NetClient netClient;

    @FXML private TextField textFieldServerEndpoint;

    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button buttonConnect;

    /* ---------------------------------------------------------------------- */

    public MenuConnect(Window window, NetClient netClient)
    {
        super(window);

        this.netClient = netClient;

        // setup networking callbacks

        netClient.clearCallbacks();

        netClient.setOnConnectSucceded(this::onConnectSucceded);
        netClient.setOnConnectFailed(this::onConnectFailed);
    }

    /* ---------------------------------------------------------------------- */

    private void setBlocked(boolean block)
    {
        progressIndicator.setVisible(block);

        textFieldServerEndpoint.setDisable(block);

        buttonConnect.setDisable(block);
    }

    // networking callbacks

    private void onConnectSucceded()
    {
        new MenuLogin(getWindow(), netClient);
    }

    private void onConnectFailed(Throwable cause)
    {
        getWindow().openError(cause);

        setBlocked(false);
    }

    // GUI callbacks

    @FXML private void buttonConnectOnAction()
    {
        String endpoint = textFieldServerEndpoint.getText();

        setBlocked(true);

        try
        {
            // let the NetClient resolve the hostname asynchronously

            netClient.connect(Util.parseEndpointUnresolved(endpoint));
        }
        catch (Throwable t)
        {
            getWindow().openError(t);

            setBlocked(false);

            return;
        }

    }

    @FXML private void buttonExitOnAction()
    {
        netClient.clearCallbacks();

        getWindow().close();
    }
}

/* -------------------------------------------------------------------------- */
