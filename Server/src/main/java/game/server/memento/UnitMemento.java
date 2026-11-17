package game.server.memento;

/** Opaque unit snapshot. Only the originating unit knows the saved private fields. */
@FunctionalInterface
public interface UnitMemento {
    void restore();
}
