package game.server;

import java.util.ArrayList;
import java.util.List;

public final class ChatRoutingSelfTest {
    public static void main(String[] args) {
        MatchmakingService service = new MatchmakingService();
        DummyConnection first = new DummyConnection();
        DummyConnection second = new DummyConnection();
        DummyConnection third = new DummyConnection();
        DummyConnection fourth = new DummyConnection();
        DummyConnection spectator = new DummyConnection();
        service.handleMessage(first, "JOIN|STANDARD");
        service.handleMessage(second, "JOIN|STANDARD");
        service.handleMessage(third, "JOIN|STANDARD");
        service.handleMessage(fourth, "JOIN|STANDARD");
        service.handleMessage(spectator, "SPECTATE|1");
        clear(first, second, third, fourth, spectator);

        String text = "Hello | opponent, good luck; 🙂";
        service.handleMessage(first, "CHAT|" + text);
        check(first.messages.equals(List.of("CHAT|1|" + text))
                && second.messages.equals(first.messages), "Echo and delivery must preserve literal punctuation and Unicode");
        check(third.messages.isEmpty() && fourth.messages.isEmpty() && spectator.messages.isEmpty(),
                "Chat must stay private to the two players in its match");

        clear(first, second);
        service.handleMessage(second, "CHAT|END_TURN|PLAYER1");
        check(first.messages.equals(List.of("CHAT|2|END_TURN|PLAYER1")) && second.messages.equals(first.messages),
                "Either player may chat; text must not spoof identity or execute a game command");

        clear(first, second);
        service.handleMessage(first, "CHAT|   \n\t");
        check(first.messages.isEmpty() && second.messages.isEmpty(), "Blank chat must be ignored");
        service.handleMessage(first, "CHAT|" + "x".repeat(301));
        check(first.messages.size() == 1 && first.messages.get(0).startsWith("CHAT_ERROR|")
                && second.messages.isEmpty(), "Oversized chat must be rejected only to its sender");

        clear(first, second);
        service.handleMessage(first, "CHAT|  one\ntwo\rthree\tfour  ");
        check(first.messages.equals(List.of("CHAT|1|one two three four")), "Chat must remain on one transcript line");
        clear(first, second);
        service.handleMessage(second, "CHAT|" + "x".repeat(300));
        check(first.messages.size() == 1 && first.messages.get(0).length() == "CHAT|2|".length() + 300,
                "Maximum-length chat must be accepted");

        service.handleMessage(spectator, "CHAT|spectator text");
        check(spectator.messages.size() == 1 && spectator.messages.get(0).startsWith("CHAT_ERROR|"),
                "Spectators must not post in player chat");
        DummyConnection waiting = new DummyConnection();
        service.handleMessage(waiting, "JOIN|STANDARD");
        waiting.messages.clear();
        service.handleMessage(waiting, "CHAT|waiting text");
        check(waiting.messages.size() == 1 && waiting.messages.get(0).startsWith("CHAT_ERROR|"),
                "Queued players must not post to a match");

        service.removePlayerFromMatch(first);
        clear(first, second);
        service.handleMessage(second, "CHAT|after match");
        check(first.messages.isEmpty() && second.messages.size() == 1
                && second.messages.get(0).startsWith("CHAT_ERROR|"), "Ended matches must disable chat");
        System.out.println("All match chat routing checks passed.");
    }

    private static void clear(DummyConnection... connections) {
        for (DummyConnection connection : connections) connection.messages.clear();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class DummyConnection extends PlayerConnection {
        private final List<String> messages = new ArrayList<>();
        DummyConnection() { super(null); }
        @Override public void send(String message) { messages.add(message); }
        @Override public boolean isConnected() { return true; }
    }
}
