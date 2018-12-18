/* -------------------------------------------------------------------------- */

package mm.client.common.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mm.common.Config;
import mm.common.data.ConcreteHero;
import mm.common.data.Hero;
import mm.common.data.TeamInfo;
import mm.common.util.Validation;

/* -------------------------------------------------------------------------- */

public class TeamState
{
    private final TeamInfo info;

    private final List< Hero > selectedHeroes;

    /* ---------------------------------------------------------------------- */

    public TeamState(TeamInfo info)
    {
        this.info = Objects.requireNonNull(info);

        this.selectedHeroes = new ArrayList<>();

        for (int i = 0; i < Config.TEAM_SIZE; ++i)
            this.selectedHeroes.add(null);
    }

    /* ---------------------------------------------------------------------- */

    public TeamInfo getInfo()
    {
        return info;
    }

    public List< Hero > getUnselectedHeroes()
    {
        List< Hero > heroes = new ArrayList<>();

        for (int i = 0; i < Config.NUM_HEROES; ++i)
            heroes.add(new ConcreteHero(i));

        heroes.removeAll(selectedHeroes);

        return heroes;
    }

    public Hero getSelectedHero(int playerIndex)
    {
        return selectedHeroes.get(Validation.validatePlayerIndex(playerIndex));
    }

    public void setSelectedHero(int playerIndex, Hero hero)
    {
        selectedHeroes.set(playerIndex, hero);
    }
}

/* -------------------------------------------------------------------------- */
