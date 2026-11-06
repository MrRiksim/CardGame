package game.server.validation;

/**
 * Concrete Handler - nothing can be played once the match has ended.
 */
public class MatchActiveHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        if (!request.getMatch().isActive()) {
            return ValidationResult.rejected("The match has ended");
        }
        return passToNext(request);
    }
}
