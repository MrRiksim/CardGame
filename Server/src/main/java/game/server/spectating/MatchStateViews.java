package game.server.spectating;

import game.server.Match;

/**
 * Creates the {@link MatchStateView} spectators of a match read through.
 * Switching proxy kinds is this one setting; nothing else in the server
 * changes, since everything downstream only sees the MatchStateView
 * interface.
 *
 * <p><b>Only {@link SpectatorProxyKind#PROTECTION} hides hands and traps.</b>
 * With CACHING or DELAYED_CREATION a spectator sees every card - including a
 * player spectating their own match from a second client - so those two are
 * for benchmarks and demos only.
 */
public final class MatchStateViews {

    public static final SpectatorProxyKind ACTIVE_PROXY = SpectatorProxyKind.PROTECTION;

    private MatchStateViews() {
    }

    public static MatchStateView create(Match match) {
        return create(match, ACTIVE_PROXY);
    }

    public static MatchStateView create(Match match, SpectatorProxyKind kind) {
        return switch (kind) {
            case PROTECTION -> new HiddenInformationProxy(new LiveMatchState(match));
            case CACHING -> new CachingStateProxy(new LiveMatchState(match), match);
            case DELAYED_CREATION -> new LazyStateProxy(() -> new LiveMatchState(match));
        };
    }
}
