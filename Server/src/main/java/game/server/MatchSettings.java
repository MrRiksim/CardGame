package game.server;

/**
 * Builder pattern - the contract both products (StandardMatch, SpeedMatch)
 * satisfy, so Match and Player can read either one without caring which.
 * It holds no data itself.
 */
public interface MatchSettings {

    int startingHealth();
    int startingEnergy();
    int energyPerTurn();
    int startingHandSize();
    int maxHandSize();

    /*
     * null means "no clock". Only a product that actually has one
     * (SpeedMatch) overrides this.
     */
    default Long timePerPlayerMillis() {
        return null;
    }

    default boolean hasTimer() {
        return timePerPlayerMillis() != null;
    }
}