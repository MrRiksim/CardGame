package game.server.cards;

import game.server.cards.spells.LightningStrike;
import game.server.cards.traps.BearTrap;
import game.server.cards.units.Knight;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * <b>Design pattern - Factory Method:</b> this is the real factory in the card
 * hierarchy (Card/Unit/Spell/Trap themselves are just abstraction, not a
 * factory - see the note in {@link Card}).
 *
 * <p>Every concrete card (see {@link Knight}, {@link LightningStrike},
 * {@link BearTrap} and friends) exposes its own static {@code create()}
 * method - a Factory Method - that knows how to build itself. This class is
 * the "creator" that calls one of those methods without needing to know the
 * concrete constructors. Adding a new card to the game means writing the
 * class and adding one line to {@link #CARD_POOL}; nothing else changes.
 *
 * <p>For now every card is equally likely and hands/draws simply pick at
 * random from the three example cards; a real deck-building system can
 * replace {@link #CARD_POOL} later without touching any calling code.
 */
public class CardFactory {

    private static final List<Supplier<Card>> CARD_POOL = List.of(
            Knight::create,
            LightningStrike::create,
            BearTrap::create
    );

    private CardFactory() {
    }

    public static Card createRandomCard() {
        int index = ThreadLocalRandom.current().nextInt(CARD_POOL.size());
        return CARD_POOL.get(index).get();
    }
}
