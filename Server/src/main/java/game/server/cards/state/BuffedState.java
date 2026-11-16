package game.server.cards.state;

import game.server.cards.Unit;

/**
 * Design pattern – State (Concrete State: Buffed).
 *
 * <p>Unit has active buff wrappers attached to it.
 * Can be crippled (Cripple spell only works on Buffed units).
 * Cannot be healed (Heal only targets Injured units).
 * Transitions:
 * <ul>
 *   <li>Takes fatal damage &rarr; {@link DeadState}</li>
 *   <li>Buffs stripped by Cripple &rarr; {@link NormalState} or {@link InjuredState}</li>
 * </ul>
 */
public class BuffedState implements UnitState {

    @Override
    public UnitStateKind getKind() {
        return UnitStateKind.BUFFED;
    }

    @Override
    public boolean canHeal() {
        return false;
    }

    @Override
    public boolean canCripple() {
        return true; // Cripple spell works on buffed units!
    }

    @Override
    public void takeDamage(Unit unit, int amount) {
        if (amount <= 0) {
            return;
        }
        unit.setInternalHealth(Math.max(0, unit.getHealth() - amount));
        if (unit.getHealth() <= 0) {
            unit.setState(new DeadState());
        }
    }

    @Override
    public void heal(Unit unit, int amount) {
        // Heal does not target Buffed units
    }

    @Override
    public String toString() {
        return "BUFFED";
    }
}
