package game.client;

/**
 * UI events reported by {@link UiColleague} components to {@link UiMediator}.
 */
public enum UiEvent {
    CARD_SELECTED,
    SELECTION_CLEARED,
    TURN_ENDED,
    STATE_UPDATED,
    NEXT_CARD,
    PREVIOUS_CARD
}
