package game.server;

/**
 * <b>Design pattern - Bridge (Concrete Implementor 1):</b> formats packet
 * data into the pipe-delimited plain text protocol expected by the client.
 */
public class PlainTextSerializer implements PacketSerializer {

    @Override
    public String serialize(String packetType, String content) {
        return packetType + "|" + content;
    }
}
