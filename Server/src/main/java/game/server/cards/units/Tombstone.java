package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;
import game.server.cards.UnitCreator;
import game.server.cards.units.creators.SkellyCreator;

public class Tombstone extends Unit {

    /*
     * Held, not inherited - Tombstone HAS a creator rather than BEING one,
     * which is what lets this same onOwnerTurnStart hook work for any
     * future spawner card just by swapping in a different UnitCreator.
     */
    private final UnitCreator creator = new SkellyCreator();

    private Tombstone() {
        super(
                "Tombstone",
                "Each turn its owner takes it raises a free Skelly in an open zone",
                3,
                4,
                0,
                Element.EARTH,
                "Undead"
        );
    }

    public static Tombstone create() {
        return new Tombstone();
    }

    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        creator.spawn(match, owner);
    }
}
