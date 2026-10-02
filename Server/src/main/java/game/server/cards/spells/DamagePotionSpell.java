package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;
import game.server.cards.units.buffs.DamagePotionBuff;

public class DamagePotionSpell extends Spell {

    private DamagePotionSpell() {
        super("Damage Potion", "Grants a friendly unit 1 extra attack.", 1, SpellType.BUFF);
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static DamagePotionSpell create() {
        return new DamagePotionSpell();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }
        match.replaceUnit(target, new DamagePotionBuff(target));
    }
}
