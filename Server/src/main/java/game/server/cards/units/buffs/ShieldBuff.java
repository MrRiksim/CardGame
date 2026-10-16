package game.server.cards.units.buffs;

import game.server.cards.Unit;
import game.server.cards.UnitBuff;
import game.server.cards.Spell;

/**
 * Concrete Decorator - absorbs exactly one hit of any size. It changes no
 * number on the unit, so the client shows it as a blue tint on the card
 * instead (one tint per Shield layer, see CardRenderer).
 */
public class ShieldBuff extends UnitBuff {

    private boolean used = false;

    public ShieldBuff(Unit wrapped, Spell source) {
        super(wrapped, source);
    }

    @Override
    protected int shieldLayers() {
        return used ? 0 : 1;
    }

    @Override
    protected int absorb(int damage) {
        if (used) {
            return damage;
        }
        used = true;
        return 0;
    }

    @Override
    protected boolean isSpent() {
        return used;
    }
}
