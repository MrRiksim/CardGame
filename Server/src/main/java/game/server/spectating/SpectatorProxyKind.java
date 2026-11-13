package game.server.spectating;

/**
 * Which proxy spectators read a match through - see
 * {@link MatchStateViews#ACTIVE_PROXY}. Only PROTECTION hides hands and
 * traps; the other two show spectators everything and are there to compare
 * the proxy kinds (benchmarks, demos).
 */
public enum SpectatorProxyKind {
    PROTECTION,
    CACHING,
    DELAYED_CREATION
}
