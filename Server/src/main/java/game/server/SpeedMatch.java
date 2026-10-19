package game.server;

/**
 * <b>Design pattern - Builder (Product).</b> The rules for a match with a
 * per-player clock. Built up one step at a time by
 * {@link SpeedMatchBuilder}; both a Speed match and a Chaos match produce
 * one of these (Chaos just fills it with random numbers - see
 * {@link MatchSettingsDirector}).
 *
 * <p>Holds everything {@link StandardMatch} does plus the clock, but does
 * NOT extend it - the five shared fields are repeated here on purpose, and
 * the two classes are tied together only by {@link MatchSettings}.
 */
public class SpeedMatch implements MatchSettings {

    private int startingHealth;
    private int startingEnergy;
    private int energyPerTurn;
    private int startingHandSize;
    private int maxHandSize;
    private long timePerPlayerMillis;

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

    /*
     * Overrides MatchSettings' "no clock" default - which is also what
     * makes hasTimer() report true for this class without it needing its
     * own override.
     */
    @Override
    public Long timePerPlayerMillis() {
        return timePerPlayerMillis;
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

    void setTimePerPlayerMillis(long timePerPlayerMillis) {
        this.timePerPlayerMillis = timePerPlayerMillis;
    }
}
