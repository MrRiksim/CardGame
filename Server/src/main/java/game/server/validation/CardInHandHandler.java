package game.server.validation;

import game.server.cards.Card;

/**
 * Concrete Handler - only cards in your own hand can be played. It also
 * stores the card it finds on the request, so every link after this one can
 * rely on {@link PlayRequest#getCard()} not being null.
 */
public class CardInHandHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        Card card = request.getPlayer().findInHand(request.getCardId());
        if (card == null) {
            return ValidationResult.rejected("That card is not in the player's hand");
        }
        request.setCard(card);
        return passToNext(request);
    }
}
