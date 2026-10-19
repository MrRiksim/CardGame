package game.server.cards;

import game.server.Match;
import game.server.Player;

/**
 * <b>Design pattern - Factory Method</b>, with the Creator spread over three
 * levels because there are two independent questions about a spawned card:
 *
 * <ul>
 *   <li><b>What gets made</b> - {@link #createCard()}, the factory method.</li>
 *   <li><b>Where it goes</b> - {@link #spawn}, which uses whatever the
 *       factory method returns.</li>
 * </ul>
 *
 * <p>This class declares both and answers neither. {@link UnitCreator},
 * {@link TrapCreator} and {@link SpellCreator} each answer the second one
 * for one kind of card (a unit field, a trap field, the hand), and a
 * concrete creator such as {@code SkellyCreator} answers the first. So a new
 * spawner only ever has to say what it makes - how and where it is placed
 * is already settled by the level above it.
 *
 * <p>Nothing here is a card: a creator is held by a card (see
 * {@code Tombstone}) that calls {@link #spawn} when its owner's turn
 * starts. Everything spawned this way is free - no energy is spent and
 * nothing comes out of a hand or deck.
 */
public abstract class CardCreator {

    public abstract Card createCard();

    /*
     * Creates a card and puts it where this kind of creator puts cards, or
     * does nothing if there's no room. Implementations should check for room
     * BEFORE calling createCard(), so a creator whose createCard() is
     * random or has side effects never makes a card it can't place.
     */
    public abstract void spawn(Match match, Player owner);
}
