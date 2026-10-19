package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;
import game.server.cards.units.buffs.ShieldBuff;

public class ShieldSpell extends Spell {

    private ShieldSpell() {
        super("Shield", "Gives a friendly unit a shield that absorbs one hit of any size.", 2, SpellType.BUFF);
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static ShieldSpell create() {
        return new ShieldSpell();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }
        match.replaceUnit(target, new ShieldBuff(target, this));
    }
}
