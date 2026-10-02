package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;
import game.server.cards.units.buffs.ArmorBuff;

public class ArmorSpell extends Spell {

    private ArmorSpell() {
        super("Armor", "Grants a friendly unit 1 extra health.", 1, SpellType.BUFF);
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static ArmorSpell create() {
        return new ArmorSpell();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }
        match.replaceUnit(target, new ArmorBuff(target));
    }
}
