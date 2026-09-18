package game.server;

public class Match {

    private final int id;
    private final PlayerConnection player1;
    private final PlayerConnection player2;

    public Match(
            int id,
            PlayerConnection player1,
            PlayerConnection player2) {

        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
    }

    public int getId() {
        return id;
    }

    public PlayerConnection getPlayer1() {
        return player1;
    }

    public PlayerConnection getPlayer2() {
        return player2;
    }

    public PlayerConnection getOpponent(PlayerConnection player) {

        if (player == player1) {
            return player2;
        }

        if (player == player2) {
            return player1;
        }

        return null;
    }

    public boolean contains(PlayerConnection player) {
        return player == player1 || player == player2;
    }
}