package game.server.cards.traps.creators;

import game.server.cards.Trap;
import game.server.cards.TrapCreator;
import game.server.cards.traps.BearTrap;

/**
 * ConcreteCreator - creates BearTrap trap cards
 */
public class BearTrapCreator extends TrapCreator {

    @Override
    public Trap createCard() {
        return BearTrap.create();
    }
}