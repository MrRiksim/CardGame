package game.server;

/**
 * Concrete Builder - the existing, fixed ruleset, unchanged from before
 * game modes existed.
 */
public class StandardMatchBuilder implements MatchBuilder {

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
        timePerPlayerMillis = null;
        return this;
    }

    @Override
    public MatchSettings build() {
        return new MatchSettings(health, energy, energyPerTurn, startingHandSize, maxHandSize, timePerPlayerMillis);
    }
}
