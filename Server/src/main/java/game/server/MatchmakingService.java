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

    public synchronized void addPlayer(PlayerConnection player) {

        /*
         * First, remove stale players.
         *
         * This handles the case where a client disconnected,
         * but the server's onClose callback has not been processed yet.
         */
        while (!waitingPlayers.isEmpty()) {

            PlayerConnection opponent =
                    waitingPlayers.poll();

            if (!opponent.isConnected()) {
                System.out.println(
                        "Removed disconnected player from waiting queue."
                );

                continue;
            }

            // We found a valid opponent.

            int matchId = nextMatchId++;

            Match match =
                    new Match(
                            matchId,
                            opponent,
                            player
                    );

            activeMatches.put(matchId, match);

            playerMatches.put(opponent, match);
            playerMatches.put(player, match);

            opponent.send("MATCH:" + matchId);
            player.send("MATCH:" + matchId);

            System.out.println(
                    "Created Match "
                            + matchId
                            + " between two players."
            );

            return;
        }

        /*
         * No opponent is available.
         * Put this player into the queue.
         */
        waitingPlayers.add(player);

        player.send("WAITING");

        System.out.println(
                "Player is waiting for an opponent."
        );
    }

    public synchronized void playerDisconnected(
            PlayerConnection player) {

        /*
         * Case 1:
         * Player was waiting for an opponent.
         */
        if (waitingPlayers.remove(player)) {

            System.out.println(
                    "Waiting player disconnected."
            );

            return;
        }

        /*
         * Case 2:
         * Player was already in a match.
         */
        Match match = playerMatches.remove(player);

        if (match == null) {
            // Player wasn't in the queue or a match.
            return;
        }

        PlayerConnection opponent =
                match.getOpponent(player);

        // Remove the match.
        playerMatches.remove(opponent);
        activeMatches.remove(match.getId());

        System.out.println(
                "Player disconnected from Match "
                        + match.getId()
        );

        /*
         * Notify the remaining player.
         */
        if (opponent != null && opponent.isConnected()) {

            opponent.send("YOU_WIN");

            System.out.println(
                    "Player in Match "
                            + match.getId()
                            + " wins because their opponent disconnected."
            );
        }
    }
}