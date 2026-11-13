package game.server.spectating;

import game.server.Match;

/**
 * <b>Design pattern - Proxy (Real Subject):</b> reads the match directly.
 * Every snapshot it gives out is complete - both hands and every trap
 * included - and freshly captured from the match.
 *
 * <p>It is ready to serve the moment it exists: creating one captures a
 * full snapshot straight away and keeps the latest one it has taken. That
 * up-front capture, paid for every match whether anybody watches it or not,
 * is what {@link LazyStateProxy} puts off until a spectator actually asks.
 */
public class LiveMatchState implements MatchStateView {

    private final Match match;
    private MatchSnapshot latest;

    public LiveMatchState(Match match) {
        this.match = match;
        this.latest = match.captureSnapshot();
    }

    @Override
    public MatchSnapshot snapshot() {
        latest = match.captureSnapshot();
        return latest;
    }
}
