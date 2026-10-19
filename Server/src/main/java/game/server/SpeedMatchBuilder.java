package game.server;

/**
 * Concrete Builder for a match with a per-player clock. Holds one
 * SpeedMatch. It doesn't extend StandardMatchBuilder, matching how the
 * products themselves are kept separate, so the three shared steps are
 * repeated here.
 */
public class SpeedMatchBuilder implements MatchBuilder {

    private final SpeedMatch product = new SpeedMatch();

    @Override
    public SpeedMatchBuilder setHealth(int health) {
        product.setStartingHealth(health);
        return this;
    }

    @Override
    public SpeedMatchBuilder setEnergy(int startingEnergy, int energyPerTurn) {
        product.setStartingEnergy(startingEnergy);
        product.setEnergyPerTurn(energyPerTurn);
        return this;
    }

    @Override
    public SpeedMatchBuilder setHandSizes(int startingHandSize, int maxHandSize) {
        product.setStartingHandSize(startingHandSize);
        product.setMaxHandSize(maxHandSize);
        return this;
    }

    @Override
    public SpeedMatchBuilder setTimer(long timePerPlayerMillis) {
        product.setTimePerPlayerMillis(timePerPlayerMillis);
        return this;
    }

    @Override
    public SpeedMatch getProduct() {
        return product;
    }
}