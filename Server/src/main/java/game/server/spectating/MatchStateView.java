package game.server.spectating;

/**
 * <b>Design pattern - Proxy (Subject):</b> what spectators read a match
 * through. {@link LiveMatchState} is the real subject; the proxies
 * ({@link HiddenInformationProxy}, {@link CachingStateProxy},
 * {@link LazyStateProxy}) implement this same interface, so whoever holds a
 * view - a {@link SpectatorRoom} - never knows which one it has.
 * {@link MatchStateViews} decides that.
 */
public interface MatchStateView {

    MatchSnapshot snapshot();
}
