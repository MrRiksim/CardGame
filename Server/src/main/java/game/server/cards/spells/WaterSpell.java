package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;

public class WaterSpell extends Spell {

    private static final int DAMAGE_AMOUNT = 2;

    public WaterSpell() {
        super(
                "Bajoru",
                "Never buying this again",
                1,
                SpellType.DAMAGE
        );
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static WaterSpell create() {
        return new WaterSpell();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }
        target.takeDamage(DAMAGE_AMOUNT);
    }
}
