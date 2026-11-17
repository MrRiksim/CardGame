package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;
import game.server.cards.iterators.WeakestFirstIterator;

public final class HealingWaveSpell extends Spell {
    private HealingWaveSpell() {
        super("Healing Wave", "Heals up to 3 injured, unbuffed allies by 1 health each, weakest first. Cannot be undone.",
                2, SpellType.SPECIAL);
    }

    public static HealingWaveSpell create() {
        return new HealingWaveSpell();
    }

    @Override
    public boolean canTarget(Match match, Player caster, Unit target) {
        return new WeakestFirstIterator(caster.getFrontZones()).hasNext();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        WeakestFirstIterator allies = new WeakestFirstIterator(caster.getFrontZones());
        for (int charge = 0; charge < 3 && allies.hasNext(); charge++) {
            allies.next().heal(1);
        }
    }

    @Override
    public boolean isUndoable() {
        return false; // Retains this global spell's existing undo-boundary behavior.
    }
}
