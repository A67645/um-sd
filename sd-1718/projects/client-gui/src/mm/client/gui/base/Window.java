/* -------------------------------------------------------------------------- */

package mm.client.gui.base;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

/* -------------------------------------------------------------------------- */

public class Window
{
    private final Stage stage;

    /* ---------------------------------------------------------------------- */

    // NOTE: Must be called on the JavaFX Application Thread.
    private Alert createAlert(AlertType type, String title, String message)
    {
        Alert alert = new Alert(type);

        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(stage);

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert;
    }

    // NOTE: Does not block.
    private void openAlert(AlertType type, String title, String message)
    {
        if (Platform.isFxApplicationThread())
            createAlert(type, title, message).show();
        else
            Platform.runLater(() -> openAlert(type, title, message));
    }

    Stage getStage()
    {
        return stage;
    }

    /* ---------------------------------------------------------------------- */

    public Window(
        Stage stage,
        String title, Dimension2D dimensions, boolean resizable
        )
    {
        this.stage = stage;

        if (title != null)
            stage.setTitle(title);

        if (dimensions != null)
        {
            stage.setWidth(dimensions.getWidth());
            stage.setHeight(dimensions.getHeight());
        }

        stage.setResizable(resizable);
    }

    /* ---------------------------------------------------------------------- */

    // NOTE: Must be called on the JavaFX Application Thread.
    public void close()
    {
        stage.close();
    }

    // NOTE: Does not block.
    public void openInfo(String message)
    {
        openAlert(AlertType.INFORMATION, "Information", message);
    }

    // NOTE: Does not block.
    public void openWarning(String message)
    {
        openAlert(AlertType.WARNING, "Warning", message);
    }

    // NOTE: Does not block.
    public void openError(String message)
    {
        openAlert(AlertType.ERROR, "Error", message);
    }

    // NOTE: Does not block.
    public void openError(Throwable throwable)
    {
        openError(throwable.getMessage());
    }

    // NOTE: Blocks until the confirmation dialog is closed.
    // NOTE: Must be called on the JavaFX Application Thread.
    public boolean openConfirmation(String message)
    {
        Alert alert = createAlert(
            AlertType.CONFIRMATION, "Confirmation", message
            );

        return alert.showAndWait().get() == ButtonType.OK;
    }
}

/* -------------------------------------------------------------------------- */
