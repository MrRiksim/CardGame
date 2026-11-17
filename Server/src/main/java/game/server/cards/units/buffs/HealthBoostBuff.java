package game.server.cards.units.buffs;

import game.server.cards.Unit;
import game.server.cards.UnitBuff;
import game.server.cards.Spell;
import game.server.memento.UnitMemento;

public class HealthBoostBuff extends UnitBuff {

    private int remaining = 1;

    @Override
    public UnitMemento createMemento() {
        UnitMemento wrappedState = super.createMemento();
        int savedRemaining = remaining;
        return () -> {
            wrappedState.restore();
            remaining = savedRemaining;
        };
    }

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
