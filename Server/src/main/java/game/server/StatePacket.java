package game.server;

/**
 * <b>Design pattern - Bridge (Refined Abstraction 1):</b> represents a full
 * board state update packet sent to players.
 */
public class StatePacket extends GamePacket {

    private final String rawState;

    public StatePacket(String rawState, PacketSerializer serializer) {
        super(serializer);
        this.rawState = rawState;
    }

    @Override
    public String getPacketData() {
        if (packetData == null) {
            String body = rawState.startsWith("STATE|")
                    ? rawState.substring("STATE|".length())
                    : rawState;
            packetData = serializer.serialize("STATE", body);
        }
        return packetData;
    }
}
