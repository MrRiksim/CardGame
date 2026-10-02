package game.server;

/**
 * Concrete Builder - the existing, fixed ruleset, unchanged from before
 * game modes existed. {@link SpeedMatchBuilder} and {@link ChaosMatchBuilder}
 * both extend this one so they only have to override the steps they
 * actually want to do differently; fields are protected for exactly that
 * reason.
 */
public class StandardMatchBuilder implements MatchBuilder {

    protected int health;
    protected int energy;
    protected int energyPerTurn;
    protected int startingHandSize;
    protected int maxHandSize;
    protected Long timePerPlayerMillis;

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
