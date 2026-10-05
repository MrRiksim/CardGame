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

/**
 * Protocol v3 message formats handled here:
 *
 * <pre>
 * JOIN|mode                        (STANDARD, SPEED or CHAOS - sent once,
 *                                    before anything else, to pick a queue)
 * PLAY_UNIT|cardId|zoneIndex
 * PLAY_SPELL|cardId|targetUnitId   (targetUnitId is "-" for no target)
 * PLAY_TRAP|cardId|zoneIndex
 * END_TURN
 * </pre>
 *
 * <p>Each {@link GameMode} gets its own, completely separate waiting
 * queue, so a player is only ever matched against someone who picked the
 * same mode - pairing a Chaos player's randomized rules with a Standard
 * opponent wouldn't make sense half-applied to only one side of the
 * match. The resolved {@link MatchSettings} for a match are assembled
 * fresh via {@link MatchSettingsDirector} once two same-mode players are
 * paired - see the Builder pattern notes on {@link MatchBuilder}.
 */
public class MatchmakingService {

    private final Map<GameMode, Queue<PlayerConnection>> waitingPlayers = new EnumMap<>(GameMode.class);
    private final Map<Integer, Match> activeMatches = new HashMap<>();
    private final Map<PlayerConnection, Match> playerMatches = new HashMap<>();
    private final Map<Player, Deque<GameCommand>> archerCommandHistory = new HashMap<>();
    private final MatchSettingsDirector settingsDirector = new MatchSettingsDirector();

    private int nextMatchId = 1;

    public MatchmakingService() {
        for (GameMode mode : GameMode.values()) {
            waitingPlayers.put(mode, new ArrayDeque<>());
        }

        /*
         * Only a timed match ever needs checking, but polling every active
         * match once a second is cheap, and it's what lets a player lose
         * the moment their clock runs out even if they never send another
         * message (see Match.getDefeatedPlayer).
         */
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
            MatchSettings settings = settingsDirector.direct(createBuilder(mode));
            Match match = new Match(matchId, opponent, connection, settings);

            activeMatches.put(matchId, match);
            playerMatches.put(opponent, match);
            playerMatches.put(connection, match);

            opponent.send("MATCH:" + matchId + ":PLAYER1");
            connection.send("MATCH:" + matchId + ":PLAYER2");

            broadcastState(match);

            System.out.println("Created " + mode + " Match " + matchId);
            System.out.println("First turn: Player " + match.getCurrentTurnPlayerNumber());
            return;
        }

        queue.add(connection);
        connection.send("WAITING");
        System.out.println("Player is waiting for a " + mode + " opponent.");
    }

    private MatchBuilder createBuilder(GameMode mode) {
        return switch (mode) {
            case STANDARD -> new StandardMatchBuilder();
            case SPEED -> new SpeedMatchBuilder();
            case CHAOS -> new ChaosMatchBuilder();
        };
    }

    public synchronized void handleMessage(PlayerConnection connection, String message) {
        if (message.startsWith("JOIN|")) {
            handleJoin(connection, message);
            return;
        }

        Match match = playerMatches.get(connection);
        if (match == null || !match.isActive()) {
            return;
        }

        Player player = match.getPlayerFor(connection);
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
                        match, player, parts[1], targetSlot
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
                    broadcastState(match);
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
                broadcastState(match);
            }
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
            if (match.endTurn(player)) {
                archerCommandHistory.remove(player);
            }
            broadcastState(match);
            checkForGameOver(match);
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
     * Health-based defeat, or a timed match's clock running out, checked
     * after anything that can deal damage or spend time (a unit attack, a
     * damage spell, ending a turn, or this service's own periodic poll).
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
        archerCommandHistory.remove(match.getPlayer1());
        archerCommandHistory.remove(match.getPlayer2());
        activeMatches.remove(match.getId());
        playerMatches.remove(winner.getConnection());
        playerMatches.remove(defeated.getConnection());

        System.out.println("Match " + match.getId() + " ended by defeat.");
    }

    /*
     * A timed match's clock can run out without either player sending a
     * message (they simply never acted), so this is the only thing that
     * catches that case - everything else is reactive to a message.
     */
    private synchronized void checkAllMatchesForTimeout() {
        for (Match match : new ArrayList<>(activeMatches.values())) {
            if (match.isActive() && match.hasTimer()) {
                broadcastState(match);
                checkForGameOver(match);
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

        Match match = playerMatches.remove(connection);
        if (match == null) {
            return;
        }

        Player player = match.getPlayerFor(connection);
        Player opponent = match.getOpponent(player);

        match.endMatch();
        archerCommandHistory.remove(match.getPlayer1());
        archerCommandHistory.remove(match.getPlayer2());
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
