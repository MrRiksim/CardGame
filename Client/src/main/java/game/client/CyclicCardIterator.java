package game.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared cursor logic for the concrete card iterators. Each subclass only
 * decides WHICH cards it visits and in what order (in its constructor); moving
 * forward/backward, wrapping around and seeking are the same for all of them.
 *
 * <p>The cards are copied when the iterator is created, so a server state
 * update arriving mid-iteration can't break the cursor - the mediator simply
 * creates a fresh iterator on every Prev/Next press.
 */
public abstract class CyclicCardIterator implements ClientCardIterator {

    protected final List<ClientCard> cards = new ArrayList<>();
    private int index = -1;

    /* Helper for subclasses: adds a single, possibly-null card (e.g. a graveyard top). */
    protected void addIfPresent(ClientCard card) {
        if (card != null) {
            cards.add(card);
        }
    }

    @Override
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    @Override
    public boolean hasNext() {
        return !cards.isEmpty();
    }

    @Override
    public ClientCard next() {
        if (cards.isEmpty()) {
            return null;
        }
        index = (index + 1) % cards.size();
        return cards.get(index);
    }

    @Override
    public boolean hasPrevious() {
        return !cards.isEmpty();
    }

    @Override
    public ClientCard previous() {
        if (cards.isEmpty()) {
            return null;
        }
        index = index <= 0 ? cards.size() - 1 : index - 1;
        return cards.get(index);
    }

    @Override
    public boolean seekTo(String cardId) {
        if (cardId == null) {
            return false;
        }
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).id().equals(cardId)) {
                index = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public int position() {
        return index + 1;
    }

    @Override
    public int size() {
        return cards.size();
    }
}
