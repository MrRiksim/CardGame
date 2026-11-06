package game.server.validation;

import game.server.Match;
import game.server.Player;
import game.server.cards.Spell;
import game.server.cards.Unit;

/**
 * Concrete Handler - what a spell's type allows it to target: DAMAGE only an
 * enemy unit, BUFF only your own unit, SPECIAL anything (including nothing).
 */
public class SpellTargetTypeHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        Spell spell = (Spell) request.getCard();
        Match match = request.getMatch();
        Player caster = request.getPlayer();
        Unit target = request.getTarget();

        boolean allowed = switch (spell.getSpellType()) {
            case DAMAGE -> target != null && match.ownerOf(target) == match.getOpponent(caster);
            case BUFF -> target != null && match.ownerOf(target) == caster;
            case SPECIAL -> true;
        };

        if (!allowed) {
            return ValidationResult.rejected(
                    spell.getSpellType() + " spell " + spell.getName() + " can't target that");
        }
        return passToNext(request);
    }
}
