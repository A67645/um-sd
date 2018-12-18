/* -------------------------------------------------------------------------- */

package mm.client.gui.util;

import java.util.function.Consumer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

/* -------------------------------------------------------------------------- */

public class Countdown
{
    private final Timeline timeline;

    private int seconds;

    /* ---------------------------------------------------------------------- */

    public Countdown(int initialSeconds, Consumer< Integer > onTick)
    {
        if (initialSeconds < 0)
        {
            throw new IllegalArgumentException(
                "initialSeconds must be non-negative"
                );
        }
        else if (initialSeconds == 0)
        {
            Platform.runLater(() -> onTick.accept(0));

            timeline = null;
        }
        else
        {
            seconds = initialSeconds;

            timeline = new Timeline(new KeyFrame(
                Duration.seconds(1),
                e -> onTick.accept(--seconds)
                ));

            timeline.setCycleCount(initialSeconds);

            Platform.runLater(() -> onTick.accept(initialSeconds));

            timeline.play();
        }
    }

    /* ---------------------------------------------------------------------- */

    public void stop()
    {
        if (timeline != null)
            timeline.stop();
    }
}

/* -------------------------------------------------------------------------- */
