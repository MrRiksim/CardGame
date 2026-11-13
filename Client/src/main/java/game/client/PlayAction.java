package game.client;

/**
 * What the player is trying to do after dropping a card. BoardPanel figures
 * out which of these a drop represents (based on the dragged card's kind and
 * what's under the cursor); GameWindow just pattern-matches on it and calls
 * the matching GameClient method.
 */
public sealed interface PlayAction {

    record UnitPlacement(String cardId, int zoneIndex) implements PlayAction {
    }

    record TrapPlacement(String cardId, int zoneIndex) implements PlayAction {
    }

    /*
     * targetUnitId is null for an untargeted (SPECIAL) spell cast.
     */
    record SpellCast(String cardId, String targetUnitId) implements PlayAction {
    }

    /*
     * A unit card dropped on one of your own units, to ride it. Only the
     * server knows which cards can ride (see Jumpy) - any other unit is
     * rejected there and snaps back.
     */
    record RiderAttach(String cardId, String targetUnitId) implements PlayAction {
    }

    record ArcherTarget(String archerId, int targetSlot) implements PlayAction {
    }

    record UndoArcherTarget() implements PlayAction {
    }
}
