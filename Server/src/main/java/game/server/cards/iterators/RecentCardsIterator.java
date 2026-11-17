package game.server.cards.iterators;

import game.server.cards.Card;
import game.server.cards.Unit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Iterator: visits graveyard unit cards newest first, skipping spells and traps.
 * A snapshot allows the caller to transfer returned cards out of the graveyard.
 */
public final class RecentCardsIterator implements Iterator<Unit> {
    private final List<Card> cards;
    private int index;

    public RecentCardsIterator(List<Card> graveyard) {
        cards = new ArrayList<>(graveyard);
        index = cards.size() - 1;
    }

    @Override
    public boolean hasNext() {
        while (index >= 0 && !(cards.get(index) instanceof Unit)) {
            index--;
        }
        return index >= 0;
    }

    @Override
    public Unit next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return (Unit) cards.get(index--);
    }
}
