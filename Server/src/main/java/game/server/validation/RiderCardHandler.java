package game.server.validation;

import game.server.cards.Card;
import game.server.cards.Unit;

/**
 * Concrete Handler - only a card that can ride (see Jumpy) may be attached to
 * another unit.
 */
public class RiderCardHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        Card card = cardOf(request);
        if (card == null) {
            return notInHand();
        }
        if (!(card instanceof Unit unit) || !unit.canRide()) {
            return ValidationResult.rejected(card.getName() + " can't be attached to another unit");
        }
        return passToNext(request);
    }
}
