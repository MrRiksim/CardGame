package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.TrapCreator;
import game.server.cards.Unit;
import game.server.cards.traps.creators.BearTrapCreator;

public class Hunter extends Unit {
    private final TrapCreator creator = new BearTrapCreator();

    private Hunter() {
        super(
                "Hunter",
                "At the beginning of a turn places a bear trap to snare enemy units",
                2,
                1,
                1,
                Element.EARTH,
                null
        );
    }

    public static Hunter create() {
        return new Hunter();
    }

    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        creator.spawn(match, owner);
    }

}
