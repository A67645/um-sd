/* -------------------------------------------------------------------------- */

package mm.client.gui.util;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/* -------------------------------------------------------------------------- */

public class Util
{
    public static Countdown setupCountdownLabel(Label label, double time)
    {
        return new Countdown((int)Math.ceil(time), t -> {
            label.setText(String.format("%d seconds remaining", t));
            label.setTextFill(t > 5 ? Color.BLACK : Color.rgb(200, 0, 0));
        });
    }
}

/* -------------------------------------------------------------------------- */
