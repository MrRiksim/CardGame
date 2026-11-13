package game.server.spectating;

import game.server.Match;

/**
 * <b>Design pattern - Proxy (Smart Proxy, added functionality):</b> keeps
 * the last snapshot and hands it out again for as long as the match hasn't
 * changed, instead of asking the real subject to capture a new one. A timed
 * match is re-broadcast every second even when nobody acts, and every
 * rejected play is broadcast too, so most requests can be answered from the
 * cache.
 *
 * <p>"Changed" means the match's state version moved (see
 * {@code Match.getStateVersion}) or either clock ticked over a second,
 * since the clocks are part of the snapshot.
 */
public class CachingStateProxy implements MatchStateView {

    private final MatchStateView realState;
    private final Match match;

    private MatchSnapshot cached;
    private long cachedVersion;
    private int cachedPlayer1Seconds;
    private int cachedPlayer2Seconds;

    public CachingStateProxy(MatchStateView realState, Match match) {
        this.realState = realState;
        this.match = match;
    }

    @Override
    public MatchSnapshot snapshot() {
        long version = match.getStateVersion();
        int player1Seconds = match.getSecondsLeft(match.getPlayer1());
        int player2Seconds = match.getSecondsLeft(match.getPlayer2());

        if (cached == null || version != cachedVersion
                || player1Seconds != cachedPlayer1Seconds || player2Seconds != cachedPlayer2Seconds) {
            cached = realState.snapshot();
            cachedVersion = version;
            cachedPlayer1Seconds = player1Seconds;
            cachedPlayer2Seconds = player2Seconds;
        }
        return cached;
    }
}
