package game.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Match {

    public static final int NUMBER_OF_ZONES = 5;

    private final int id;
    private final PlayerConnection player1;
    private final PlayerConnection player2;

    private final List<Card> player1Hand = new ArrayList<>();
    private final List<Card> player2Hand = new ArrayList<>();

    /*
     * Each map represents the five spaces of a
     * particular card type.
     *
     * key   = zone index (0-4)
     * value = card currently occupying the zone
     */
    private final Map<Integer, Card> player1BrownZones = new HashMap<>();
    private final Map<Integer, Card> player1BlueZones = new HashMap<>();
    private final Map<Integer, Card> player2BrownZones = new HashMap<>();
    private final Map<Integer, Card> player2BlueZones = new HashMap<>();

    private PlayerConnection currentTurn;
    private boolean active = true;

    public Match(int id, PlayerConnection player1, PlayerConnection player2) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;

        /*
         * Give each player:
         *
         * 2 light brown cards
         * 2 light blue cards
         */
        createStartingCards(player1Hand, "P1");
        createStartingCards(player2Hand, "P2");

        /*
         * Randomly select who goes first.
         */
        currentTurn = ThreadLocalRandom.current().nextBoolean() ? player1 : player2;
    }

    private void createStartingCards(List<Card> hand, String playerPrefix) {
        hand.add(new Card(playerPrefix + "-B1", CardType.LIGHT_BROWN));
        hand.add(new Card(playerPrefix + "-B2", CardType.LIGHT_BROWN));
        hand.add(new Card(playerPrefix + "-L1", CardType.LIGHT_BLUE));
        hand.add(new Card(playerPrefix + "-L2", CardType.LIGHT_BLUE));
    }

    /*
     * Attempts to place or move a card.
     *
     * Returns true if the operation was successful.
     */
    public synchronized boolean moveCard(
            PlayerConnection player, String cardId, CardType targetType, int targetZone) {

        if (!active) {
            return false;
        }
        /*
         * Only the player whose turn it is may play cards.
         */
        if (currentTurn != player) {
            return false;
        }
        if (targetZone < 0 || targetZone >= NUMBER_OF_ZONES) {
            return false;
        }

        /*
         * Cards that can be moved are ONLY cards
         * currently in the player's hand.
         *
         * Played cards cannot be moved.
         */
        List<Card> hand = getHand(player);
        Card card = findCard(hand, cardId);
        if (card == null) {
            return false;
        }

        /*
         * Make sure the card is being placed into a
         * zone corresponding to its type.
         */
        if (card.getType() != targetType) {
            return false;
        }

        Map<Integer, Card> targetZones = targetType == CardType.LIGHT_BROWN
                ? getBrownZones(player)
                : getBlueZones(player);

        /*
         * A zone may only contain one card.
         */
        if (targetZones.containsKey(targetZone)) {
            return false;
        }

        /*
         * Move the card from the hand to the field.
         */
        hand.remove(card);
        targetZones.put(targetZone, card);
        card.setZoneIndex(targetZone);

        System.out.println("Player " + getPlayerNumber(player) + " played " + card.getId() + " to " + targetType + " zone " + targetZone);

        return true;
    }

    private Card findCard(List<Card> cards, String cardId) {
        for (Card card : cards) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        return null;
    }

    private Card findCard(Map<Integer, Card> zones, String cardId) {
        for (Card card : zones.values()) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        return null;
    }

    public synchronized boolean endTurn(PlayerConnection player) {
        if (!active) {
            return false;
        }
        if (currentTurn != player) {
            return false;
        }
        currentTurn = getOpponent(player);
        return true;
    }

    /*
     * Builds a state from the perspective of a
     * particular player.
     *
     * The player gets their complete hand.
     * The opponent only gets the number of cards
     * remaining in their hand.
     */
    public synchronized String buildStateFor(PlayerConnection viewer) {
        List<Card> ownHand = getHand(viewer);
        List<Card> opponentHand = getHand(getOpponent(viewer));
        Map<Integer, Card> ownBrown = getBrownZones(viewer);
        Map<Integer, Card> ownBlue = getBlueZones(viewer);
        Map<Integer, Card> opponentBrown = getBrownZones(getOpponent(viewer));
        Map<Integer, Card> opponentBlue = getBlueZones(getOpponent(viewer));

        return "STATE|" + id
                + "|" + getCurrentTurnPlayerNumber()
                + "|" + serializeHand(ownHand)
                + "|" + opponentHand.size()
                + "|" + serializePlayedCards(ownBrown, ownBlue)
                + "|" + serializePlayedCards(opponentBrown, opponentBlue);
    }

    private String serializeHand(List<Card> hand) {
        if (hand.isEmpty()) {
            return "-";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            if (i > 0) {
                result.append(";");
            }
            Card card = hand.get(i);
            result.append(card.getId()).append(",").append(card.getType());
        }
        return result.toString();
    }

    private String serializePlayedCards(Map<Integer, Card> brownZones, Map<Integer, Card> blueZones) {
        StringBuilder result = new StringBuilder();

        for (int zone = 0; zone < NUMBER_OF_ZONES; zone++) {
            Card brownCard = brownZones.get(zone);
            if (brownCard != null) {
                appendPlayedCard(result, brownCard, "BROWN", zone);
            }
            Card blueCard = blueZones.get(zone);
            if (blueCard != null) {
                appendPlayedCard(result, blueCard, "BLUE", zone);
            }
        }

        if (result.isEmpty()) {
            return "-";
        }
        return result.toString();
    }

    private void appendPlayedCard(StringBuilder result, Card card, String zoneType, int zone) {
        if (!result.isEmpty()) {
            result.append(";");
        }
        result.append(card.getId()).append(",")
                .append(card.getType() == CardType.LIGHT_BROWN ? "BROWN" : "BLUE")
                .append(",").append(zone);
    }

    public synchronized PlayerConnection getOpponent(PlayerConnection player) {
        if (player == player1) {
            return player2;
        }
        if (player == player2) {
            return player1;
        }
        return null;
    }

    public synchronized int getPlayerNumber(PlayerConnection player) {
        if (player == player1) {
            return 1;
        }
        if (player == player2) {
            return 2;
        }
        return -1;
    }

    public synchronized int getCurrentTurnPlayerNumber() {
        return getPlayerNumber(currentTurn);
    }

    private List<Card> getHand(PlayerConnection player) {
        return player == player1 ? player1Hand : player2Hand;
    }

    private Map<Integer, Card> getBrownZones(PlayerConnection player) {
        return player == player1 ? player1BrownZones : player2BrownZones;
    }

    private Map<Integer, Card> getBlueZones(PlayerConnection player) {
        return player == player1 ? player1BlueZones : player2BlueZones;
    }

    public synchronized boolean contains(PlayerConnection player) {
        return player == player1 || player == player2;
    }

    public synchronized void endMatch() {
        active = false;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized int getId() {
        return id;
    }

    public PlayerConnection getPlayer1() {
        return player1;
    }

    public PlayerConnection getPlayer2() {
        return player2;
    }
}
