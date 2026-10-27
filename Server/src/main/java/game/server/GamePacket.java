package game.server;

/**
 * <b>Design pattern - Bridge (Abstraction):</b> base class for network packets,
 * holding a reference to a {@link PacketSerializer} implementor so that packet
 * types and serialization formats can vary independently.
 */
public abstract class GamePacket {

    protected String packetData;
    protected final PacketSerializer serializer;

    protected GamePacket(PacketSerializer serializer) {
        this.serializer = serializer;
    }

    public abstract String getPacketData();
}
