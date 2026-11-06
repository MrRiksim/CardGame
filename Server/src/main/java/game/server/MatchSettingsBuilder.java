package game.server;

/**
 * Builder pattern - Builder. Each step now receives the actual numbers
 * instead of deciding them itself; a concrete builder just applies them to
 * the product it's holding. build() became getProduct().
 *
 * setTimer is an optional step with an empty default, which is how the
 * original pattern description suggests handling steps only some builders
 * care about: StandardMatchSettingsBuilder's product has no clock, so it simply
 * inherits the no-op, while SpeedMatchSettingsBuilder overrides it.
 */
public interface MatchSettingsBuilder {

    MatchSettingsBuilder setHealth(int health);

    MatchSettingsBuilder setEnergy(int startingEnergy, int energyPerTurn);

    MatchSettingsBuilder setHandSizes(int startingHandSize, int maxHandSize);

    default MatchSettingsBuilder setTimer(long timePerPlayerMillis) {
        return this;
    }

    MatchSettings getProduct();
}