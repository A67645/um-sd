/* -------------------------------------------------------------------------- */

package mm.client.gui;

import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;
import mm.client.common.net.NetClient;
import mm.client.gui.base.Window;
import mm.client.gui.menu.MenuConnect;
import mm.client.gui.menu.MenuLogin;

/* -------------------------------------------------------------------------- */

public class Main extends Application
{
    private Arguments arguments;

    private NetClient netClient;

    /* ---------------------------------------------------------------------- */

    @Override
    public void init()
    {
        arguments = Arguments.parse(
            getParameters().getRaw().toArray(new String[0])
            );

        netClient = new NetClient();
    }

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            Window window = new Window(
                primaryStage,
                "Matchmaking", new Dimension2D(1100, 800), true
                );

            if (arguments.getServerEndpoint() == null)
            {
                new MenuConnect(window, netClient);
            }
            else
            {
                netClient.setOnConnectSucceded(() -> {
                    new MenuLogin(window, netClient);
                });

                netClient.setOnConnectFailed(cause -> {
                    cause.printStackTrace();
                    System.exit(1);
                });

                netClient.connect(arguments.getServerEndpoint());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop()
    {
        netClient.clearCallbacks();

        netClient.disconnect();
        netClient.waitUntilDisconnected();
    }

    /* ---------------------------------------------------------------------- */

    public static void main(String[] args)
    {
        launch(args);
    }
}

/* -------------------------------------------------------------------------- */
