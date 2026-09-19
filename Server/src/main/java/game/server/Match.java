package game.server;

import java.util.concurrent.ThreadLocalRandom;

public class Match {

    public static final int BOARD_WIDTH = 600;
    public static final int BOARD_HEIGHT = 400;
    public static final int CARD_SIZE = 60;

    private final int id;
    private final PlayerConnection player1;
    private final PlayerConnection player2;

    private int player1X = 100;
    private int player1Y = 170;

    private int player2X = 440;
    private int player2Y = 170;

    private PlayerConnection currentTurn;

    private boolean active = true;

    public Match(
            int id,
            PlayerConnection player1,
            PlayerConnection player2) {

        this.id = id;
        this.player1 = player1;
        this.player2 = player2;

        // Randomly choose who goes first.
        if (ThreadLocalRandom.current().nextBoolean()) {
            currentTurn = player1;
        } else {
            currentTurn = player2;
        }
    }

    public synchronized boolean movePlayer(
            PlayerConnection player,
            int x,
            int y) {

        // Only the player whose turn it is may move.
        if (!active || currentTurn != player) {
            return false;
        }

        // Keep the card inside the board.
        x = clamp(
                x,
                0,
                BOARD_WIDTH - CARD_SIZE
        );

        y = clamp(
                y,
                0,
                BOARD_HEIGHT - CARD_SIZE
        );

        if (player == player1) {
            player1X = x;
            player1Y = y;

            return true;
        }

        if (player == player2) {
            player2X = x;
            player2Y = y;

            return true;
        }

        return false;
    }

    public synchronized boolean endTurn(
            PlayerConnection player) {

        if (!active || currentTurn != player) {
            return false;
        }

        currentTurn = getOpponent(player);

        return true;
    }

    public synchronized PlayerConnection getOpponent(
            PlayerConnection player) {

        if (player == player1) {
            return player2;
        }

        if (player == player2) {
            return player1;
        }

        return null;
    }

    public synchronized int getPlayerNumber(
            PlayerConnection player) {

        if (player == player1) {
            return 1;
        }

        if (player == player2) {
            return 2;
        }

        return -1;
    }

    public synchronized int getCurrentTurnPlayerNumber() {

        return getPlayerNumber(currentTurn);
    }

    public synchronized boolean contains(
            PlayerConnection player) {

        return player == player1 || player == player2;
    }

    public synchronized void endMatch() {
        active = false;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized int getId() {
        return id;
    }

    public synchronized int getPlayer1X() {
        return player1X;
    }

    public synchronized int getPlayer1Y() {
        return player1Y;
    }

    public synchronized int getPlayer2X() {
        return player2X;
    }

    public synchronized int getPlayer2Y() {
        return player2Y;
    }

    public PlayerConnection getPlayer1() {
        return player1;
    }

    public PlayerConnection getPlayer2() {
        return player2;
    }

    private int clamp(
            int value,
            int min,
            int max) {

        return Math.max(min, Math.min(max, value));
    }

}