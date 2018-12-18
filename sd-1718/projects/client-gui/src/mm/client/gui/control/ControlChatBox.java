/* -------------------------------------------------------------------------- */

package mm.client.gui.control;

import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mm.client.gui.base.Control;

/* -------------------------------------------------------------------------- */

public class ControlChatBox extends VBox
{
    private final Consumer< String > sendMessage;

    @FXML private ScrollPane scrollPane;
    @FXML private VBox vboxMessages;
    @FXML private TextField textFieldMessage;

    /* ---------------------------------------------------------------------- */

    public ControlChatBox(Consumer< String > sendMessage)
    {
        this.sendMessage = sendMessage;

        Control.load("fxml/ControlChatBox.fxml", this, true);

        vboxMessages.heightProperty().addListener(
            (obs, oldValue, newValue) -> scrollPane.setVvalue(1)
            );
    }

    /* ---------------------------------------------------------------------- */

    public void addMessage(String username, String message)
    {
        Platform.runLater(() -> {
            Label labelUsername = new Label(username + ":");
            labelUsername.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
            labelUsername.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
            labelUsername.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

            Label labelMessage = new Label(message);
            labelMessage.setWrapText(true);

            HBox hbox = new HBox(5, labelUsername, labelMessage);
            hbox.setAlignment(Pos.TOP_LEFT);
            hbox.setMinHeight(USE_PREF_SIZE);
            hbox.setPrefHeight(USE_COMPUTED_SIZE);
            hbox.setMaxHeight(USE_PREF_SIZE);

            vboxMessages.getChildren().add(hbox);
        });
    }

    /* ---------------------------------------------------------------------- */

    @FXML private void textFieldMessageOnAction()
    {
        sendMessage.accept(textFieldMessage.getText());

        textFieldMessage.setText("");
    }
}

/* -------------------------------------------------------------------------- */
