package game.server.validation;

import game.server.cards.Card;

/**
 * Concrete Handler - the card has to be the kind this play expects, so a
 * PLAY_UNIT message can't place a spell. The links after this one can safely
 * cast the request's card to that kind.
 */
public class CardKindHandler extends PlayValidationHandler {

    private final Class<? extends Card> expectedKind;

    public CardKindHandler(Class<? extends Card> expectedKind) {
        this.expectedKind = expectedKind;
    }

    @Override
    public ValidationResult handle(PlayRequest request) {
        if (!expectedKind.isInstance(request.getCard())) {
            return ValidationResult.rejected(
                    request.getCard().getName() + " is not a " + expectedKind.getSimpleName());
        }
        return passToNext(request);
    }
}
