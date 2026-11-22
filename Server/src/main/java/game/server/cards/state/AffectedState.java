package game.server.cards.state;

import game.server.cards.Unit;
import game.server.cards.UnitBuff;

/**
 * Design pattern – State (Concrete State: Affected).
 *
 * <p>Represents a unit under one or more active modifiers:
 * <ul>
 *   <li>Damaged (health &lt; maxHealth) &rarr; can be targeted by Heal spells</li>
 *   <li>Buffed (UnitBuff wrapper or stat buffs) &rarr; can be targeted by Cripple spells</li>
 *   <li>Cursed, shielded, or other status effects</li>
 * </ul>
 *
 * <p>Transitions:
 * <ul>
 *   <li>Damage drops health &le; 0 &rarr; {@link DeadState}</li>
 *   <li>Healing back to full health with no remaining buffs &rarr; {@link NormalState}</li>
 * </ul>
 */
public class AffectedState implements UnitState {

    @Override
    public UnitStateKind getKind() {
        return UnitStateKind.AFFECTED;
    }

    @Override
    public boolean canPlay() {
        return false; // Already on field
    }

    @Override
    public boolean canHeal(Unit unit) {
        return unit != null && unit.getHealth() < unit.getMaxHealth();
    }

    @Override
    public boolean canCripple(Unit unit) {
        return unit != null && (unit instanceof UnitBuff || unit.isHealthBuffed() || unit.isDamageBuffed() || unit.getShieldCount() > 0);
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
        // If fully healed and has no remaining buffs, return to NormalState
        if (unit.getHealth() >= unit.getMaxHealth() && !canCripple(unit)) {
            unit.setState(new NormalState());
        }
    }

    @Override
    public String toString() {
        return "AFFECTED";
    }
}
