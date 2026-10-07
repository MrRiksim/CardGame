package game.server.cards;

import game.server.Match;
import game.server.Player;

/**
 * <b>Design pattern - Factory Method.</b> Creator = this class, factory
 * method = {@link #createUnit()}, ConcreteCreator = {@code SkellyCreator}
 * (see {@code game.server.cards.units.creators}).
 *
 * <p>{@link #spawn} is the part of the pattern that's easy to miss: the
 * Creator doesn't just expose a way to make a product, it also contains
 * the operation that USES that product - here, finding a unit's owner a
 * free zone and placing it there. That logic is identical no matter which
 * concrete creator you're holding, so it lives here once, is declared
 * {@code final} so no concrete creator can accidentally fork it, and never
 * needs to change when a new creator (say, one that picks randomly among
 * three units, or spawns conditionally) gets added later - only
 * {@link #createUnit()} varies per subclass.
 *
 * <p>This is not itself a card - nothing ever draws a UnitCreator into a
 * hand. A card like {@code Tombstone} holds one to decide what it spawns.
 */
public abstract class UnitCreator {

    public abstract Unit createUnit();

    /*
     * Places a freshly created unit in owner's leftmost empty front zone,
     * at no energy cost - or does nothing if the row is full. Shared by
     * every concrete creator; see Match.findEmptyFrontZone and
     * Match.placeUnitForFree for the board-side half of this.
     */
    public final void spawn(Match match, Player owner) {
        int zoneIndex = match.findEmptyFrontZone(owner);
        if (zoneIndex < 0) {
            return;
        }
        match.placeUnitForFree(owner, zoneIndex, createUnit());
    }
}
