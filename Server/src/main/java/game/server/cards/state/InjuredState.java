package game.server.cards.state;

import game.server.cards.Unit;

/**
 * Design pattern – State (Concrete State: Injured).
 *
 * <p>Unit's health is below its maximum printed health.
 * Can be healed (Heal spell only targets Injured units).
 * Cannot be crippled (no buffs).
 * Transitions:
 * <ul>
 *   <li>Healed back to max health &rarr; {@link NormalState}</li>
 *   <li>Takes fatal damage &rarr; {@link DeadState}</li>
 * </ul>
 */
public class InjuredState implements UnitState {

    @Override
    public UnitStateKind getKind() {
        return UnitStateKind.INJURED;
    }

    @Override
    public boolean canHeal() {
        return true; // Heal spell works on injured units!
    }

    @Override
    public boolean canCripple() {
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
        }
    }

    @Override
    public void heal(Unit unit, int amount) {
        if (amount <= 0) {
            return;
        }
        unit.setInternalHealth(Math.min(unit.getMaxHealth(), unit.getHealth() + amount));
        if (unit.getHealth() >= unit.getMaxHealth()) {
            unit.setState(new NormalState());
        }
    }

    @Override
    public String toString() {
        return "INJURED";
    }
}
