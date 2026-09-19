package game.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GameClient extends WebSocketClient {

    private final GameWindow window;

    private int myPlayerNumber = -1;

    private boolean hasConnected = false;

    public GameClient(
            URI serverUri,
            GameWindow window) {

        super(serverUri);

        this.window = window;
    }

    @Override
    public void onOpen(
            ServerHandshake handshake) {

        hasConnected = true;

        System.out.println(
                "Connected to server."
        );

        window.setWaiting();
    }

    @Override
    public void onMessage(
            String message) {

        System.out.println(
                "Server: " + message
        );

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
        }
    }

    private void handleMatchMessage(
            String message) {

        try {

            String[] parts =
                    message.split(":");

            if (parts.length != 3) {
                return;
            }

            int matchId =
                    Integer.parseInt(parts[1]);

            if (parts[2].equals("PLAYER1")) {

                myPlayerNumber = 1;

            } else if (parts[2].equals("PLAYER2")) {

                myPlayerNumber = 2;

            } else {

                return;
            }

            System.out.println(
                    "You are Player "
                            + myPlayerNumber
            );

            window.showMatch(matchId);

        } catch (NumberFormatException e) {

            System.err.println(
                    "Invalid match message: "
                            + message
            );
        }
    }

    private void handleStateMessage(
            String message) {

        try {

            /*
             * STATE
             *   1 = match ID
             *   2 = turn player
             *   3 = own hand
             *   4 = opponent hand count
             *   5 = own played
             *   6 = opponent played
             */
            String[] parts =
                    message.split(
                            "\\|",
                            -1
                    );

            if (parts.length != 7) {
                return;
            }

            int matchId =
                    Integer.parseInt(parts[1]);

            int turnPlayer =
                    Integer.parseInt(parts[2]);

            List<ClientCard> ownHand =
                    parseHand(parts[3]);

            int opponentHandCount =
                    Integer.parseInt(parts[4]);

            List<ClientCard> ownPlayed =
                    parsePlayedCards(parts[5]);

            List<ClientCard> opponentPlayed =
                    parsePlayedCards(parts[6]);

            boolean yourTurn =
                    turnPlayer == myPlayerNumber;

            window.updateGame(
                    matchId,
                    ownHand,
                    opponentHandCount,
                    ownPlayed,
                    opponentPlayed,
                    yourTurn
            );

        } catch (Exception e) {

            System.err.println(
                    "Invalid STATE message: "
                            + message
            );

            e.printStackTrace();
        }
    }

    private List<ClientCard> parseHand(
            String data) {

        List<ClientCard> cards =
                new ArrayList<>();

        if (data.equals("-") ||
                data.isEmpty()) {

            return cards;
        }

        String[] cardEntries =
                data.split(";");

        for (String entry :
                cardEntries) {

            String[] parts =
                    entry.split(",");

            if (parts.length != 2) {
                continue;
            }

            CardType type =
                    parseCardType(parts[1]);

            if (type == null) {
                continue;
            }

            cards.add(
                    ClientCard.handCard(
                            parts[0],
                            type
                    )
            );
        }

        return cards;
    }

    private List<ClientCard> parsePlayedCards(
            String data) {

        List<ClientCard> cards =
                new ArrayList<>();

        if (data.equals("-") ||
                data.isEmpty()) {

            return cards;
        }

        String[] cardEntries =
                data.split(";");

        for (String entry :
                cardEntries) {

            String[] parts =
                    entry.split(",");

            if (parts.length != 3) {
                continue;
            }

            CardType type =
                    parseCardType(parts[1]);

            if (type == null) {
                continue;
            }

            int zone =
                    Integer.parseInt(parts[2]);

            cards.add(
                    ClientCard.playedCard(
                            parts[0],
                            type,
                            zone
                    )
            );
        }

        return cards;
    }

    private CardType parseCardType(
            String value) {

        if (value.equals("BROWN")) {
            return CardType.LIGHT_BROWN;
        }

        if (value.equals("BLUE")) {
            return CardType.LIGHT_BLUE;
        }

        /*
         * Server enum names.
         */
        if (value.equals("LIGHT_BROWN")) {
            return CardType.LIGHT_BROWN;
        }

        if (value.equals("LIGHT_BLUE")) {
            return CardType.LIGHT_BLUE;
        }

        return null;
    }

    public void moveCard(
            BoardPanel.CardMove move) {

        if (!isOpen()) {
            return;
        }

        String type =
                move.targetType() ==
                        BoardPanel.DropZoneType.BROWN
                        ? "BROWN"
                        : "BLUE";

        send(
                "MOVE_CARD|"
                        + move.cardId()
                        + "|"
                        + type
                        + "|"
                        + move.targetZone()
        );
    }

    public void endTurn() {

        if (!isOpen()) {
            return;
        }

        send("END_TURN");
    }

    @Override
    public void onClose(
            int code,
            String reason,
            boolean remote) {

        System.out.println(
                "Disconnected from server."
        );

        if (hasConnected) {

            window.showDisconnected();
        }
    }

    @Override
    public void onError(
            Exception exception) {

        /*
         * A failed initial connection is an expected
         * situation when the server isn't running.
         *
         * Do not print a stack trace for it.
         */
        if (!hasConnected) {

            window.showCannotConnect();

            return;
        }

        /*
         * Once we have successfully connected, errors
         * are unexpected and useful to see while developing.
         */
        System.err.println(
                "WebSocket error: "
                        + exception.getMessage()
        );
    }

    public static void main(
            String[] args)
            throws Exception {

        GameWindow window =
                new GameWindow();

        URI serverUri =
                new URI(
                        "ws://localhost:6767"
                );

        GameClient client =
                new GameClient(
                        serverUri,
                        window
                );

        window.setClient(client);

        client.connect();
    }
}