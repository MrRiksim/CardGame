package game.client;

/**
 * <b>Design pattern - Iterator (Aggregate interface):</b> anything that holds
 * cards and can hand out iterators over them. {@link BoardPanel} implements it:
 * its cards are spread over several lists and single fields (hand, front row,
 * back row, graveyards...), but callers only ever see a {@link ClientCardIterator}.
 */
public interface ClientCardAggregate {

    /* Every card currently visible on screen, both players' sides. */
    ClientCardIterator createAllCardsIterator();

    /* Only the viewer's own cards: hand, own front row, own back row, own graveyard. */
    ClientCardIterator createOwnCardsIterator();
}
