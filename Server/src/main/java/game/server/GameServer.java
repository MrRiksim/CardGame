package game.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer extends WebSocketServer {

    private final MatchmakingService matchmakingService;
    private final Map<WebSocket, PlayerConnection> players = new ConcurrentHashMap<>();

    public GameServer(int port) {
        super(new InetSocketAddress(port));
        matchmakingService = new MatchmakingService();
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake handshake) {
        System.out.println("New connection from: " + connection.getRemoteSocketAddress());
        PlayerConnection player = new PlayerConnection(connection);
        players.put(connection, player);
        matchmakingService.addPlayer(player);
    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + connection.getRemoteSocketAddress());
        PlayerConnection player = players.remove(connection);
        if (player != null) matchmakingService.removePlayerFromMatch(player);
    }

    @Override
    public void onMessage(WebSocket connection, String message) {
        PlayerConnection player = players.get(connection);
        if (player == null) return;
        System.out.println("Received from player: " + message);
        matchmakingService.handleMessage(player, message);
    }

    @Override
    public void onError(WebSocket connection, Exception exception) {
        System.err.println("WebSocket error:");
        exception.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server started on port " + getPort());
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(6767);
        server.start();
        System.out.println("Waiting for players...");
    }
}
