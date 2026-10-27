package game.server;

import game.server.cards.Unit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import game.server.commands.GameCommand;
import game.server.commands.ChooseArcherTargetCommand;
import game.server.GameStatistics;

public class MatchmakingService {

    private final Map<GameMode, Queue<PlayerConnection>> waitingPlayers = new EnumMap<>(GameMode.class);
    private final Map<Integer, MatchFacade> activeMatches = new HashMap<>();
    private final Map<PlayerConnection, MatchFacade> playerMatches = new HashMap<>();
    private final Map<Player, Deque<GameCommand>> archerCommandHistory = new HashMap<>();
    private final MatchSettingsDirector settingsDirector = new MatchSettingsDirector();

    private int nextMatchId = 1;

    public MatchmakingService() {
        for (GameMode mode : GameMode.values()) {
            waitingPlayers.put(mode, new ArrayDeque<>());
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "match-timeout-checker");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::checkAllMatchesForTimeout, 1, 1, TimeUnit.SECONDS);
    }

    public synchronized void addPlayer(PlayerConnection connection, GameMode mode) {
        Queue<PlayerConnection> queue = waitingPlayers.get(mode);

        while (!queue.isEmpty()) {
            PlayerConnection opponent = queue.poll();
            if (!opponent.isConnected()) {
                continue;
            }

            int matchId = nextMatchId++;
            MatchSettings settings = createSettings(mode);
            Match match = new Match(matchId, opponent, connection, settings);
            MatchFacade matchFacade = new MatchFacade(match);

            activeMatches.put(matchId, matchFacade);
            playerMatches.put(opponent, matchFacade);
            playerMatches.put(connection, matchFacade);

            opponent.send("MATCH:" + matchId + ":PLAYER1");
            connection.send("MATCH:" + matchId + ":PLAYER2");

            broadcastState(matchFacade);

            System.out.println("Created " + mode + " Match " + matchId);
            System.out.println("First turn: Player " + matchFacade.getCurrentTurnPlayerNumber());
            return;
        }

        queue.add(connection);
        connection.send("WAITING");
        System.out.println("Player is waiting for a " + mode + " opponent.");
    }

    private MatchSettings createSettings(GameMode mode) {
        return switch (mode) {
            case STANDARD -> settingsDirector.buildStandardMatch();
            case SPEED -> settingsDirector.buildSpeedMatch();
            case CHAOS -> settingsDirector.buildChaosMatch();
        };
    }

    public synchronized void handleMessage(PlayerConnection connection, String message) {
        if (message.startsWith("JOIN|")) {
            handleJoin(connection, message);
            return;
        }

        MatchFacade matchFacade = playerMatches.get(connection);
        if (matchFacade == null || !matchFacade.isActive()) {
            return;
        }

        Player player = matchFacade.getPlayerFor(connection);
        if (player == null) {
            return;
        }

        if (message.startsWith("ARCHER_TARGET|")) {
            String[] parts = message.split("\\|");

            if (parts.length != 3) {
                return;
            }

            try {
                int targetSlot = Integer.parseInt(parts[2]);

                GameCommand command = new ChooseArcherTargetCommand(
                        matchFacade, player, parts[1], targetSlot
                );

                boolean accepted = command.execute();
                if (accepted) {
                    archerCommandHistory.computeIfAbsent(player, key -> new ArrayDeque<>())
                            .push(command);
                }
                System.out.println("Archer target slot " + (targetSlot + 1)
                        + (accepted ? " accepted." : " rejected."));
                connection.send(
                        accepted ? "ARCHER_TARGET_OK" : "ARCHER_TARGET_REJECTED"
                );
                if (accepted) {
                    broadcastState(matchFacade);
                }
            } catch (NumberFormatException e) {
                connection.send("ARCHER_TARGET_REJECTED");
            }

            return;
        }

        if (message.equals("UNDO_ARCHER_TARGET")) {
            Deque<GameCommand> history = archerCommandHistory.get(player);
            boolean undone = history != null && !history.isEmpty() && history.peek().undo();
            if (undone) {
                history.pop();
            }
            connection.send(undone ? "ARCHER_UNDO_OK" : "ARCHER_UNDO_REJECTED");
            if (undone) {
                broadcastState(matchFacade);
            }
            return;
        }

        if (message.startsWith("PLAY_UNIT|")) {
            handlePlayUnit(player, matchFacade, message);
            return;
        }
        if (message.startsWith("PLAY_SPELL|")) {
            handlePlaySpell(player, matchFacade, message);
            return;
        }
        if (message.startsWith("PLAY_TRAP|")) {
            handlePlayTrap(player, matchFacade, message);
            return;
        }
        if (message.equals("END_TURN")) {
            if (matchFacade.endTurn(player)) {
                archerCommandHistory.remove(player);
            }
            broadcastState(matchFacade);
            checkForGameOver(matchFacade);
            return;
        }

        System.out.println("Unknown message: " + message);
    }

    private void handleJoin(PlayerConnection connection, String message) {
        String[] parts = message.split("\\|");
        if (parts.length != 2) {
            return;
        }

        GameMode mode;
        try {
            mode = GameMode.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid game mode: " + parts[1]);
            return;
        }

        addPlayer(connection, mode);
    }

    private void handlePlayUnit(Player player, MatchFacade matchFacade, String message) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length != 3) {
                return;
            }

            String cardId = parts[1];
            int zoneIndex = Integer.parseInt(parts[2]);

            boolean played = matchFacade.playUnit(player, cardId, zoneIndex);
            System.out.println(played ? "Unit play accepted." : "Unit play rejected.");

            broadcastState(matchFacade);
            checkForGameOver(matchFacade);

        } catch (NumberFormatException e) {
            System.out.println("Invalid unit play: " + message);
        }
    }

    private void handlePlaySpell(Player player, MatchFacade matchFacade, String message) {
        String[] parts = message.split("\\|");
        if (parts.length != 3) {
            return;
        }

        String cardId = parts[1];
        String targetId = parts[2];
        Unit target = targetId.equals("-") ? null : matchFacade.findUnitOnField(targetId);

        boolean played = matchFacade.playSpell(player, cardId, target);
        System.out.println(played ? "Spell play accepted." : "Spell play rejected.");

        broadcastState(matchFacade);
        checkForGameOver(matchFacade);
    }

    private void handlePlayTrap(Player player, MatchFacade matchFacade, String message) {
        try {
            String[] parts = message.split("\\|");
            if (parts.length != 3) {
                return;
            }

            String cardId = parts[1];
            int zoneIndex = Integer.parseInt(parts[2]);

            boolean played = matchFacade.playTrap(player, cardId, zoneIndex);
            System.out.println(played ? "Trap play accepted." : "Trap play rejected.");

            broadcastState(matchFacade);

        } catch (NumberFormatException e) {
            System.out.println("Invalid trap play: " + message);
        }
    }

    private void broadcastState(MatchFacade matchFacade) {
        matchFacade.notifyObservers();

    }

    private void checkForGameOver(MatchFacade matchFacade) {
        Player defeated = matchFacade.getDefeatedPlayer();
        if (defeated == null) {
            return;
        }

        Player winner = matchFacade.getOpponent(defeated);
        winner.send("YOU_WIN");
        defeated.send("YOU_LOSE");

        matchFacade.endMatch();
        GameStatistics.getInstance().recordMatchCompleted();
        archerCommandHistory.remove(matchFacade.getPlayer1());
        archerCommandHistory.remove(matchFacade.getPlayer2());
        activeMatches.remove(matchFacade.getId());
        playerMatches.remove(winner.getConnection());
        playerMatches.remove(defeated.getConnection());

        System.out.println("Match " + matchFacade.getId() + " ended by defeat.");
        System.out.println(GameStatistics.getInstance().getSummary());
    }

    private synchronized void checkAllMatchesForTimeout() {
        for (MatchFacade matchFacade : new ArrayList<>(activeMatches.values())) {
            if (matchFacade.isActive() && matchFacade.hasTimer()) {
                broadcastState(matchFacade);
                checkForGameOver(matchFacade);
            }
        }
    }

    public synchronized void removePlayerFromMatch(PlayerConnection connection) {
        for (Queue<PlayerConnection> queue : waitingPlayers.values()) {
            if (queue.remove(connection)) {
                System.out.println("Waiting player disconnected.");
                return;
            }
        }

        MatchFacade matchFacade = playerMatches.remove(connection);
        if (matchFacade == null) {
            return;
        }

        Player player = matchFacade.getPlayerFor(connection);
        Player opponent = matchFacade.getOpponent(player);

        matchFacade.endMatch();
        archerCommandHistory.remove(matchFacade.getPlayer1());
        archerCommandHistory.remove(matchFacade.getPlayer2());
        matchFacade.removeObserver(connection);
        playerMatches.remove(opponent.getConnection());
        activeMatches.remove(matchFacade.getId());

        System.out.println("Player " + matchFacade.getPlayerNumber(player)
                + " disconnected from Match " + matchFacade.getId());

        if (opponent.isConnected()) {
            opponent.send("YOU_WIN");
            System.out.println("Player " + matchFacade.getPlayerNumber(opponent) + " wins.");
        }
    }
}