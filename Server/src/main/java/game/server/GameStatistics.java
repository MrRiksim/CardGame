package game.server;

public final class GameStatistics {

    private static final GameStatistics INSTANCE =
            new GameStatistics();

    private int matchesStarted;
    private int matchesCompleted;
    private int cardsPlayed;
    private int damageDealt;

    private GameStatistics() {
    }

    public static GameStatistics getInstance() {
        return INSTANCE;
    }

    public synchronized void recordMatchStarted() {
        matchesStarted++;
    }

    public synchronized void recordMatchCompleted() {
        matchesCompleted++;
    }

    public synchronized void recordCardPlayed() {
        cardsPlayed++;
    }
    public synchronized void recordDamageDealt(int dmg){
        damageDealt+=dmg;
    }

    public synchronized String getSummary() {
        return "Matches started: " + matchesStarted
                + ", completed: " + matchesCompleted
                + ", cards played: " + cardsPlayed
                + ", damage dealt: " + damageDealt;
    }
}