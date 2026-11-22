package game.client;

import java.util.Comparator;
import java.util.List;

/**
 * <b>Design pattern - Iterator (Concrete Iterator 2):</b> visits only the
 * viewer's own cards - hand, front row, back row and graveyard top - skipping
 * everything on the opponent's side. Handy for quickly reviewing your own
 * options during your turn.
 */
public class OwnCardsIterator extends CyclicCardIterator {

    OwnCardsIterator(
            List<ClientCard> ownHand,
            List<ClientUnit> ownFront,
            List<ClientTrap> ownBack,
            ClientCard ownGraveyardTop) {

        cards.addAll(ownHand);
        ownFront.stream()
                .sorted(Comparator.comparingInt(ClientUnit::zoneIndex).reversed())
                .forEach(cards::add);
        ownBack.stream()
                .sorted(Comparator.comparingInt(ClientTrap::zoneIndex).reversed())
                .forEach(cards::add);
        addIfPresent(ownGraveyardTop);
    }
}
