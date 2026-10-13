package game.server;

/**
 * Concrete Builder for a match with no clock. Holds one StandardMatch and
 * builds it up step by step. Single-use: the Director creates a fresh
 * builder for every match. The overrides use covariant return types so the
 * Director's call chain keeps its specific types all the way to
 * getProduct().
 */
public class StandardMatchBuilder implements MatchBuilder {

    private final StandardMatch product = new StandardMatch();

    @Override
    public StandardMatchBuilder setHealth(int health) {
        product.setStartingHealth(health);
        return this;
    }

    @Override
    public StandardMatchBuilder setEnergy(int startingEnergy, int energyPerTurn) {
        product.setStartingEnergy(startingEnergy);
        product.setEnergyPerTurn(energyPerTurn);
        return this;
    }

    @Override
    public StandardMatchBuilder setHandSizes(int startingHandSize, int maxHandSize) {
        product.setStartingHandSize(startingHandSize);
        product.setMaxHandSize(maxHandSize);
        return this;
    }

    @Override
    public StandardMatch getProduct() {
        return product;
    }
}