package game.client;

/**
 * <b>Design pattern - Iterator (Iterator interface):</b> walks through cards one
 * at a time, in both directions, without the caller knowing where or how the
 * cards are stored.
 *
 * <p>The iterator is cyclic: calling {@link #next()} on the last card wraps
 * around to the first one, and {@link #previous()} on the first card wraps to
 * the last. That suits the Prev / Next buttons in {@link CardInfoPanel}.
 */
public interface ClientCardIterator {

    /* True when there is nothing to iterate over at all. */
    boolean isEmpty();

    /* Same as hasNext() in java.util.Iterator - for a cyclic iterator it is true whenever it isn't empty. */
    boolean hasNext();

    ClientCard next();

    boolean hasPrevious();

    ClientCard previous();

    /*
     * Moves the cursor onto the card with this id, so that next()/previous()
     * continue from it. Returns false (and leaves the cursor where it was)
     * if the card isn't part of this iteration.
     */
    boolean seekTo(String cardId);

    /* 1-based position of the card the cursor is on, or 0 before the first next()/previous(). */
    int position();

    int size();
}
