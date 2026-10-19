package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;

public class WaterUnit extends Unit {

    public WaterUnit() {
        super(
                "Antaniukas",
                "He loves drinking water",
                1,
                1,
                1,
                Element.WATER,
                null
        );
    }

    public static WaterUnit create() {
        return new WaterUnit();
    }

}
