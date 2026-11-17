package game.server.cards.spells;

import game.server.GameStatistics;
import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;
import game.server.cards.iterators.CircularTargetIterator;

public final class ChainLightningSpell extends Spell {
    private ChainLightningSpell() {
        super("Chain Lightning", "Deals 1 damage to up to 3 enemies, starting at the target and wrapping right.",
                2, SpellType.DAMAGE);
    }

    public static ChainLightningSpell create() {
        return new ChainLightningSpell();
    }

    @Override
    public boolean canTarget(Match match, Player caster, Unit target) {
        return target != null && !target.isDead() && match.ownerOf(target) == match.getOpponent(caster);
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (!canTarget(match, caster, target)) {
            return;
        }
        Unit[] zones = match.getOpponent(caster).getFrontZones();
        int start = 0;
        while (zones[start] != target) {
            start++;
        }
        CircularTargetIterator targets = new CircularTargetIterator(zones, start);
        int hit = 0;
        while (hit < 3 && targets.hasNext()) {
            Unit victim = targets.next();
            if (match.ownerOf(victim) != match.getOpponent(caster)) {
                continue; // A previous hit may also have removed a linked illusion.
            }
            hit++;
            int before = victim.getHealth();
            victim.takeDamage(1);
            GameStatistics.getInstance().recordDamageDealt(Math.max(0, before - victim.getHealth()));
            match.resolveUnitState(victim);
        }
    }

}
