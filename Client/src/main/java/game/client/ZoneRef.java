package game.client;

/**
 * Identifies a board zone that can be highlighted when a card is selected.
 */
public record ZoneRef(Row row, int index) {

    public enum Row {
        OWN_FRONT,
        OWN_BACK,
        OPPONENT_FRONT,
        OWN_UTILITY,
        OPPONENT_UTILITY
    }
}
