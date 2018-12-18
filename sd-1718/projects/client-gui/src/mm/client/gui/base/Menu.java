/* -------------------------------------------------------------------------- */

package mm.client.gui.base;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/* -------------------------------------------------------------------------- */

public abstract class Menu
{
    private static final FXMLLoader loader = new FXMLLoader();

    private final Window window;

    /* ---------------------------------------------------------------------- */

    private void setWindowContent(Parent root)
    {
        if (Platform.isFxApplicationThread())
        {
            Stage stage = window.getStage();

            if (stage.getScene() == null)
            {
                stage.setScene(new Scene(root));
                stage.show();
            }
            else
            {
                stage.getScene().setRoot(root);
            }
        }
        else
        {
            Platform.runLater(() -> setWindowContent(root));
        }
    }

    /* ---------------------------------------------------------------------- */

    protected Menu(Window window)
    {
        this.window = window;

        // load fxml

        String pathFxml = "fxml/" + getClass().getSimpleName() + ".fxml";
        URL urlFxml = Objects.requireNonNull(getClass().getResource(pathFxml));

        Parent root;

        synchronized (loader)
        {
            loader.setRoot(null);
            loader.setController(this);
            loader.setLocation(urlFxml);

            try
            {
                root = loader.load();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        // set window content

        setWindowContent(root);
    }

    /* ---------------------------------------------------------------------- */

    public Window getWindow()
    {
        return window;
    }
}

/* -------------------------------------------------------------------------- */
