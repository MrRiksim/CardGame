package game.client;

import java.util.Comparator;
import java.util.List;

/**
 * <b>Design pattern - Iterator (Concrete Iterator 1):</b> visits every card
 * visible on screen, in reading order: your hand, your front row, your back
 * row, your graveyard, then the opponent's front row, revealed back row /
 * hand (spectator mode only) and graveyard. Rows are walked left to right as
 * they appear on the board.
 */
public class AllVisibleCardsIterator extends CyclicCardIterator {

    AllVisibleCardsIterator(
            List<ClientCard> ownHand,
            List<ClientUnit> ownFront,
            List<ClientTrap> ownBack,
            ClientCard ownGraveyardTop,
            List<ClientUnit> opponentFront,
            List<ClientTrap> opponentBackShown,
            List<ClientCard> opponentHandShown,
            ClientCard opponentGraveyardTop) {

        cards.addAll(ownHand);
        // Your zone 0 is drawn rightmost, so higher zone indexes come first left-to-right.
        ownFront.stream()
                .sorted(Comparator.comparingInt(ClientUnit::zoneIndex).reversed())
                .forEach(cards::add);
        ownBack.stream()
                .sorted(Comparator.comparingInt(ClientTrap::zoneIndex).reversed())
                .forEach(cards::add);
        addIfPresent(ownGraveyardTop);

        // The opponent's zone 0 is drawn leftmost.
        opponentFront.stream()
                .sorted(Comparator.comparingInt(ClientUnit::zoneIndex))
                .forEach(cards::add);
        if (opponentBackShown != null) {
            opponentBackShown.stream()
                    .sorted(Comparator.comparingInt(ClientTrap::zoneIndex))
                    .forEach(cards::add);
        }
        if (opponentHandShown != null) {
            cards.addAll(opponentHandShown);
        }
        addIfPresent(opponentGraveyardTop);
    }
}
