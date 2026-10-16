package game.client;

/**
 * zoneIndex is -1 for a card still sitting in a hand; once placed, it's the
 * front-zone slot (0-4) this unit occupies.
 */
public record ClientUnit(
        String id,
        String name,
        int energyCost,
        String description,
        int health,
        int maxHealth,
        int damage,
        Element element,
        String clan,
        int zoneIndex,
        boolean healthBuffed,
        boolean damageBuffed,
        int shieldCount) implements ClientCard {

    /* Units in a hand or a graveyard never carry buffs, so existing callers don't change. */
    public ClientUnit(
            String id, String name, int energyCost, String description,
            int health, int maxHealth, int damage, Element element,
            String clan, int zoneIndex) {

        this(id, name, energyCost, description, health, maxHealth, damage, element, clan,
                zoneIndex, false, false, 0);
    }

    @Override
    public CardKind kind() {
        return CardKind.UNIT;
    }

    public boolean hasClan() {
        return clan != null && !clan.equals("NONE");
    }
}
