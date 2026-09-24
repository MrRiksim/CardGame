package game.server.cards.units;

import game.server.cards.Element;
import game.server.cards.Unit;

public class Knight extends Unit {

    private Knight() {
        super(
                "Knight",
                "A sturdy frontline warrior",
                2,
                3,
                1,
                Element.EARTH,
                null
        );
    }

    /*
     * Factory Method - CardFactory calls this instead of "new Knight()"
     * directly, so the creation details stay with the card that knows them.
     */
    public static Knight create() {
        return new Knight();
    }
}
