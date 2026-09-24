package game.client;

/**
 * Spells resolve immediately and never sit on the board, so unlike
 * ClientUnit/ClientTrap there's no zoneIndex to carry.
 */
public record ClientSpell(
        String id,
        String name,
        int energyCost,
        String description,
        SpellType spellType) implements ClientCard {

    @Override
    public CardKind kind() {
        return CardKind.SPELL;
    }
}
