package game.server;

/**
 * <b>Design pattern - Bridge (Implementor):</b> defines the interface for
 * serializing packet data into a specific wire format.
 */
public interface PacketSerializer {

    String serialize(String packetType, String content);
}
