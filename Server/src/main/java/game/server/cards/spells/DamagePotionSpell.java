package game.server.cards.spells;

import game.server.cards.BuffSpell;
import game.server.cards.Unit;
import game.server.cards.UnitBuff;
import game.server.cards.units.buffs.DamagePotionBuff;

public class DamagePotionSpell extends BuffSpell {

    private DamagePotionSpell() {
        super("Damage Potion", "Grants a friendly unit 1 extra attack.", 1);
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static DamagePotionSpell create() {
        return new DamagePotionSpell();
    }

    @Override
    protected UnitBuff createBuff(Unit target) {
        return new DamagePotionBuff(target, this);
    }
}
