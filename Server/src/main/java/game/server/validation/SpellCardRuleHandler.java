package game.server.validation;

import game.server.cards.Spell;

/**
 * Concrete Handler - any targeting rule a spell adds for itself on top of its
 * type, such as Cripple only working on a buffed enemy unit.
 */
public class SpellCardRuleHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        Spell spell = (Spell) request.getCard();
        if (!spell.canTarget(request.getMatch(), request.getPlayer(), request.getTarget())) {
            return ValidationResult.rejected(spell.getName() + " can't be used on that target");
        }
        return passToNext(request);
    }
}
