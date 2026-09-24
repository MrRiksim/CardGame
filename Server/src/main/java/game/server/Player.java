package game.server;

import game.server.cards.Card;
import game.server.cards.CardFactory;
import game.server.cards.Trap;
import game.server.cards.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds one player's game state for a {@link Match}: health, energy, hand,
 * board zones and graveyard. This is separate from {@link PlayerConnection},
 * which only knows how to talk to the socket - Player is the domain object,
 * PlayerConnection is purely the network transport.
 */
public class Player {

    public static final int STARTING_HEALTH = 25;
    public static final int STARTING_ENERGY = 6;
    public static final int STARTING_HAND_SIZE = 5;
    public static final int MAX_HAND_SIZE = 7;
    public static final int ENERGY_PER_TURN = 2;
    public static final int NUMBER_OF_ZONES = 5;

    private final PlayerConnection connection;

    private int health = STARTING_HEALTH;
    private int energy = STARTING_ENERGY;

    private final List<Card> hand = new ArrayList<>();
    private final List<Card> graveyard = new ArrayList<>();

    /*
     * Front row: units, visible to both players.
     * Back row: face-down traps, hidden from the opponent.
     */
    private final Unit[] frontZones = new Unit[NUMBER_OF_ZONES];
    private final Trap[] backZones = new Trap[NUMBER_OF_ZONES];

    public Player(PlayerConnection connection) {
        this.connection = connection;

        for (int i = 0; i < STARTING_HAND_SIZE; i++) {
            hand.add(CardFactory.createRandomCard());
        }
    }

    public void send(String message) {
        connection.send(message);
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public PlayerConnection getConnection() {
        return connection;
    }

    public int getHealth() {
        return health;
    }

    public int getEnergy() {
        return energy;
    }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public boolean isDefeated() {
        return health <= 0;
    }

    public boolean spendEnergy(int amount) {
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        return true;
    }

    /*
     * Called at the start of this player's turn: +2 energy and a card draw.
     */
    public void gainTurnResources() {
        energy += ENERGY_PER_TURN;
        drawCard();
    }

    public void drawCard() {
        if (hand.size() < MAX_HAND_SIZE) {
            hand.add(CardFactory.createRandomCard());
        }
    }

    public List<Card> getHand() {
        return hand;
    }

    public Card findInHand(String cardId) {
        for (Card card : hand) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        return null;
    }

    public Unit[] getFrontZones() {
        return frontZones;
    }

    public Trap[] getBackZones() {
        return backZones;
    }

    public List<Card> getGraveyard() {
        return graveyard;
    }

    public void sendToGraveyard(Card card) {
        graveyard.add(card);
    }
}
