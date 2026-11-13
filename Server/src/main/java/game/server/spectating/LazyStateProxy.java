package game.server.spectating;

import java.util.function.Supplier;

/**
 * <b>Design pattern - Proxy (Virtual Proxy, delayed creation):</b> stands in
 * for the real subject without creating it. The real {@link LiveMatchState}
 * - and the full snapshot it captures the moment it's made - only comes into
 * existence the first time somebody asks for a snapshot. Every match gets a
 * view when it starts, but most are never watched, so most never pay for
 * one.
 */
public class LazyStateProxy implements MatchStateView {

    private final Supplier<MatchStateView> realStateFactory;
    private MatchStateView realState;

    public LazyStateProxy(Supplier<MatchStateView> realStateFactory) {
        this.realStateFactory = realStateFactory;
    }

    @Override
    public MatchSnapshot snapshot() {
        if (realState == null) {
            realState = realStateFactory.get();
        }
        return realState.snapshot();
    }

    public boolean isRealStateCreated() {
        return realState != null;
    }
}
