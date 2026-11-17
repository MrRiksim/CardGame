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

        if (message.startsWith("CHAT|")) {
            // Limit the split so pipes, commas and semicolons in chat remain literal text.
            String[] parts = message.split("\\|", 3);
            if (parts.length == 3 && (parts[1].equals("1") || parts[1].equals("2"))) {
                int sender = Integer.parseInt(parts[1]);
                window.showChatMessage(sender == myPlayerNumber ? "You" : "Player " + sender, parts[2]);
            }
            return;
        }
        if (message.startsWith("CHAT_ERROR|")) {
            window.showChatMessage("System", message.substring("CHAT_ERROR|".length()));
            return;
        }
        if (message.startsWith("CHAT_INFO|")) {
            window.showChatMessage("System", message.substring("CHAT_INFO|".length()));
            return;
        }

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

        if (message.equals("SPELL_UNDO_OK")) {
            window.showArcherCommandStatus("Last spell undone.");
            return;
        }
        if (message.equals("SPELL_UNDO_REJECTED")) {
            window.showArcherCommandStatus("No spell can be undone.");
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
            return;
        }

        if (message.startsWith("MATCHES|")) {
            handleMatchList(message);
            return;
        }
        if (message.startsWith("SPECTATING|")) {
            window.showSpectating(Integer.parseInt(message.substring("SPECTATING|".length())));
            return;
        }
        if (message.equals("SPECTATE_REJECTED")) {
            window.showSpectateRejected();
            return;
        }
        if (message.startsWith("SPECTATE_STATE|")) {
            handleSpectatorState(message);
            return;
        }
        if (message.startsWith("MATCH_OVER|")) {
            window.showMatchOver(Integer.parseInt(message.substring("MATCH_OVER|".length())));
        }
    }

    /* MATCHES|id,MODE,turnNumber;...  or  MATCHES|- */
    private void handleMatchList(String message) {
        List<MatchListing> matches = new ArrayList<>();
        String data = message.substring("MATCHES|".length());
        if (!data.equals("-") && !data.isEmpty()) {
            for (String entry : data.split(";")) {
                String[] parts = entry.split(",");
                if (parts.length == 3) {
                    matches.add(new MatchListing(
                            Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2])));
                }
            }
        }
        window.showMatchList(matches);
    }

    /*
     * SPECTATE_STATE|matchId|turnPlayer|p1Health|p2Health|p1HandCount|p2HandCount|
     *                p1Hand|p2Hand|p1Front|p2Front|p1Back|p2Back|p1BackZones|p2BackZones|
     *                p1SecondsLeft|p2SecondsLeft|p1GraveyardTop|p2GraveyardTop
     *
     * A hand or back row of "?" was hidden by the server and becomes null.
     * See SpectatorStatePacket (server) for the full format.
     */
    private void handleSpectatorState(String message) {
        try {
            String[] parts = message.split("\\|", -1);
            if (parts.length != 19) {
                return;
            }

            window.updateSpectator(new SpectatorState(
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    parts[7].equals("?") ? null : parseHand(parts[7]),
                    parts[8].equals("?") ? null : parseHand(parts[8]),
                    parseFront(parts[9]),
                    parseFront(parts[10]),
                    parts[11].equals("?") ? null : parseOwnBack(parts[11]),
                    parts[12].equals("?") ? null : parseOwnBack(parts[12]),
                    parseZoneList(parts[13]),
                    parseZoneList(parts[14]),
                    parseSeconds(parts[15]),
                    parseSeconds(parts[16]),
                    parseGraveyardTop(parts[17]),
                    parseGraveyardTop(parts[18])
            ));
        } catch (Exception e) {
            System.err.println("Invalid SPECTATE_STATE message: " + message);
            e.printStackTrace();
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
            if (parts.length != 16 && parts.length != 17) {
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
            ClientCard ownGraveyardTop = parseGraveyardTop(parts[14]);
            ClientCard opponentGraveyardTop = parseGraveyardTop(parts[15]);

            boolean yourTurn = turnPlayer == myPlayerNumber;
            Map<Integer, Integer> archerTargets = parts.length == 17
                    ? parseArcherTargets(parts[16]) : Map.of();

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
                    ownGraveyardTop,
                    opponentGraveyardTop,
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
            // Description is the last field and may itself contain commas.
            int fieldCount = entry.startsWith("UNIT,") ? 10 : 6;
            String[] parts = entry.split(",", fieldCount);
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

            if (parts.length != 13) {
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
                    Integer.parseInt(parts[0]),
                    parts[9].equals("1"),
                    parts[10].equals("1"),
                    Integer.parseInt(parts[11]),
                    Integer.parseInt(parts[12])
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
            String[] parts = entry.split(",", 6);
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

    private ClientCard parseGraveyardTop(String data) {
        List<ClientCard> cards = parseHand(data);
        return cards.isEmpty() ? null : cards.get(0);
    }

    public boolean sendChat(String text) {
        if (!isOpen()) {
            return false;
        }
        try {
            send("CHAT|" + text);
            return true;
        } catch (org.java_websocket.exceptions.WebsocketNotConnectedException exception) {
            return false;
        }
    }

    public void joinQueue(GameMode mode) {
        if (!isOpen()) {
            return;
        }
        send("JOIN|" + mode.name());
    }

    public void requestMatchList() {
        if (!isOpen()) {
            return;
        }
        send("LIST_MATCHES");
    }

    public void spectate(int matchId) {
        if (!isOpen()) {
            return;
        }
        send("SPECTATE|" + matchId);
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

    public void attachRider(String cardId, String targetUnitId) {
        if (!isOpen()) {
            return;
        }
        send("ATTACH|" + cardId + "|" + targetUnitId);
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

    public void undoSpell() {
        if (isOpen()) {
            send("UNDO_SPELL");
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
