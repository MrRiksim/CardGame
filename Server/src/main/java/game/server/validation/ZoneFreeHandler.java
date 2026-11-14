package game.server.validation;

import game.server.Player;
import game.server.cards.Card;

import java.util.function.Function;

/**
 * Concrete Handler - a field holds one card. Given which row to look at, so
 * unit plays (front row) and trap plays (back row) can share this one link.
 */
public class ZoneFreeHandler extends PlayValidationHandler {

    private final Function<Player, Card[]> row;

    public ZoneFreeHandler(Function<Player, Card[]> row) {
        this.row = row;
    }

    /*
     * Checks the index itself as well, rather than relying on a
     * ZoneInRangeHandler earlier in the chain, so it's safe to run on its own.
     */
    @Override
    public ValidationResult handle(PlayRequest request) {
        Card[] zones = row.apply(request.getPlayer());
        int zoneIndex = request.getZoneIndex();
        if (zoneIndex < 0 || zoneIndex >= zones.length) {
            return ValidationResult.rejected("There is no zone " + zoneIndex);
        }
        if (zones[zoneIndex] != null) {
            return ValidationResult.rejected("Zone " + zoneIndex + " is already occupied");
        }
        return passToNext(request);
    }
}
