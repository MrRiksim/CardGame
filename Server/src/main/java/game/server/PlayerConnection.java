package game.server;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

public class PlayerConnection {

    private final WebSocket socket;

    public PlayerConnection(WebSocket socket) {
        this.socket = socket;
    }

    public WebSocket getSocket() {
        return socket;
    }

    public boolean isConnected() {
        return socket.isOpen();
    }

    public void send(String message) {

        if (!socket.isOpen()) {
            return;
        }

        try {
            socket.send(message);
        } catch (WebsocketNotConnectedException e) {
            // The connection may have closed between isOpen()
            // and send(). Ignore it because the player is gone.
        }
    }
}