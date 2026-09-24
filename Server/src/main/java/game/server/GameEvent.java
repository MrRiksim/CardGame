package game.server;

import game.server.cards.Unit;

/**
 * A lightweight notification describing something that just happened on the
 * board, used so face-down traps can check their own activation condition
 * (see {@code Trap.isTriggeredBy}). Extend {@link Type} as more trigger
 * conditions are needed later (a spell being cast, a unit dying, etc).
 */
public class GameEvent {

    public enum Type {
        UNIT_PLAYED
    }

    private final Type type;
    private final Player triggeringPlayer;
    private final Unit unit;

    public GameEvent(Type type, Player triggeringPlayer, Unit unit) {
        this.type = type;
        this.triggeringPlayer = triggeringPlayer;
        this.unit = unit;
    }

    public Type getType() {
        return type;
    }

    public Player getTriggeringPlayer() {
        return triggeringPlayer;
    }

    public Unit getUnit() {
        return unit;
    }
}
