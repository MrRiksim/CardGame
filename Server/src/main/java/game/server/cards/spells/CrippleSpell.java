package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;
import game.server.cards.UnitBuff;

public class CrippleSpell extends Spell {

    private CrippleSpell() {
        super("Cripple", "Removes every buff from an enemy unit.", 2, SpellType.SPECIAL);
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static CrippleSpell create() {
        return new CrippleSpell();
    }

    /*
     * SpellType.SPECIAL on its own would allow any target (or none), so
     * the real rules live here: an enemy unit that actually has a buff on
     * it - which is exactly "is a UnitBuff", since a unit with no buffs is
     * the plain base unit. Checked before any energy is spent.
     */
    @Override
    public boolean canTarget(Match match, Player caster, Unit target) {
        return target instanceof UnitBuff && match.isEnemyUnit(caster, target);
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }
        match.stripBuffs(target);
    }
}
