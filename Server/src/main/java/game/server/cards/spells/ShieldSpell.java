package game.server.cards.spells;

import game.server.cards.BuffSpell;
import game.server.cards.Unit;
import game.server.cards.UnitBuff;
import game.server.cards.units.buffs.ShieldBuff;

public class ShieldSpell extends BuffSpell {

    private ShieldSpell() {
        super("Shield", "Gives a friendly unit a shield that absorbs one hit of any size.", 2);
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static ShieldSpell create() {
        return new ShieldSpell();
    }

    @Override
    protected UnitBuff createBuff(Unit target) {
        return new ShieldBuff(target, this);
    }
}
