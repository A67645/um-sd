/* -------------------------------------------------------------------------- */

package mm.client.gui.base;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/* -------------------------------------------------------------------------- */

public class Control
{
    public static <T> Node load(
        String pathFxml,
        T controller, boolean controllerIsRoot
        )
    {
        URL urlFxml = Objects.requireNonNull(
            controller.getClass().getResource(pathFxml)
            );

        FXMLLoader loader = new FXMLLoader();

        loader.setRoot(controllerIsRoot ? controller : null);
        loader.setController(controller);
        loader.setLocation(urlFxml);

        try
        {
            return loader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}

/* -------------------------------------------------------------------------- */
