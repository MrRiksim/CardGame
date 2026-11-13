package game.server.spectating;

/**
 * <b>Design pattern - Proxy (Protection Proxy):</b> stands between
 * spectators and the real match state and removes what nobody but the owner
 * may see - both hands (only their sizes stay) and the identity of every
 * face-down trap (only which zones are occupied stays). The real subject
 * still captures everything; this proxy is what guarantees none of it
 * reaches a spectator.
 */
public class HiddenInformationProxy implements MatchStateView {

    private final MatchStateView realState;

    public HiddenInformationProxy(MatchStateView realState) {
        this.realState = realState;
    }

    @Override
    public MatchSnapshot snapshot() {
        return realState.snapshot().withoutHiddenInformation();
    }
}
