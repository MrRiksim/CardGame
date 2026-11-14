package game.server.validation;

/**
 * Concrete Handler - only cards in your own hand can be played. It also
 * stores the card it finds on the request, so the links after it don't have
 * to look it up again.
 */
public class CardInHandHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        if (cardOf(request) == null) {
            return notInHand();
        }
        return passToNext(request);
    }
}
