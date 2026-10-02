package game.server;

/**
 * Concrete Builder - identical to Standard except each player gets a
 * 10-minute total clock that runs down during their own turns.
 */
public class SpeedMatchBuilder extends StandardMatchBuilder {

    static final long TEN_MINUTES_MILLIS = 10L * 60 * 1000;

    @Override
    public MatchBuilder setTimer() {
        timePerPlayerMillis = TEN_MINUTES_MILLIS;
        return this;
    }
}
