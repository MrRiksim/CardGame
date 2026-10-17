package game.server.cards;

import game.server.Match;
import game.server.Player;

/**
 * The middle level for creators that make traps: it settles WHERE (the
 * owner's leftmost empty trap field) and leaves WHAT to a concrete creator.
 * The trap arrives face-down like any other, and for free.
 */
public abstract class TrapCreator extends CardCreator {

    @Override
    public abstract Trap createCard();

    @Override
    public final void spawn(Match match, Player owner) {
        int zoneIndex = match.findEmptyBackZone(owner);
        if (zoneIndex < 0) {
            return;
        }
        match.placeTrapForFree(owner, zoneIndex, createCard());
    }
}
