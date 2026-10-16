package game.server.cards;

import game.server.cards.spells.ShiftingSpellCard;

public class DamageBuffForStrategy extends UnitBuff{
    public DamageBuffForStrategy(Unit wrapped, Spell spell) {
        super(
                wrapped,
                spell
        );
    }

    @Override
    protected int damageBonus() {
        return 1;
    }
}
