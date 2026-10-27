package game.server;

/**
 * <b>Design pattern - Bridge (Refined Abstraction 2):</b> represents a packet
 * carrying a {@link GameEvent} notification.
 */
public class EventPacket extends GamePacket {

    private final GameEvent event;

    public EventPacket(GameEvent event, PacketSerializer serializer) {
        super(serializer);
        this.event = event;
    }

    public GameEvent getEvent() {
        return event;
    }

    @Override
    public String getPacketData() {
        if (packetData == null) {
            String unitName = event.getUnit() != null ? event.getUnit().getName() : "NONE";
            String payload = event.getType().name() + "|" + unitName;
            packetData = serializer.serialize("EVENT", payload);
        }
        return packetData;
    }
}
