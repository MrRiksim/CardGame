package game.server.cards.units.buffs;

import game.server.cards.Unit;
import game.server.cards.UnitBuff;
import game.server.cards.Spell;

public class DamagePotionBuff extends UnitBuff {

    public DamagePotionBuff(Unit wrapped, Spell source) {
        super(wrapped, source);
    }

    @Override
    protected int damageBonus() {
        return 1;
    }
}