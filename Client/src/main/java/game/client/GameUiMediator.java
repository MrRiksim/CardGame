package game.client;

/**
 * <b>Design pattern - Mediator (Concrete Mediator):</b> coordinates
 * interactions between {@link BoardPanel}, {@link CardInfoPanel}, and
 * {@link StatusBar}.
 *
 * <p>Colleagues never reference each other directly; they notify this mediator,
 * which uses {@link CardInfoVisitor} and {@link PlayableZonesVisitor} to update
 * the side table, board zone highlights, and status bar hints. The Prev / Next
 * buttons are served by an Iterator ({@link ClientCardIterator}) obtained from
 * the board, which acts as the {@link ClientCardAggregate}.
 */
public class GameUiMediator implements UiMediator {

    private final BoardPanel board;
    private final CardInfoPanel info;
    private final StatusBar status;

    private String selectedCardId;

    public GameUiMediator(BoardPanel board, CardInfoPanel info, StatusBar status) {
        this.board = board;
        this.info = info;
        this.status = status;

        this.board.setMediator(this);
        this.info.setMediator(this);
        this.status.setMediator(this);
    }

    @Override
    public void notify(UiColleague sender, UiEvent event, ClientCard card) {
        switch (event) {
            case CARD_SELECTED -> {
                // A direct click isn't part of a Prev/Next walk, so drop the "3 / 12" counter.
                info.setPositionText("");
                handleCardSelected(card);
            }
            case SELECTION_CLEARED, TURN_ENDED -> clearSelection();
            case STATE_UPDATED -> handleStateUpdated();
            case NEXT_CARD -> browse(true);
            case PREVIOUS_CARD -> browse(false);
        }
    }

    /*
     * Iterator pattern client: asks the board (the Aggregate) for an
     * iterator, continues from whichever card is selected, and then selects
     * the next/previous card exactly as if the player had clicked it.
     * The mediator never sees the board's internal hand/row/graveyard fields.
     */
    private void browse(boolean forward) {
        ClientCardIterator iterator = info.isOwnCardsOnly()
                ? board.createOwnCardsIterator()
                : board.createAllCardsIterator();

        if (iterator.isEmpty()) {
            status.setHint("There are no cards to browse.");
            info.setPositionText("");
            return;
        }

        iterator.seekTo(selectedCardId);
        ClientCard card = forward ? iterator.next() : iterator.previous();
        handleCardSelected(card);
        info.setPositionText(iterator.position() + " / " + iterator.size());
    }

    private void handleCardSelected(ClientCard card) {
        if (card == null) {
            clearSelection();
            return;
        }

        selectedCardId = card.id();
        board.setSelectedCard(card.id());
        info.show(card);

        CardInfoVisitor infoVisitor = new CardInfoVisitor();
        card.accept(infoVisitor);

        if (board.isInOwnHand(card.id()) && board.isYourTurn()) {
            PlayableZonesVisitor zonesVisitor = new PlayableZonesVisitor(
                    board.getOwnFront(),
                    board.getOpponentFront(),
                    board.getOwnBack()
            );
            card.accept(zonesVisitor);
            board.highlight(zonesVisitor.getZones());
            status.setHint(infoVisitor.getHint());
        } else {
            board.clearHighlight();
            status.setHint("Inspecting: " + card.name());
        }
    }

    private void handleStateUpdated() {
        if (selectedCardId == null) {
            board.clearHighlight();
            return;
        }

        ClientCard updated = board.findCardById(selectedCardId);
        if (updated == null) {
            clearSelection();
        } else {
            handleCardSelected(updated);
        }
    }

    private void clearSelection() {
        selectedCardId = null;
        board.setSelectedCard(null);
        info.setPositionText("");
        info.clear();
        board.clearHighlight();
        status.setHint(null);
    }
}
