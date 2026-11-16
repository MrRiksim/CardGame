package game.client;

/**
 * <b>Design pattern - Mediator (Concrete Mediator):</b> coordinates
 * interactions between {@link BoardPanel}, {@link CardInfoPanel}, and
 * {@link StatusBar}.
 *
 * <p>Colleagues never reference each other directly; they notify this mediator,
 * which uses {@link CardInfoVisitor} and {@link PlayableZonesVisitor} to update
 * the side table, board zone highlights, and status bar hints.
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
            case CARD_SELECTED -> handleCardSelected(card);
            case SELECTION_CLEARED, TURN_ENDED -> clearSelection();
            case STATE_UPDATED -> handleStateUpdated();
        }
    }

    private void handleCardSelected(ClientCard card) {
        if (card == null) {
            clearSelection();
            return;
        }

        selectedCardId = card.id();
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
        info.clear();
        board.clearHighlight();
        status.setHint(null);
    }
}
