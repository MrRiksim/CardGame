package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;

public class FireUnit extends Unit {

    public FireUnit() {
        super(
                "Pyro",
                "mmpphhgggffff",
                1,
                2,
                2,
                Element.FIRE,
                null
        );
    }

    public static FireUnit create() {
        return new FireUnit();
    }

}
