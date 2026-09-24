package game.client;

/**
 * zoneIndex is -1 for a card still sitting in a hand; once placed, it's the
 * back-zone slot (0-4) this trap occupies. The server only ever sends us a
 * ClientTrap with real data for our OWN back zones - an opponent's occupied
 * slots arrive as bare zone indices (see BoardPanel.updateState), never as
 * a ClientTrap, so there's no way for this class to leak a hidden trap.
 */
public record ClientTrap(
        String id,
        String name,
        int energyCost,
        String description,
        String activationCondition,
        int zoneIndex) implements ClientCard {

    @Override
    public CardKind kind() {
        return CardKind.TRAP;
    }
}
