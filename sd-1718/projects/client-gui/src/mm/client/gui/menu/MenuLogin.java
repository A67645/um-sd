/* -------------------------------------------------------------------------- */

package mm.client.gui.menu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import mm.client.common.net.NetClient;
import mm.client.gui.base.Menu;
import mm.client.gui.base.Window;
import mm.common.data.LoginError;
import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

public class MenuLogin extends Menu
{
    private final NetClient netClient;

    @FXML private Hyperlink hyperlinkSignUp;

    @FXML private TextField textFieldUsername;
    @FXML private PasswordField passwordFieldPassword;

    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button buttonLogin;
    @FXML private Button buttonDisconnect;

    /* ---------------------------------------------------------------------- */

    public MenuLogin(Window window, NetClient netClient)
    {
        super(window);

        this.netClient = netClient;

        // setup networking callbacks

        netClient.clearCallbacks();

        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnLoginSucceded(this::onLoginSucceded);
        netClient.setOnLoginFailed(this::onLoginFailed);
    }

    /* ---------------------------------------------------------------------- */

    private void setBlocked(boolean block)
    {
        progressIndicator.setVisible(block);

        hyperlinkSignUp.setDisable(block);

        textFieldUsername.setDisable(block);
        passwordFieldPassword.setDisable(block);

        buttonLogin.setDisable(block);
    }

    // networking callbacks

    private void onDisconnect(Throwable cause)
    {
        if (cause != null)
            getWindow().openError(cause);

        new MenuConnect(getWindow(), netClient);
    }

    private void onLoginSucceded()
    {
        new MenuLoggedIn(getWindow(), netClient);
    }

    private void onLoginFailed(LoginError error)
    {
        getWindow().openError(error.toString());

        setBlocked(false);
    }

    // GUI callbacks

    @FXML private void hyperlinkSignUpOnAction()
    {
        new MenuSignUp(getWindow(), netClient);
    }

    @FXML private void buttonLoginOnAction()
    {
        String username = textFieldUsername.getText();
        String password = passwordFieldPassword.getText();

        if (!Validation.isValidUsername(username))
        {
            getWindow().openError("Invalid username.");
            return;
        }

        if (!Validation.isValidPassword(password))
        {
            getWindow().openError("Invalid password.");
            return;
        }

        setBlocked(true);

        try
        {
            netClient.login(username, password);
        }
        catch (Throwable t)
        {
            getWindow().openError(t);

            setBlocked(false);

            return;
        }

    }

    @FXML private void buttonDisconnectOnAction()
    {
        setBlocked(true);

        netClient.disconnect();
    }
}

/* -------------------------------------------------------------------------- */
