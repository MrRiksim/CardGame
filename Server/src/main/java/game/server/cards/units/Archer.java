package game.server.cards.units;

import game.server.cards.Element;
import game.server.cards.Unit;

public class Archer extends Unit {

    private Archer() {
        super(
                "Archer",
                "nimble sniper",
                2,
                2,
                1,
                Element.EARTH,
                null
        );
    }

    /*
     * Factory Method - CardFactory calls this instead of "new Knight()"
     * directly, so the creation details stay with the card that knows them.
     */
    public static Archer create() {
        return new Archer();
    }
}
