package game.server.cards.units;

import game.server.cards.Element;
import game.server.cards.Unit;

/**
 * A normal, drawable unit card in its own right - it isn't only ever
 * produced by Tombstone's SkellyCreator, it can also just be dealt into a
 * hand like any other card (see CardFactory's pool).
 */
public class Skelly extends Unit {

    private Skelly() {
        super(
                "Skelly",
                "A rattling and disposable skeleton",
                1,
                1,
                1,
                Element.EARTH,
                "Undead"
        );
    }

    /*
     * Factory Method - see the note on CardFactory. This is the "creates
     * itself" static method CardFactory calls directly for a normal draw;
     * SkellyCreator.createUnit() also calls this same method when
     * Tombstone spawns one - the card doesn't know or care which.
     */
    public static Skelly create() {
        return new Skelly();
    }
}
