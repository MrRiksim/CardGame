package game.server;

/**
 * Concrete Builder - identical to Standard except each player gets a
 * 10-minute total clock that runs down during their own turns.
 */
public class SpeedMatchBuilder implements MatchBuilder {

    static final long TEN_MINUTES_MILLIS = 10L * 60 * 1000;

    private int health;
    private int energy;
    private int energyPerTurn;
    private int startingHandSize;
    private int maxHandSize;
    private Long timePerPlayerMillis;

    @Override
    public MatchBuilder setHealth() {
        health = 25;
        return this;
    }

    @Override
    public MatchBuilder setEnergy() {
        energy = 6;
        energyPerTurn = 2;
        return this;
    }

    @Override
    public MatchBuilder setHandSizes() {
        startingHandSize = 5;
        maxHandSize = 7;
        return this;
    }

    @Override
    public MatchBuilder setTimer() {
        timePerPlayerMillis = TEN_MINUTES_MILLIS;
        return this;
    }

    @Override
    public MatchSettings build() {
        return new MatchSettings(health, energy, energyPerTurn, startingHandSize, maxHandSize, timePerPlayerMillis);
    }
}
