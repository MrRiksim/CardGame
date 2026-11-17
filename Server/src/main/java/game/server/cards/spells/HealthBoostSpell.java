package game.server.cards.spells;

import game.server.cards.BuffSpell;
import game.server.cards.Unit;
import game.server.cards.UnitBuff;
import game.server.cards.units.buffs.HealthBoostBuff;

public class HealthBoostSpell extends BuffSpell {

    private HealthBoostSpell() {
        super("Health Boost", "Grants a friendly unit 1 extra health.", 1);
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static HealthBoostSpell create() {
        return new HealthBoostSpell();
    }

    @Override
    protected UnitBuff createBuff(Unit target) {
        return new HealthBoostBuff(target, this);
    }
}
