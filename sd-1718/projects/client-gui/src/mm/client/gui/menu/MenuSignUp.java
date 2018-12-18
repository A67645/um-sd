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
import mm.common.data.SignUpError;
import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

public class MenuSignUp extends Menu
{
    private final NetClient netClient;

    @FXML private Hyperlink hyperlinkLogin;

    @FXML private TextField textFieldUsername;
    @FXML private PasswordField passwordFieldPassword;
    @FXML private PasswordField passwordFieldConfirmPassword;

    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button buttonSignUp;
    @FXML private Button buttonDisconnect;

    /* ---------------------------------------------------------------------- */

    public MenuSignUp(Window window, NetClient netClient)
    {
        super(window);

        this.netClient = netClient;

        // setup networking callbacks

        netClient.clearCallbacks();

        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnLoginSucceded(this::onLoginSucceded);
        netClient.setOnSignUpFailed(this::onSignUpFailed);
    }

    /* ---------------------------------------------------------------------- */

    private void setBlocked(boolean block)
    {
        progressIndicator.setVisible(block);

        hyperlinkLogin.setDisable(block);

        textFieldUsername.setDisable(block);
        passwordFieldPassword.setDisable(block);
        passwordFieldConfirmPassword.setDisable(block);

        buttonSignUp.setDisable(block);
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

    private void onSignUpFailed(SignUpError error)
    {
        getWindow().openError(error.toString());

        setBlocked(false);
    }

    // GUI callbacks

    @FXML private void hyperlinkLoginOnAction()
    {
        new MenuLogin(getWindow(), netClient);
    }

    @FXML private void buttonSignUpOnAction()
    {
        String username  = textFieldUsername.getText();
        String password1 = passwordFieldPassword.getText();
        String password2 = passwordFieldConfirmPassword.getText();

        if (!Validation.isValidUsername(username))
        {
            getWindow().openError("Invalid username.");
            return;
        }

        if (!Validation.isValidPassword(password1))
        {
            getWindow().openError("Invalid password.");
            return;
        }

        if (!password1.equals(password2))
        {
            getWindow().openError("The passwords don't match.");
            return;
        }

        setBlocked(true);

        try
        {
            netClient.signUp(username, password1);
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
