package game.server;

/**
 * Concrete Builder for a match with a per-player clock. Holds one
 * SpeedMatchSettings. It doesn't extend StandardMatchSettingsBuilder, matching how the
 * products themselves are kept separate, so the three shared steps are
 * repeated here.
 */
public class SpeedMatchSettingsBuilder implements MatchSettingsBuilder {

    private final SpeedMatchSettings product = new SpeedMatchSettings();

    @Override
    public SpeedMatchSettingsBuilder setHealth(int health) {
        product.setStartingHealth(health);
        return this;
    }

    @Override
    public SpeedMatchSettingsBuilder setEnergy(int startingEnergy, int energyPerTurn) {
        product.setStartingEnergy(startingEnergy);
        product.setEnergyPerTurn(energyPerTurn);
        return this;
    }

    @Override
    public SpeedMatchSettingsBuilder setHandSizes(int startingHandSize, int maxHandSize) {
        product.setStartingHandSize(startingHandSize);
        product.setMaxHandSize(maxHandSize);
        return this;
    }

    @Override
    public SpeedMatchSettingsBuilder setTimer(long timePerPlayerMillis) {
        product.setTimePerPlayerMillis(timePerPlayerMillis);
        return this;
    }

    @Override
    public SpeedMatchSettings getProduct() {
        return product;
    }
}