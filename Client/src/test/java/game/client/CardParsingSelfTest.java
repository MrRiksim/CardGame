package game.client;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

/** Regression checks for descriptions containing protocol field delimiters. */
public final class CardParsingSelfTest {
    public static void main(String[] args) throws Exception {
        GameClient client = new GameClient(URI.create("ws://localhost:6767"), null);
        Method parseHand = GameClient.class.getDeclaredMethod("parseHand", String.class);
        parseHand.setAccessible(true);

        String[] descriptions = {
                "Deals 1 damage to up to 3 enemies, starting at the target and wrapping right. Cannot be undone.",
                "Heals up to 3 injured, unbuffed allies by 1 health each, weakest first. Cannot be undone.",
                "Returns up to 2 newest unit cards from your graveyard to your hand, if there is room. Cannot be undone."
        };
        String[] names = {"Chain Lightning", "Healing Wave", "Raise the Fallen"};
        String packet = "";
        for (int i = 0; i < names.length; i++) {
            if (i > 0) packet += ";";
            packet += "SPELL,s" + i + "," + names[i] + ",2,"
                    + (i == 0 ? "DAMAGE" : "SPECIAL") + "," + descriptions[i];
        }
        List<?> hand = (List<?>) parseHand.invoke(client, packet);
        check(hand.size() == 3, "All three new spells must appear in the hand");
        for (int i = 0; i < names.length; i++) {
            ClientCard card = (ClientCard) hand.get(i);
            check(card.name().equals(names[i]) && card.description().equals(descriptions[i]),
                    "Spell name and complete description must survive parsing");
        }

        List<?> mixed = (List<?>) parseHand.invoke(client,
                "UNIT,u,Knight,2,3,3,1,EARTH,NONE,Sturdy, brave, and strong;"
                + "TRAP,t,Bear Trap,1,ON_UNIT_PLAY,Wait, then strike;"
                + "SPELL,s,Heal,1,SPECIAL,");
        check(mixed.size() == 3, "Units, traps, and empty descriptions must also parse");
        check(((ClientCard) mixed.get(0)).description().equals("Sturdy, brave, and strong"), "Unit commas");
        check(((ClientCard) mixed.get(1)).description().equals("Wait, then strike"), "Trap commas");
        check(((ClientCard) mixed.get(2)).description().isEmpty(), "Empty description");
        check(((List<?>) parseHand.invoke(client, "-")).isEmpty(), "Empty hand marker");

        Method parseBack = GameClient.class.getDeclaredMethod("parseOwnBack", String.class);
        parseBack.setAccessible(true);
        List<?> back = (List<?>) parseBack.invoke(client, "0,t,Bear Trap,1,ON_UNIT_PLAY,Wait, then strike");
        check(back.size() == 1 && ((ClientTrap) back.get(0)).description().equals("Wait, then strike"),
                "Placed traps must preserve descriptions too");

        Method parseGraveyard = GameClient.class.getDeclaredMethod("parseGraveyardTop", String.class);
        parseGraveyard.setAccessible(true);
        check(parseGraveyard.invoke(client, packet.split(";")[0]) instanceof ClientSpell,
                "Graveyard cards use the same parser and must remain visible");
        System.out.println("All card parsing regression checks passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
