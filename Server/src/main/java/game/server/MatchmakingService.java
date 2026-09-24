package game.server;

import game.server.cards.Unit;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Protocol v2 message formats handled here:
 *
 * <pre>
 * PLAY_UNIT|cardId|zoneIndex
 * PLAY_SPELL|cardId|targetUnitId   (targetUnitId is "-" for no target)
 * PLAY_TRAP|cardId|zoneIndex
 * END_TURN
 * </pre>
 */
public class MatchmakingService {

    private final Queue<PlayerConnection> waitingPlayers = new ArrayDeque<>();
    private final Map<Integer, Match> activeMatches = new HashMap<>();
    private final Map<PlayerConnection, Match> playerMatches = new HashMap<>();

    private int nextMatchId = 1;

    public synchronized void addPlayer(PlayerConnection connection) {
        while (!waitingPlayers.isEmpty()) {
            PlayerConnection opponent = waitingPlayers.poll();
            if (!opponent.isConnected()) {
                continue;
            }

            int matchId = nextMatchId++;
            Match match = new Match(matchId, opponent, connection);

            activeMatches.put(matchId, match);
            playerMatches.put(opponent, match);
            playerMatches.put(connection, match);

            opponent.send("MATCH:" + matchId + ":PLAYER1");
            connection.send("MATCH:" + matchId + ":PLAYER2");

            broadcastState(match);

            System.out.println("Created Match " + matchId);
            System.out.println("First turn: Player " + match.getCurrentTurnPlayerNumber());
            return;
        }

        waitingPlayers.add(connection);
        connection.send("WAITING");
        System.out.println("Player is waiting for an opponent.");
    }

    public synchronized void handleMessage(PlayerConnection connection, String message) {
        Match match = playerMatches.get(connection);
        if (match == null || !match.isActive()) {
            return;
        }

        Player player = match.getPlayerFor(connection);
        if (player == null) {
            return;
        }

        if (message.startsWith("PLAY_UNIT|")) {
            handlePlayUnit(player, match, message);
            return;
        }
        if (message.startsWith("PLAY_SPELL|")) {
            handlePlaySpell(player, match, message);
            return;
        }
        if (message.startsWith("PLAY_TRAP|")) {
            handlePlayTrap(player, match, message);
            return;
        }
        if (message.equals("END_TURN")) {
            match.endTurn(player);
            broadcastState(match);
            checkForGameOver(match);
            return;
        }

        System.out.println("Unknown message: " + message);
    }

    private void handlePlayUnit(Player player, Match match, String message) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length != 3) {
                return;
            }

            String cardId = parts[1];
            int zoneIndex = Integer.parseInt(parts[2]);

            boolean played = match.playUnit(player, cardId, zoneIndex);
            System.out.println(played ? "Unit play accepted." : "Unit play rejected.");

            broadcastState(match);
            checkForGameOver(match);

        } catch (NumberFormatException e) {
            System.out.println("Invalid unit play: " + message);
        }
    }

    private void handlePlaySpell(Player player, Match match, String message) {
        String[] parts = message.split("\\|");
        if (parts.length != 3) {
            return;
        }

        String cardId = parts[1];
        String targetId = parts[2];
        Unit target = targetId.equals("-") ? null : match.findUnitOnField(targetId);

        boolean played = match.playSpell(player, cardId, target);
        System.out.println(played ? "Spell play accepted." : "Spell play rejected.");

        broadcastState(match);
        checkForGameOver(match);
    }

    private void handlePlayTrap(Player player, Match match, String message) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length != 3) {
                return;
            }

            String cardId = parts[1];
            int zoneIndex = Integer.parseInt(parts[2]);

            boolean played = match.playTrap(player, cardId, zoneIndex);
            System.out.println(played ? "Trap play accepted." : "Trap play rejected.");

            broadcastState(match);

        } catch (NumberFormatException e) {
            System.out.println("Invalid trap play: " + message);
        }
    }

    private void broadcastState(Match match) {
        match.getPlayer1().send(match.buildStateFor(match.getPlayer1()));
        match.getPlayer2().send(match.buildStateFor(match.getPlayer2()));
    }

    /*
     * Health-based win condition, checked after anything that can deal
     * damage (a unit attack or a damage spell).
     */
    private void checkForGameOver(Match match) {
        Player defeated = match.getDefeatedPlayer();
        if (defeated == null) {
            return;
        }

        Player winner = match.getOpponent(defeated);
        winner.send("YOU_WIN");
        defeated.send("YOU_LOSE");

        match.endMatch();
        activeMatches.remove(match.getId());
        playerMatches.remove(winner.getConnection());
        playerMatches.remove(defeated.getConnection());

        System.out.println("Match " + match.getId() + " ended by defeat.");
    }

    public synchronized void removePlayerFromMatch(PlayerConnection connection) {
        if (waitingPlayers.remove(connection)) {
            System.out.println("Waiting player disconnected.");
            return;
        }

        Match match = playerMatches.remove(connection);
        if (match == null) {
            return;
        }

        Player player = match.getPlayerFor(connection);
        Player opponent = match.getOpponent(player);

        match.endMatch();
        playerMatches.remove(opponent.getConnection());
        activeMatches.remove(match.getId());

        System.out.println("Player " + match.getPlayerNumber(player)
                + " disconnected from Match " + match.getId());

        if (opponent.isConnected()) {
            opponent.send("YOU_WIN");
            System.out.println("Player " + match.getPlayerNumber(opponent) + " wins.");
        }
    }
}
