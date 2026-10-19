package game.server.cards;

import game.server.cards.spells.ShiftingSpellCard;

public class DamageBuffForStrategy extends UnitBuff {

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

    @Override
    public Unit cloneShallow() {
        // Shallow copy shares the exact same wrapped unit reference
        return new DamageBuffForStrategy(this.getWrapped(), this.getSource());
    }

    @Override
    public Unit cloneDeep() {
        // Deep copy recursively deep-copies the underlying unit or buff stack beneath it
        Unit clonedWrapped = (this.getWrapped() != null) ? this.getWrapped().cloneDeep() : null;

        // Return a brand-new independent buff wrapping the cloned stack
        return new DamageBuffForStrategy(clonedWrapped, this.getSource());
    }
}