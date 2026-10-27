package game.server;

/**
 * <b>Design pattern - Observer:</b> interface implemented by listeners (such as
 * {@link PlayerConnection}) that observe a {@link Match} and receive updates via
 * {@link GamePacket}.
 */
public interface MatchObserver {

    void onMatchUpdate(GamePacket packet);
}
