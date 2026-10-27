package game.server;

/**
 * <b>Design pattern - Bridge (Concrete Implementor 2):</b> formats packet
 * data into a JSON string representation.
 */
public class JsonSerializer implements PacketSerializer {

    @Override
    public String serialize(String packetType, String content) {
        String escaped = content == null ? "" : content.replace("\"", "\\\"");
        return "{\"type\":\"" + packetType + "\",\"data\":\"" + escaped + "\"}";
    }
}
