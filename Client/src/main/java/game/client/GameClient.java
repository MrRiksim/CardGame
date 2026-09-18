package game.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class GameClient extends WebSocketClient {

    private final GameWindow window;

    public GameClient(
            URI serverUri,
            GameWindow window) {

        super(serverUri);

        this.window = window;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {

        System.out.println(
                "Connected to server."
        );

        window.setStatus(
                "Connected - waiting..."
        );
    }

    @Override
    public void onMessage(String message) {

        System.out.println(
                "Server: " + message
        );

        if (message.equals("WAITING")) {

            window.setStatus(
                    "Waiting for player..."
            );

            return;
        }

        if (message.startsWith("MATCH:")) {

            String matchId =
                    message.substring("MATCH:".length());

            window.setStatus(
                    "Match " + matchId
            );

            return;
        }

        if (message.equals("YOU_WIN")) {

            window.setStatus(
                    "You win"
            );
        }
    }

    @Override
    public void onClose(
            int code,
            String reason,
            boolean remote) {

        System.out.println(
                "Disconnected from server."
        );

        window.setStatus(
                "Disconnected from server"
        );
    }

    @Override
    public void onError(Exception exception) {

        System.err.println(
                "Connection error:"
        );

        exception.printStackTrace();

        window.setStatus(
                "Can't connect to server"
        );
    }

    public static void main(String[] args)
            throws Exception {

        GameWindow window =
                new GameWindow();

        URI serverUri =
                new URI("ws://localhost:6767");

        GameClient client =
                new GameClient(
                        serverUri,
                        window
                );

        client.connect();
    }
}