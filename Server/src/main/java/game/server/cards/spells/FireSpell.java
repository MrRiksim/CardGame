package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;

public class FireSpell extends Spell {

    private static final int DAMAGE_AMOUNT = 67;

    public FireSpell() {
        super(
                "DUM BUM",
                "wonder why these are illegal now",
                1,
                SpellType.DAMAGE
        );
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static FireSpell create() {
        return new FireSpell();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }
        target.takeDamage(DAMAGE_AMOUNT);
    }
}
