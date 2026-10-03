package game.server;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Concrete Builder - every step rolls its own random value, within the
 * ranges below.
 */
public class ChaosMatchBuilder implements MatchBuilder {

    private int health;
    private int energy;
    private int energyPerTurn;
    private int startingHandSize;
    private int maxHandSize;
    private Long timePerPlayerMillis;

    private int randomBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    @Override
    public MatchBuilder setHealth() {
        health = randomBetween(10, 40);
        return this;
    }

    @Override
    public MatchBuilder setEnergy() {
        energy = randomBetween(0, 10);
        energyPerTurn = randomBetween(1, 4);
        return this;
    }

    @Override
    public MatchBuilder setHandSizes() {
        startingHandSize = randomBetween(2, 6);
        maxHandSize = randomBetween(6, 10);
        return this;
    }

    /*
     * A coin flip on whether this chaos match also gets Speed Match's
     * 10-minute clock.
     */
    @Override
    public MatchBuilder setTimer() {
        timePerPlayerMillis = ThreadLocalRandom.current().nextBoolean()
                ? SpeedMatchBuilder.TEN_MINUTES_MILLIS
                : null;
        return this;
    }

    @Override
    public MatchSettings build() {
        return new MatchSettings(health, energy, energyPerTurn, startingHandSize, maxHandSize, timePerPlayerMillis);
    }
}
