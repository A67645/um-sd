/* ------------------------------------------------------------------------- */

package mm.client.gui.util;

import javafx.scene.paint.Color;
import mm.common.data.Team;

/* ------------------------------------------------------------------------- */

public class Config
{
    private Config()
    {
    }

    /* --------------------------------------------------------------------- */

    public static Color getTeamColor(Team team)
    {
        switch (team)
        {
        case BLUE: return Color.rgb(  0,   0, 180);
        case RED : return Color.rgb(180,   0,   0);

        default: throw new IllegalArgumentException();
        }
    }
}

/* ------------------------------------------------------------------------- */
