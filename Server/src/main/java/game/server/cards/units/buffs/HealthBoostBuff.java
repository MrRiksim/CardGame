package game.server.cards.units.buffs;

import game.server.cards.Unit;
import game.server.cards.UnitBuff;
import game.server.cards.Spell;

public class HealthBoostBuff extends UnitBuff {

    private int remaining = 1;

    public HealthBoostBuff(Unit wrapped, Spell source) {
        super(wrapped, source);
    }

    @Override
    protected int healthBonus() {
        return remaining;
    }

    @Override
    protected int absorb(int damage) {
        int absorbed = Math.min(remaining, damage);
        remaining -= absorbed;
        return damage - absorbed;
    }

    @Override
    protected boolean isSpent() {
        return remaining <= 0;
    }
}