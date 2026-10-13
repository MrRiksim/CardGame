package game.server;

/**
 * Builder pattern - Builder. Each step now receives the actual numbers
 * instead of deciding them itself; a concrete builder just applies them to
 * the product it's holding. build() became getProduct().
 *
 * setTimer is an optional step with an empty default, which is how the
 * original pattern description suggests handling steps only some builders
 * care about: StandardMatchBuilder's product has no clock, so it simply
 * inherits the no-op, while SpeedMatchBuilder overrides it.
 */
public interface MatchBuilder {

    MatchBuilder setHealth(int health);

    MatchBuilder setEnergy(int startingEnergy, int energyPerTurn);

    MatchBuilder setHandSizes(int startingHandSize, int maxHandSize);

    default MatchBuilder setTimer(long timePerPlayerMillis) {
        return this;
    }

    MatchSettings getProduct();
}