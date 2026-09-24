package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;

public class LightningStrike extends Spell {

    private static final int DAMAGE_AMOUNT = 2;

    private LightningStrike() {
        super(
                "Lightning Strike",
                "Strikes a single enemy unit with a bolt of lightning",
                1,
                SpellType.DAMAGE
        );
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static LightningStrike create() {
        return new LightningStrike();
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target == null) {
            return;
        }
        target.takeDamage(DAMAGE_AMOUNT);
    }
}
