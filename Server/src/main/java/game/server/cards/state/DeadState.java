package game.server.cards.state;

import game.server.cards.Unit;

/**
 * Design pattern – State (Concrete State: Dead).
 *
 * <p>Unit's health has dropped to 0 or below and the card is eliminated from the field.
 * Cannot take further damage, cannot be healed, and cannot be crippled.
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
    public boolean canHeal() {
        return false;
    }

    @Override
    public boolean canCripple() {
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
