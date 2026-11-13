package game.server.validation;

import game.server.Match;

/**
 * Concrete Handler - the target has to be a unit on the player's own side of
 * the field.
 */
public class OwnUnitTargetHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        Match match = request.getMatch();
        if (request.getTarget() == null || match.ownerOf(request.getTarget()) != request.getPlayer()) {
            return ValidationResult.rejected("The target has to be one of the player's own units");
        }
        return passToNext(request);
    }
}
