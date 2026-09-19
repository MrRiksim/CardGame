package game.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class GameClient extends WebSocketClient {

    private final GameWindow window;

    private int myPlayerNumber = -1;

    /*
     * Used to distinguish:
     *
     * 1. Never connected -> connection failure
     * 2. Connected, then lost connection -> disconnected
     */
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

        /*
         * Waiting for an opponent.
         */
        if (message.equals("WAITING")) {

            window.setWaiting();

            return;
        }

        /*
         * MATCH:1:PLAYER1
         *
         * or
         *
         * MATCH:1:PLAYER2
         */
        if (message.startsWith("MATCH:")) {

            handleMatchMessage(message);

            return;
        }

        /*
         * STATE:matchId:p1X:p1Y:p2X:p2Y:turnPlayer
         */
        if (message.startsWith("STATE:")) {

            handleStateMessage(message);

            return;
        }

        /*
         * Opponent disconnected.
         */
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

            String[] parts =
                    message.split(":");

            if (parts.length != 7) {
                return;
            }

            int player1X =
                    Integer.parseInt(parts[2]);

            int player1Y =
                    Integer.parseInt(parts[3]);

            int player2X =
                    Integer.parseInt(parts[4]);

            int player2Y =
                    Integer.parseInt(parts[5]);

            int turnPlayer =
                    Integer.parseInt(parts[6]);

            int myX;
            int myY;

            int enemyX;
            int enemyY;

            /*
             * Convert server Player 1/Player 2
             * coordinates into "me" and "enemy".
             */
            if (myPlayerNumber == 1) {

                myX = player1X;
                myY = player1Y;

                enemyX = player2X;
                enemyY = player2Y;

            } else if (myPlayerNumber == 2) {

                myX = player2X;
                myY = player2Y;

                enemyX = player1X;
                enemyY = player1Y;

            } else {

                return;
            }

            boolean yourTurn =
                    turnPlayer == myPlayerNumber;

            window.updateGame(
                    myX,
                    myY,
                    enemyX,
                    enemyY,
                    yourTurn
            );

        } catch (NumberFormatException e) {

            System.err.println(
                    "Invalid STATE message: "
                            + message
            );
        }
    }

    public void sendMove(
            int x,
            int y) {

        if (!isOpen()) {
            return;
        }

        send(
                "MOVE:" + x + ":" + y
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

        /*
         * If we never connected successfully, onError()
         * already handles the UI. Don't overwrite it.
         */
        if (hasConnected) {

            window.showDisconnected();
        }
    }

    @Override
    public void onError(
            Exception exception) {

        System.err.println(
                "Connection error:"
        );

        /*
         * You can still print the error for debugging,
         * but the user gets a clean UI message.
         */
        exception.printStackTrace();

        if (!hasConnected) {

            window.showCannotConnect();
        }
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