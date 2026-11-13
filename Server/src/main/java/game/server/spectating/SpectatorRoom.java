package game.server.spectating;

import game.server.GameMode;
import game.server.PlainTextSerializer;
import game.server.PlayerConnection;
import game.server.SpectatorStatePacket;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Design pattern - Proxy (Client):</b> the spectators of one match and the
 * {@link MatchStateView} they all read it through. It only knows the
 * interface, never which proxy (if any) is behind it.
 *
 * <p>One snapshot is taken per broadcast and sent to every spectator, and
 * none at all while nobody is watching - which is what lets
 * {@link LazyStateProxy} skip creating the real state for unwatched matches.
 */
public class SpectatorRoom {

    private final int matchId;
    private final GameMode mode;
    private final MatchStateView view;
    private final List<PlayerConnection> spectators = new ArrayList<>();

    public SpectatorRoom(int matchId, GameMode mode, MatchStateView view) {
        this.matchId = matchId;
        this.mode = mode;
        this.view = view;
    }

    public int getMatchId() {
        return matchId;
    }

    public GameMode getMode() {
        return mode;
    }

    public void add(PlayerConnection spectator) {
        spectators.add(spectator);
        spectator.send("SPECTATING|" + matchId);
        spectator.send(statePacketData());
    }

    public void remove(PlayerConnection spectator) {
        spectators.remove(spectator);
    }

    public List<PlayerConnection> getSpectators() {
        return List.copyOf(spectators);
    }

    public void broadcast() {
        if (spectators.isEmpty()) {
            return;
        }
        String data = statePacketData();
        for (PlayerConnection spectator : new ArrayList<>(spectators)) {
            spectator.send(data);
        }
    }

    /* winnerNumber is 1 or 2. Spectators stay connected; they just stop getting updates. */
    public void endMatch(int winnerNumber) {
        for (PlayerConnection spectator : new ArrayList<>(spectators)) {
            spectator.send("MATCH_OVER|" + winnerNumber);
        }
        spectators.clear();
    }

    private String statePacketData() {
        return new SpectatorStatePacket(view.snapshot(), new PlainTextSerializer()).getPacketData();
    }
}
