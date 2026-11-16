package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;

/**
 * Heal spell: heals 2 health for 1 energy cost.
 *
 * <p><b>Design pattern note – State:</b> {@link #canTarget} enforces that
 * Heal can only be cast on an INJURED friendly unit via {@code target.getState().canHeal()}.
 * Normal (full HP), Buffed, or Dead units cannot be targeted.
 */
public class HealSpell extends Spell {

    private static final int HEAL_AMOUNT = 2;

    private HealSpell() {
        super("Heal", "Restores 2 health to an injured friendly unit.", 1, SpellType.SPECIAL);
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static HealSpell create() {
        return new HealSpell();
    }

    @Override
    public boolean canTarget(Match match, Player caster, Unit target) {
        return target != null
                && match.ownerOf(target) == caster
                && target.getState().canHeal();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null || !canTarget(match, caster, target)) {
            return;
        }
        target.heal(HEAL_AMOUNT);
    }
}
