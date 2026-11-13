package game.server.validation;

import game.server.cards.Unit;

/**
 * Concrete Handler - only a card that can ride (see Jumpy) may be attached to
 * another unit. Must come after a {@link CardKindHandler} for Unit.
 */
public class RiderCardHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        Unit unit = (Unit) request.getCard();
        if (!unit.canRide()) {
            return ValidationResult.rejected(unit.getName() + " can't be attached to another unit");
        }
        return passToNext(request);
    }
}
