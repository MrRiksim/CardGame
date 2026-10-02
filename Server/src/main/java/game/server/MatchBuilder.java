package game.server;

/**
 * <b>Design pattern - Builder.</b> Builder = this interface, Product =
 * {@link MatchSettings}, ConcreteBuilders = {@link StandardMatchBuilder},
 * {@link SpeedMatchBuilder} and {@link ChaosMatchBuilder}, Director =
 * {@link MatchSettingsDirector}.
 *
 * <p>Each step here just decides ONE piece of the settings - how it's
 * decided (a fixed number, a mode-specific number, or a random roll) is
 * entirely up to the concrete builder. The ORDER the steps get called in
 * is fixed and lives in the Director, not in any builder - swapping
 * Standard/Speed/Chaos never has to touch that sequence, which is what
 * separates this from simply being three overloads of a factory method.
 */
public interface MatchBuilder {

    MatchBuilder setHealth();

    MatchBuilder setEnergy();

    MatchBuilder setHandSizes();

    MatchBuilder setTimer();

    MatchSettings build();
}
