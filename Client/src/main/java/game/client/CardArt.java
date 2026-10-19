package game.client;

import java.util.Map;

/**
 * Maps a card's kind + name (the only things the server tells us about a
 * card) to where its artwork lives under assets/.
 *
 * Named cards get an explicit entry (kept in sync with the file names
 * used server-side when those cards were written); anything not listed
 * falls back to slugifying the name, so future cards work without needing
 * an entry here unless their art file name needs to be something else.
 */
public final class CardArt {

    private static final Map<String, String> FILE_NAME_OVERRIDES = Map.ofEntries(
            Map.entry("Knight", "knight.png"),
            Map.entry("Lightning Strike", "lightning_strike.png"),
            Map.entry("Bear Trap", "bear_trap.png"),
            Map.entry("Health Boost", "health_boost.png"),
            Map.entry("Damage Potion", "damage_potion.png"),
            Map.entry("Shifting Spell", "shifting_spell.png"),
            Map.entry("Skelly", "skelly.png"),
            Map.entry("Tombstone", "tombstone.png"),
            Map.entry("Cripple", "cripple.png"),
            Map.entry("Shield", "shield.png"),
            Map.entry("Phantom Mirror", "Phantom_Mirror_Spell.png"),
            Map.entry("Essence Replication", "Essence_Replication_Spell.png")
    );

    private CardArt() {
    }

    public static String getImagePath(CardKind kind, String cardName) {
        String fileName = FILE_NAME_OVERRIDES.getOrDefault(cardName, slugify(cardName) + ".png");
        return "assets/" + folderFor(kind) + "/" + fileName;
    }

    private static String folderFor(CardKind kind) {
        return switch (kind) {
            case UNIT -> "units";
            case SPELL -> "spells";
            case TRAP -> "traps";
        };
    }

    private static String slugify(String name) {
        return name.toLowerCase().trim().replaceAll("[^a-z0-9]+", "_");
    }
}
