package game.server.cards.state;

import game.server.cards.Unit;

/**
 * Design pattern – State (Concrete State: Dead).
 *
 * <p>Unit's health has dropped to 0 or below and the card is eliminated from the field.
 * Transitions:
 * <ul>
 *   <li>Unit reset (resurrection / graveyard preparation) &rarr; {@link NormalState}</li>
 * </ul>
 */
public class DeadState implements UnitState {

    @Override
    public UnitStateKind getKind() {
        return UnitStateKind.DEAD;
    }

    @Override
    public boolean canPlay() {
        return false;
    }

    @Override
    public boolean canHeal(Unit unit) {
        return false;
    }

    @Override
    public boolean canCripple(Unit unit) {
        return false;
    }

    @Override
    public void takeDamage(Unit unit, int amount) {
        // Already dead, cannot take further damage
    }

    @Override
    public void heal(Unit unit, int amount) {
        // Dead unit cannot be healed on the field
    }

    @Override
    public String toString() {
        return "DEAD";
    }
}
