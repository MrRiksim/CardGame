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
}
