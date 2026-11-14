package game.server.validation;

import game.server.cards.Card;

/**
 * Concrete Handler - the card has to be the kind this play expects, so a
 * PLAY_UNIT message can't place a spell.
 */
public class CardKindHandler extends PlayValidationHandler {

    private final Class<? extends Card> expectedKind;

    public CardKindHandler(Class<? extends Card> expectedKind) {
        this.expectedKind = expectedKind;
    }

    @Override
    public ValidationResult handle(PlayRequest request) {
        Card card = cardOf(request);
        if (card == null) {
            return notInHand();
        }
        if (!expectedKind.isInstance(card)) {
            return ValidationResult.rejected(card.getName() + " is not a " + expectedKind.getSimpleName());
        }
        return passToNext(request);
    }
}
