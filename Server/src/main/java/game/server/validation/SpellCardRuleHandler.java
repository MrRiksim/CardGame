package game.server.validation;

import game.server.cards.Card;
import game.server.cards.Spell;

/**
 * Concrete Handler - any targeting rule a spell adds for itself on top of its
 * type, such as Cripple only working on a buffed enemy unit.
 */
public class SpellCardRuleHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        Card card = cardOf(request);
        if (card == null) {
            return notInHand();
        }
        if (!(card instanceof Spell spell)) {
            return ValidationResult.rejected(card.getName() + " is not a Spell");
        }
        if (!spell.canTarget(request.getMatch(), request.getPlayer(), request.getTarget())) {
            return ValidationResult.rejected(spell.getName() + " can't be used on that target");
        }
        return passToNext(request);
    }
}
