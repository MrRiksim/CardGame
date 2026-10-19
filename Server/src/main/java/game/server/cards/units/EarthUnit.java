package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;

public class EarthUnit extends Unit {

    public EarthUnit() {
        super(
                "BroKarolis",
                "Rocking the trendiest broccoli haircut",
                1,
                2,
                1,
                Element.EARTH,
                null
        );
    }

    public static EarthUnit create() {
        return new EarthUnit();
    }

}
