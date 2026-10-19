package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;

public class WindUnit extends Unit {

    public WindUnit() {
        super(
                "Fog man",
                "must've been the wind",
                1,
                2,
                1,
                Element.WIND,
                null
        );
    }

    public static WindUnit create() {
        return new WindUnit();
    }

}
