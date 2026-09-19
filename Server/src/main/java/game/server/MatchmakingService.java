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

        while (!waitingPlayers.isEmpty()) {

            PlayerConnection opponent =
                    waitingPlayers.poll();

            if (!opponent.isConnected()) {
                continue;
            }

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

            opponent.send(
                    "MATCH:"
                            + matchId
                            + ":PLAYER1"
            );

            player.send(
                    "MATCH:"
                            + matchId
                            + ":PLAYER2"
            );

            broadcastState(match);

            System.out.println(
                    "Created Match "
                            + matchId
            );

            System.out.println(
                    "First turn: Player "
                            + match.getCurrentTurnPlayerNumber()
            );

            return;
        }

        waitingPlayers.add(player);

        player.send("WAITING");

        System.out.println(
                "Player is waiting for an opponent."
        );
    }

    public synchronized void handleMessage(
            PlayerConnection player,
            String message) {

        Match match =
                playerMatches.get(player);

        if (match == null ||
                !match.isActive()) {

            return;
        }

        if (message.startsWith("MOVE_CARD|")) {

            handleCardMove(
                    player,
                    match,
                    message
            );

            return;
        }

        if (message.equals("END_TURN")) {

            boolean changed =
                    match.endTurn(player);

            /*
             * Whether the turn changed or not,
             * send authoritative state back.
             */
            if (changed) {

                System.out.println(
                        "Match "
                                + match.getId()
                                + ": Player "
                                + match.getPlayerNumber(player)
                                + " ended their turn."
                );
            }

            broadcastState(match);

            return;
        }

        System.out.println(
                "Unknown message: "
                        + message
        );
    }

    private void handleCardMove(
            PlayerConnection player,
            Match match,
            String message) {

        try {

            /*
             * MOVE_CARD|cardId|BROWN|2
             */
            String[] parts =
                    message.split("\\|");

            if (parts.length != 4) {
                return;
            }

            String cardId =
                    parts[1];

            CardType targetType;

            if (parts[2].equals("BROWN")) {

                targetType =
                        CardType.LIGHT_BROWN;

            } else if (parts[2].equals("BLUE")) {

                targetType =
                        CardType.LIGHT_BLUE;

            } else {

                return;
            }

            int targetZone =
                    Integer.parseInt(parts[3]);

            boolean moved =
                    match.moveCard(
                            player,
                            cardId,
                            targetType,
                            targetZone
                    );

            if (moved) {

                System.out.println(
                        "Card move accepted."
                );
            } else {

                System.out.println(
                        "Card move rejected."
                );
            }

            /*
             * Always send the authoritative state.
             *
             * If the client made an invalid move,
             * this effectively makes it snap back.
             */
            broadcastState(match);

        } catch (NumberFormatException e) {

            System.out.println(
                    "Invalid card movement: "
                            + message
            );

            broadcastState(match);
        }
    }

    private void broadcastState(
            Match match) {

        match.getPlayer1().send(
                match.buildStateFor(
                        match.getPlayer1()
                )
        );

        match.getPlayer2().send(
                match.buildStateFor(
                        match.getPlayer2()
                )
        );
    }

    public synchronized void removePlayerFromMatch(
            PlayerConnection player) {

        if (waitingPlayers.remove(player)) {

            System.out.println(
                    "Waiting player disconnected."
            );

            return;
        }

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

        if (opponent != null &&
                opponent.isConnected()) {

            opponent.send("YOU_WIN");

            System.out.println(
                    "Player "
                            + match.getPlayerNumber(opponent)
                            + " wins."
            );
        }
    }
}