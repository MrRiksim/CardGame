package game.server.validation;

/**
 * Concrete Handler - a card can only be played on your own turn.
 */
public class PlayersTurnHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        if (!request.getMatch().isCurrentTurn(request.getPlayer())) {
            return ValidationResult.rejected("It is not this player's turn");
        }
        return passToNext(request);
    }
}
