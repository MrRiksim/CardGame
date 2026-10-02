package game.server.cards.units.buffs;

import game.server.cards.Unit;
import game.server.cards.UnitBuff;

/**
 * Concrete Decorator - adds +1 health (both current and max) on top of
 * whatever unit it wraps, which may itself already be another buff layer.
 */
public class ArmorBuff extends UnitBuff {

    public ArmorBuff(Unit wrapped) {
        super("Armor", "Grants the unit 1 extra health.", 1, wrapped, 1, 0);
    }
}
