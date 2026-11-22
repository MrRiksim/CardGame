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
     * Design pattern note – State: Cripple can only target an enemy unit whose
     * current combat state is BUFFED (enforced via target.getState().canCripple()).
     */
    @Override
    public boolean canTarget(Match match, Player caster, Unit target) {
        return target != null && match.isEnemyUnit(caster, target) && target.canCripple();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }
        match.stripBuffs(target);
    }
}
