package game.server.cards;

import game.server.Match;
import game.server.Player;

/**
 * The middle level for creators that make units: it settles WHERE (the
 * owner's leftmost empty unit field) and leaves WHAT to a concrete creator
 * such as SkellyCreator. spawn is final so every unit creator places cards
 * the same way - drop the modifier if one ever needs to place differently.
 */
public abstract class UnitCreator extends CardCreator {

    /* Narrowed from Card: the product always goes in a unit field. */
    @Override
    public abstract Unit createCard();

    @Override
    public final void spawn(Match match, Player owner) {
        int zoneIndex = match.findEmptyFrontZone(owner);
        if (zoneIndex < 0) {
            return;
        }
        match.placeUnitForFree(owner, zoneIndex, createCard());
    }
}