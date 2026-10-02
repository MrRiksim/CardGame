package game.server;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Concrete Builder - every step rolls its own random value, within the
 * ranges below. Reuses {@link StandardMatchBuilder}'s fields and
 * {@code build()}; only HOW each step is filled in differs.
 */
public class ChaosMatchBuilder extends StandardMatchBuilder {

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

    private int randomBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }
}
