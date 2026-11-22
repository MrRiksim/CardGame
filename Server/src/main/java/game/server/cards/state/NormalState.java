package game.server.cards.state;

import game.server.cards.Unit;

/**
 * Design pattern – State (Concrete State: Normal).
 *
 * <p>Default state of a unit: full health, no modifiers/buffs active.
 * Transitions:
 * <ul>
 *   <li>Takes non-fatal damage or receives buff/curse &rarr; {@link AffectedState}</li>
 *   <li>Takes fatal damage &rarr; {@link DeadState}</li>
 * </ul>
 */
public class NormalState implements UnitState {

    @Override
    public UnitStateKind getKind() {
        return UnitStateKind.NORMAL;
    }

    @Override
    public boolean canPlay() {
        return false; // Already on field
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
        if (amount <= 0) {
            return;
        }
        unit.setInternalHealth(Math.max(0, unit.getHealth() - amount));
        if (unit.getHealth() <= 0) {
            unit.setState(new DeadState());
        } else if (unit.getHealth() < unit.getMaxHealth()) {
            unit.setState(new AffectedState());
        }
    }

    @Override
    public void heal(Unit unit, int amount) {
        // Already at maximum health in normal state
    }

    @Override
    public String toString() {
        return "NORMAL";
    }
}
