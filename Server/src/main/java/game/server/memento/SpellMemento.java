package game.server.memento;

/**
 * Design pattern – Memento (Narrow Interface).
 *
 * <p>This interface exposes NO methods, providing a "narrow" view of the
 * snapshot to the Caretaker ({@link SpellMementoHistory}) and any other class.
 *
 * <p>Only the Originator ({@code Match}) implements the wide interface via a
 * private inner class, guaranteeing secure data restoration ("saugus duomenų
 * atstatymas") where no other class can inspect, access, or tamper with the
 * saved state.
 */
public interface SpellMemento {
}
