package game.server;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MatchmakingService {

    private final Queue<PlayerConnection> waitingPlayers =
            new ArrayDeque<>();

    private final Map<Integer, Match> activeMatches =
            new HashMap<>();

    private final Map<PlayerConnection, Match> playerMatches =
            new HashMap<>();

    private int nextMatchId = 1;

    public synchronized void addPlayer(
            PlayerConnection player) {

        /*
         * Remove disconnected players which may still
         * be sitting in the waiting queue.
         */
        while (!waitingPlayers.isEmpty()) {

            PlayerConnection opponent =
                    waitingPlayers.poll();

            if (!opponent.isConnected()) {

                System.out.println(
                        "Removed disconnected player from queue."
                );

                continue;
            }

            /*
             * Create the match.
             */
            int matchId = nextMatchId++;

            Match match =
                    new Match(
                            matchId,
                            opponent,
                            player
                    );

            activeMatches.put(
                    matchId,
                    match
            );

            playerMatches.put(
                    opponent,
                    match
            );

            playerMatches.put(
                    player,
                    match
            );

            /*
             * Tell each player which player number they are.
             */
            opponent.send(
                    "MATCH:" + matchId + ":PLAYER1"
            );

            player.send(
                    "MATCH:" + matchId + ":PLAYER2"
            );

            /*
             * Send initial game state.
             */
            broadcastState(match);

            System.out.println(
                    "Created Match "
                            + matchId
                            + ". Player 1 goes against Player 2."
            );

            System.out.println(
                    "First turn: Player "
                            + match.getCurrentTurnPlayerNumber()
            );

            return;
        }

        /*
         * Nobody is waiting.
         */
        waitingPlayers.add(player);

        player.send("WAITING");

        System.out.println(
                "Player is waiting for an opponent."
        );
    }

    public synchronized void handleMessage(
            PlayerConnection player,
            String message) {

        Match match = playerMatches.get(player);

        if (match == null || !match.isActive()) {
            return;
        }

        /*
         * MOVE:x:y
         */
        if (message.startsWith("MOVE:")) {

            handleMove(
                    player,
                    match,
                    message
            );

            return;
        }

        /*
         * END_TURN
         */
        if (message.equals("END_TURN")) {

            boolean changed =
                    match.endTurn(player);

            if (changed) {

                System.out.println(
                        "Match "
                                + match.getId()
                                + ": Player "
                                + match.getPlayerNumber(player)
                                + " ended their turn."
                );

                broadcastState(match);
            }

            return;
        }

        System.out.println(
                "Unknown message: " + message
        );
    }

    private void handleMove(
            PlayerConnection player,
            Match match,
            String message) {

        try {

            String[] parts =
                    message.split(":");

            if (parts.length != 3) {
                return;
            }

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);

            boolean moved =
                    match.movePlayer(
                            player,
                            x,
                            y
                    );

            if (moved) {
                broadcastState(match);
            }

        } catch (NumberFormatException e) {

            System.out.println(
                    "Invalid MOVE message: "
                            + message
            );
        }
    }

    private void broadcastState(
            Match match) {

        String message =
                "STATE:"
                        + match.getId()
                        + ":"
                        + match.getPlayer1X()
                        + ":"
                        + match.getPlayer1Y()
                        + ":"
                        + match.getPlayer2X()
                        + ":"
                        + match.getPlayer2Y()
                        + ":"
                        + match.getCurrentTurnPlayerNumber();

        match.getPlayer1().send(message);
        match.getPlayer2().send(message);
    }

    public synchronized void removePlayerFromMatch(
            PlayerConnection player) {

        /*
         * Was the player simply waiting?
         */
        if (waitingPlayers.remove(player)) {

            System.out.println(
                    "Waiting player disconnected."
            );

            return;
        }

        /*
         * Was the player in an active match?
         */
        Match match =
                playerMatches.remove(player);

        if (match == null) {
            return;
        }

        PlayerConnection opponent =
                match.getOpponent(player);

        match.endMatch();

        playerMatches.remove(opponent);

        activeMatches.remove(
                match.getId()
        );

        System.out.println(
                "Player "
                        + match.getPlayerNumber(player)
                        + " disconnected from Match "
                        + match.getId()
        );

        /*
         * Tell the remaining player that they won.
         */
        if (opponent != null && opponent.isConnected()) {

            opponent.send("YOU_WIN");

            System.out.println(
                    "Player "
                            + match.getPlayerNumber(opponent)
                            + " wins Match "
                            + match.getId()
                            + " because their opponent disconnected."
            );
        }
    }
}