package game.server;

public class Card {

    private final String id;
    private final CardType type;
    private int zoneIndex = -1;

    public Card(String id, CardType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public CardType getType() {
        return type;
    }

    public int getZoneIndex() {
        return zoneIndex;
    }

    public void setZoneIndex(int zoneIndex) {
        this.zoneIndex = zoneIndex;
    }
}
