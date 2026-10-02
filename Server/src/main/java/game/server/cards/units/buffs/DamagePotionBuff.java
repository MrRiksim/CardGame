package game.server.cards.units.buffs;

import game.server.cards.Unit;
import game.server.cards.UnitBuff;

/**
 * Concrete Decorator - adds +1 damage on top of whatever unit it wraps,
 * which may itself already be another buff layer.
 */
public class DamagePotionBuff extends UnitBuff {

    public DamagePotionBuff(Unit wrapped) {
        super("Damage Potion", "Grants the unit 1 extra attack.", 1, wrapped, 0, 1);
    }
}
