package game.server.validation;

import game.server.Match;
import game.server.Player;
import game.server.cards.Card;
import game.server.cards.Unit;

/**
 * <b>Design pattern - Chain of Responsibility (Request):</b> the attempted
 * play that travels down a validation chain. It isn't read-only:
 * {@link CardInHandHandler} looks the card up and stores it here, which is
 * what every later link works from, so the order of the links matters.
 */
public final class PlayRequest {

    private static final int NO_ZONE = -1;

    private final Match match;
    private final Player player;
    private final String cardId;
    private final int zoneIndex;
    private final Unit target;

    private Card card;

    private PlayRequest(Match match, Player player, String cardId, int zoneIndex, Unit target) {
        this.match = match;
        this.player = player;
        this.cardId = cardId;
        this.zoneIndex = zoneIndex;
        this.target = target;
    }

    public static PlayRequest placement(Match match, Player player, String cardId, int zoneIndex) {
        return new PlayRequest(match, player, cardId, zoneIndex, null);
    }

    /* target is null for a spell cast with no unit under it. */
    public static PlayRequest spellCast(Match match, Player player, String cardId, Unit target) {
        return new PlayRequest(match, player, cardId, NO_ZONE, target);
    }

    public Match getMatch() {
        return match;
    }

    public Player getPlayer() {
        return player;
    }

    public String getCardId() {
        return cardId;
    }

    public int getZoneIndex() {
        return zoneIndex;
    }

    public Unit getTarget() {
        return target;
    }

    /* Null until CardInHandHandler has run. */
    public Card getCard() {
        return card;
    }

    void setCard(Card card) {
        this.card = card;
    }
}
