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

    @Override
    public ValidationResult handle(PlayRequest request) {
        if (row.apply(request.getPlayer())[request.getZoneIndex()] != null) {
            return ValidationResult.rejected("Zone " + request.getZoneIndex() + " is already occupied");
        }
        return passToNext(request);
    }
}
