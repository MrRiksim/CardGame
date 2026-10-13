package game.server;

/**
 * <b>Design pattern - Builder (Product).</b> The rules for a match with no
 * clock. Built up one step at a time by {@link StandardMatchBuilder}; the
 * setters are package-private on purpose, so the builder (same package) is
 * the only thing meant to ever write to one of these. Everything else only
 * reads it through {@link MatchSettings}.
 *
 * <p>Deliberately separate from {@link SpeedMatch} rather than a parent of
 * it - the two share only the {@link MatchSettings} contract.
 */
public class StandardMatch implements MatchSettings {

    private int startingHealth;
    private int startingEnergy;
    private int energyPerTurn;
    private int startingHandSize;
    private int maxHandSize;

    @Override
    public int startingHealth() {
        return startingHealth;
    }

    @Override
    public int startingEnergy() {
        return startingEnergy;
    }

    @Override
    public int energyPerTurn() {
        return energyPerTurn;
    }

    @Override
    public int startingHandSize() {
        return startingHandSize;
    }

    @Override
    public int maxHandSize() {
        return maxHandSize;
    }

    void setStartingHealth(int startingHealth) {
        this.startingHealth = startingHealth;
    }

    void setStartingEnergy(int startingEnergy) {
        this.startingEnergy = startingEnergy;
    }

    void setEnergyPerTurn(int energyPerTurn) {
        this.energyPerTurn = energyPerTurn;
    }

    void setStartingHandSize(int startingHandSize) {
        this.startingHandSize = startingHandSize;
    }

    void setMaxHandSize(int maxHandSize) {
        this.maxHandSize = maxHandSize;
    }
}
