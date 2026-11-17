package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;
import game.server.cards.iterators.RecentCardsIterator;

public final class RaiseTheFallenSpell extends Spell {
    private RaiseTheFallenSpell() {
        super("Raise the Fallen", "Returns up to 2 newest unit cards from your graveyard to your hand, if there is room. Cannot be undone.",
                3, SpellType.SPECIAL);
    }

    public static RaiseTheFallenSpell create() {
        return new RaiseTheFallenSpell();
    }

    @Override
    public boolean canTarget(Match match, Player caster, Unit target) {
        // Casting this spell frees its own hand slot before the effect resolves.
        return caster.getHand().size() - (caster.getHand().contains(this) ? 1 : 0)
                < caster.getMaxHandSize()
                && new RecentCardsIterator(caster.getGraveyard()).hasNext();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        RecentCardsIterator fallen = new RecentCardsIterator(caster.getGraveyard());
        for (int recovered = 0; recovered < 2 && !caster.isHandFull() && fallen.hasNext(); recovered++) {
            Unit unit = fallen.next();
            unit.reset();
            if (caster.addToHand(unit)) {
                caster.getGraveyard().remove(unit);
            }
        }
    }

    @Override
    public boolean isUndoable() {
        return false; // Retains this global spell's existing undo-boundary behavior.
    }
}
