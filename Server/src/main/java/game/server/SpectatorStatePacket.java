package game.server;

import game.server.spectating.MatchSnapshot;
import game.server.spectating.SideSnapshot;

/**
 * <b>Design pattern - Bridge (Refined Abstraction 3):</b> a spectator's view
 * of a match, from a {@link MatchSnapshot}. Field format, top level split by
 * "|", with player 1 always before player 2:
 *
 * <pre>
 * SPECTATE_STATE|matchId|turnPlayerNumber|p1Health|p2Health|p1HandCount|p2HandCount|
 *                p1Hand|p2Hand|p1Front|p2Front|p1Back|p2Back|p1BackZones|p2BackZones|
 *                p1SecondsLeft|p2SecondsLeft|p1GraveyardTop|p2GraveyardTop
 * </pre>
 *
 * <p>Card lists use the same entry formats as STATE (see
 * {@code Match.buildStateFor}). A hand or back row that was hidden (see
 * {@code HiddenInformationProxy}) is sent as "?", which is different from an
 * empty one ("-").
 */
public class SpectatorStatePacket extends GamePacket {

    private static final String HIDDEN = "?";

    private final MatchSnapshot snapshot;

    public SpectatorStatePacket(MatchSnapshot snapshot, PacketSerializer serializer) {
        super(serializer);
        this.snapshot = snapshot;
    }

    @Override
    public String getPacketData() {
        if (packetData == null) {
            SideSnapshot p1 = snapshot.player1();
            SideSnapshot p2 = snapshot.player2();
            String body = snapshot.matchId()
                    + "|" + snapshot.turnPlayer()
                    + "|" + p1.health()
                    + "|" + p2.health()
                    + "|" + p1.handCount()
                    + "|" + p2.handCount()
                    + "|" + orHidden(p1.hand())
                    + "|" + orHidden(p2.hand())
                    + "|" + p1.front()
                    + "|" + p2.front()
                    + "|" + orHidden(p1.back())
                    + "|" + orHidden(p2.back())
                    + "|" + p1.backZones()
                    + "|" + p2.backZones()
                    + "|" + p1.secondsLeft()
                    + "|" + p2.secondsLeft()
                    + "|" + p1.graveyardTop()
                    + "|" + p2.graveyardTop();
            packetData = serializer.serialize("SPECTATE_STATE", body);
        }
        return packetData;
    }

    private static String orHidden(String value) {
        return value == null ? HIDDEN : value;
    }
}
