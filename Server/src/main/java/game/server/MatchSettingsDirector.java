package game.server;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Builder pattern - Director. One method per product, each owning the real
 * numbers for its mode and the order of the build steps; the builders only
 * apply what they're handed.
 *
 * Chaos has no builder of its own: it produces the same product TYPE as a
 * Speed match (a SpeedMatch), just filled with random numbers. Builders
 * exist per product type, not per game mode.
 */
public class MatchSettingsDirector {

    public StandardMatch buildStandardMatch() {
        return new StandardMatchBuilder()
                .setHealth(25)
                .setEnergy(6, 2)
                .setHandSizes(5, 7)
                .getProduct();
    }

    public SpeedMatch buildSpeedMatch() {
        return new SpeedMatchBuilder()
                .setHealth(25)
                .setEnergy(6, 2)
                .setHandSizes(5, 7)
                .setTimer(minutesToMillis(10))
                .getProduct();
    }

    public SpeedMatch buildChaosMatch() {
        return new SpeedMatchBuilder()
                .setHealth(randomBetween(10, 40))
                .setEnergy(randomBetween(0, 10), randomBetween(1, 4))
                .setHandSizes(randomBetween(2, 6), randomBetween(6, 10))
                .setTimer(minutesToMillis(randomBetween(5, 15)))
                .getProduct();
    }

    private int randomBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private long minutesToMillis(int minutes) {
        return minutes * 60L * 1000L;
    }
}