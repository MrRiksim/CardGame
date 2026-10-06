package game.server;

import game.server.cards.Card;
import game.server.cards.CardFactory;
import game.server.cards.Trap;
import game.server.cards.Unit;
import game.server.cards.Spell;
import game.server.cards.spells.ShiftingSpellCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds one player's game state for a {@link Match}: health, energy, hand,
 * board zones and graveyard. This is separate from {@link PlayerConnection},
 * which only knows how to talk to the socket - Player is the domain object,
 * PlayerConnection is purely the network transport.
 *
 * <p>The starting numbers (health, energy, hand size...) used to be fixed
 * constants here. Now that a match can be Standard, Speed or Chaos (see
 * {@link MatchBuilder}), they come in per-match as a {@link MatchSettings},
 * assembled once by whichever builder the match was created with.
 */
public class Player {

    public static final int NUMBER_OF_ZONES = 5;

    private final PlayerConnection connection;
    private final MatchSettings settings;

    private int health;
    private int energy;

    /*
     * Only meaningful when settings.hasTimer() - how much of this
     * player's clock was left as of the start of their current turn. See
     * Match's turn-clock methods for how this gets charged and read.
     */
    private long remainingTimeMillis;

    private final List<Card> hand = new ArrayList<>();
    private final List<Card> graveyard = new ArrayList<>();

    /*
     * Front row: units, visible to both players.
     * Back row: face-down traps, hidden from the opponent.
     */
    private final Unit[] frontZones = new Unit[NUMBER_OF_ZONES];
    private final Trap[] backZones = new Trap[NUMBER_OF_ZONES];

    public Player(PlayerConnection connection, MatchSettings settings) {
        this.connection = connection;
        this.settings = settings;
        this.health = settings.startingHealth();
        this.energy = settings.startingEnergy();
        this.remainingTimeMillis = settings.hasTimer() ? settings.timePerPlayerMillis() : 0;

        hand.add(ShiftingSpellCard.create());
        for (int i = 0; i < settings.startingHandSize(); i++) {
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
     * Called at the start of this player's turn: gains this match's
     * energy-per-turn and a card draw.
     */
    public void gainTurnResources() {
        energy += settings.energyPerTurn();
        drawCard();
    }

    public void drawCard() {
        if (hand.size() < settings.maxHandSize()) {
            hand.add(CardFactory.createRandomCard());
        }
    }

    public long getRemainingTimeMillis() {
        return remainingTimeMillis;
    }

    public void spendTime(long millis) {
        remainingTimeMillis = Math.max(0, remainingTimeMillis - millis);
    }

    public boolean isOutOfTime() {
        return settings.hasTimer() && remainingTimeMillis <= 0;
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
