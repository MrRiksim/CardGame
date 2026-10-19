package game.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GameClient extends WebSocketClient {

    private final GameWindow window;

    private int myPlayerNumber = -1;
    private boolean hasConnected = false;

    public GameClient(URI serverUri, GameWindow window) {
        super(serverUri);
        this.window = window;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        hasConnected = true;
        System.out.println("Connected to server.");
        /*
         * No auto-queue anymore - the server waits for a JOIN|<mode>
         * message (see joinQueue) once the player picks a match type.
         */
        window.showModeSelect();
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Server: " + message);

        if (message.equals("ARCHER_TARGET_OK")) {
            window.showArcherCommandStatus("Archer target selected. You can undo before ending your turn.");
            return;
        }
        if (message.equals("ARCHER_TARGET_REJECTED")) {
            window.showArcherCommandStatus("Archer target selection rejected.");
            return;
        }
        if (message.equals("ARCHER_UNDO_OK")) {
            window.showArcherCommandStatus("Previous archer target restored.");
            return;
        }
        if (message.equals("ARCHER_UNDO_REJECTED")) {
            window.showArcherCommandStatus("No archer target selection can be undone now.");
            return;
        }

        if (message.equals("WAITING")) {
            window.setWaiting();
            return;
        }
        if (message.startsWith("MATCH:")) {
            handleMatchMessage(message);
            return;
        }
        if (message.startsWith("STATE|")) {
            handleStateMessage(message);
            return;
        }
        if (message.equals("YOU_WIN")) {
            window.showWin();
            return;
        }
        if (message.equals("YOU_LOSE")) {
            window.showLose();
        }
    }

    private void handleMatchMessage(String message) {
        try {
            String[] parts = message.split(":");
            if (parts.length != 3) {
                return;
            }
            int matchId = Integer.parseInt(parts[1]);

            if (parts[2].equals("PLAYER1")) {
                myPlayerNumber = 1;
            } else if (parts[2].equals("PLAYER2")) {
                myPlayerNumber = 2;
            } else {
                return;
            }

            System.out.println("You are Player " + myPlayerNumber);
            window.showMatch(matchId);
        } catch (NumberFormatException e) {
            System.err.println("Invalid match message: " + message);
        }
    }

    /*
     * STATE|matchId|turnPlayer|ownHealth|opponentHealth|ownEnergy|
     *       ownHand|opponentHandCount|ownFront|opponentFront|
     *       ownBack|opponentBackZones|ownSecondsLeft|opponentSecondsLeft|ownArcherTargets
     *
     * The two clock fields are "-" (parsed here as null) for a match with no
     * clock. See Match.buildStateFor (server) for the full format.
     */
    private void handleStateMessage(String message) {
        try {
            String[] parts = message.split("\\|", -1);
            if (parts.length != 14 && parts.length != 15) {
                return;
            }

            int matchId = Integer.parseInt(parts[1]);
            int turnPlayer = Integer.parseInt(parts[2]);
            int ownHealth = Integer.parseInt(parts[3]);
            int opponentHealth = Integer.parseInt(parts[4]);
            int ownEnergy = Integer.parseInt(parts[5]);
            List<ClientCard> ownHand = parseHand(parts[6]);
            int opponentHandCount = Integer.parseInt(parts[7]);
            List<ClientUnit> ownFront = parseFront(parts[8]);
            List<ClientUnit> opponentFront = parseFront(parts[9]);
            List<ClientTrap> ownBack = parseOwnBack(parts[10]);
            List<Integer> opponentBackZones = parseZoneList(parts[11]);
            Integer ownSecondsLeft = parseSeconds(parts[12]);
            Integer opponentSecondsLeft = parseSeconds(parts[13]);

            boolean yourTurn = turnPlayer == myPlayerNumber;
            Map<Integer, Integer> archerTargets = parts.length == 15
                    ? parseArcherTargets(parts[14]) : Map.of();

            window.updateGame(
                    matchId,
                    ownHealth,
                    opponentHealth,
                    ownEnergy,
                    ownHand,
                    opponentHandCount,
                    ownFront,
                    opponentFront,
                    ownBack,
                    opponentBackZones,
                    ownSecondsLeft,
                    opponentSecondsLeft,
                    yourTurn,
                    archerTargets
            );

        } catch (Exception e) {
            System.err.println("Invalid STATE message: " + message);
            e.printStackTrace();
        }
    }

    private Integer parseSeconds(String value) {
        return value.equals("-") ? null : Integer.parseInt(value);
    }

    private Map<Integer, Integer> parseArcherTargets(String data) {
        Map<Integer, Integer> targets = new HashMap<>();
        if (data.equals("-") || data.isEmpty()) {
            return targets;
        }
        for (String entry : data.split(";")) {
            String[] pair = entry.split(",");
            if (pair.length != 2) {
                throw new IllegalArgumentException("Invalid archer target entry");
            }
            int source = Integer.parseInt(pair[0]);
            int target = Integer.parseInt(pair[1]);
            if (source < 0 || source >= 5 || target < 0 || target >= 5) {
                throw new IllegalArgumentException("Invalid archer target slot");
            }
            targets.put(source, target);
        }
        return targets;
    }

    private List<ClientCard> parseHand(String data) {
        List<ClientCard> cards = new ArrayList<>();
        if (data.equals("-") || data.isEmpty()) {
            return cards;
        }

        for (String entry : data.split(";")) {
            String[] parts = entry.split(",");
            if (parts.length == 0) {
                continue;
            }

            switch (parts[0]) {
                case "UNIT" -> {
                    if (parts.length != 10) {
                        continue;
                    }
                    cards.add(new ClientUnit(
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3]),
                            parts[9],
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6]),
                            parseElement(parts[7]),
                            parts[8],
                            -1
                    ));
                }
                case "SPELL" -> {
                    if (parts.length != 6) {
                        continue;
                    }
                    cards.add(new ClientSpell(
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3]),
                            parts[5],
                            parseSpellType(parts[4])
                    ));
                }
                case "TRAP" -> {
                    if (parts.length != 6) {
                        continue;
                    }
                    cards.add(new ClientTrap(
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3]),
                            parts[5],
                            parts[4],
                            -1
                    ));
                }
                default -> {
                }
            }
        }
        return cards;
    }

    private List<ClientUnit> parseFront(String data) {
        List<ClientUnit> units = new ArrayList<>();
        if (data.equals("-") || data.isEmpty()) {
            return units;
        }

        for (String entry : data.split(";")) {
            String[] parts = entry.split(",");
            if (parts.length != 9) {
                continue;
            }

            units.add(new ClientUnit(
                    parts[1],
                    parts[2],
                    Integer.parseInt(parts[3]),
                    "",
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    parseElement(parts[7]),
                    parts[8],
                    Integer.parseInt(parts[0])
            ));
        }
        return units;
    }

    private List<ClientTrap> parseOwnBack(String data) {
        List<ClientTrap> traps = new ArrayList<>();
        if (data.equals("-") || data.isEmpty()) {
            return traps;
        }

        for (String entry : data.split(";")) {
            String[] parts = entry.split(",");
            if (parts.length != 6) {
                continue;
            }

            traps.add(new ClientTrap(
                    parts[1],
                    parts[2],
                    Integer.parseInt(parts[3]),
                    parts[5],
                    parts[4],
                    Integer.parseInt(parts[0])
            ));
        }
        return traps;
    }

    private List<Integer> parseZoneList(String data) {
        List<Integer> zones = new ArrayList<>();
        if (data.equals("-") || data.isEmpty()) {
            return zones;
        }

        for (String entry : data.split(",")) {
            zones.add(Integer.parseInt(entry));
        }
        return zones;
    }

    private Element parseElement(String value) {
        try {
            return Element.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Element.EARTH;
        }
    }

    private SpellType parseSpellType(String value) {
        try {
            return SpellType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return SpellType.SPECIAL;
        }
    }

    public void joinQueue(GameMode mode) {
        if (!isOpen()) {
            return;
        }
        send("JOIN|" + mode.name());
    }

    public void playUnit(String cardId, int zoneIndex) {
        if (!isOpen()) {
            return;
        }
        send("PLAY_UNIT|" + cardId + "|" + zoneIndex);
    }

    public void playSpell(String cardId, String targetUnitId) {
        if (!isOpen()) {
            return;
        }
        send("PLAY_SPELL|" + cardId + "|" + (targetUnitId == null ? "-" : targetUnitId));
    }

    public void playTrap(String cardId, int zoneIndex) {
        if (!isOpen()) {
            return;
        }
        send("PLAY_TRAP|" + cardId + "|" + zoneIndex);
    }

    public void endTurn() {
        if (!isOpen()) {
            return;
        }
        send("END_TURN");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from server.");
        if (hasConnected) {
            window.showDisconnected();
        }
    }

    @Override
    public void onError(Exception exception) {
        /*
         * A failed initial connection is an expected situation when the
         * server isn't running - don't print a stack trace for it.
         */
        if (!hasConnected) {
            window.showCannotConnect();
            return;
        }

        System.err.println("WebSocket error: " + exception.getMessage());
    }

    public void chooseArcherTarget(String archerId, int targetSlot) {
        if (isOpen()) {
            send("ARCHER_TARGET|" + archerId + "|" + targetSlot);
        }
    }

    public void undoArcherTarget() {
        if (isOpen()) {
            send("UNDO_ARCHER_TARGET");
        }
    }

    public static void main(String[] args) throws Exception {
        GameWindow window = new GameWindow();
        URI serverUri = new URI("ws://localhost:6767");
        GameClient client = new GameClient(serverUri, window);
        window.setClient(client);
        client.connect();
    }
}
