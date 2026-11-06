package game.server.validation;

import game.server.Match;

/**
 * Concrete Handler - the zone index comes straight from the network, so it
 * has to be checked before {@link ZoneFreeHandler} uses it on an array.
 */
public class ZoneInRangeHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        int zoneIndex = request.getZoneIndex();
        if (zoneIndex < 0 || zoneIndex >= Match.NUMBER_OF_ZONES) {
            return ValidationResult.rejected("There is no zone " + zoneIndex);
        }
        return passToNext(request);
    }
}
