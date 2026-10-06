package game.server.cards;
import game.server.Match;
import game.server.Player;

public class SpellTargetContext {

    public enum TargetZone {
        ALLY_UNIT,
        ENEMY_UNIT,
        GRAVEYARD,
        DECK
    }

    private final TargetZone zone;
    private final Unit unit; // null if dropped on Deck or Graveyard

    public SpellTargetContext(TargetZone zone, Unit unit) {
        this.zone = zone;
        this.unit = unit;
    }

    public TargetZone getZone() {
        return zone;
    }

    public Unit getTargetUnit() {
        return unit;
    }
}
