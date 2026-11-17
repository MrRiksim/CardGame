package game.server.cards;

import game.server.Match;
import game.server.Player;

/**
 * Template Method: applies a buff using a fixed sequence, while concrete
 * spells supply the UnitBuff decorator created for the target.
 */
public abstract class BuffSpell extends Spell {

    protected BuffSpell(String name, String description, int energyCost) {
        super(name, description, energyCost, SpellType.BUFF);
    }

    @Override
    public final void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }

        UnitBuff buff = createBuff(target);
        match.replaceUnit(target, buff);
    }

    protected abstract UnitBuff createBuff(Unit target);
}
