package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Card;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;

public class EssenceReplicationSpell extends Spell {

    public EssenceReplicationSpell() {
        super(
                "Essence Replication",
                "Targets any unit on the field and adds a fully independent buff-intact deep copy to your hand.",
                4, // Adjust energy cost as needed
                SpellType.SPECIAL
        );
    }

    @Override
    public boolean canTarget(Match match, Player caster, Unit target) {
        // Can target any unit currently active on the field
        return target != null && match.findUnitOnField(target.getId()) != null;
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (!canTarget(match, caster, target)) {
            return;
        }

        // 1. Perform Deep Copy via Prototype Pattern (recursively copies buffs & adapters)
        Unit deepCopiedUnit = target.cloneDeep();

        // 2. Add the independent cloned card straight into the player's hand
        caster.getHand().add(deepCopiedUnit);
    }
    public static Card create() {
        return new EssenceReplicationSpell();
    }
}