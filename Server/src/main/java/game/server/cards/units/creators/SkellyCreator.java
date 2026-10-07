package game.server.cards.units.creators;

import game.server.cards.Unit;
import game.server.cards.UnitCreator;
import game.server.cards.units.Skelly;

/**
 * ConcreteCreator - the only thing it decides is which unit comes out of
 * {@link #createUnit()}. Everything about how/where that unit actually
 * gets placed lives in {@link UnitCreator#spawn} and never needs touching
 * here, no matter how many more creators like this get added later.
 */
public class SkellyCreator extends UnitCreator {

    @Override
    public Unit createUnit() {
        return Skelly.create();
    }
}
