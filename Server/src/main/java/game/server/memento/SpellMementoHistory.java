package game.server.memento;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Design pattern – Memento (Caretaker).
 *
 * <p>Holds the undo stack of {@link SpellMemento} objects produced during a
 * single turn.  The Caretaker's one rule: it stores mementos and hands them
 * back, but it never inspects or modifies their contents.  All knowledge
 * about what a memento means lives exclusively in {@code Match} (the
 * Originator).
 *
 * <p>The stack is cleared at the end of every turn so a player can never
 * undo actions from a previous turn.
 */
public final class SpellMementoHistory {

    private final Deque<SpellMemento> stack = new ArrayDeque<>();

    /** Pushes a new memento onto the top of the undo stack. */
    public void push(SpellMemento memento) {
        stack.push(memento);
    }

    /**
     * Removes and returns the most recent memento, or {@code null} if the
     * stack is empty.
     */
    public SpellMemento pop() {
        return stack.isEmpty() ? null : stack.pop();
    }

    /** Returns {@code true} if there is at least one spell that can be undone. */
    public boolean hasUndo() {
        return !stack.isEmpty();
    }

    /** Discards all saved mementos (called at end-of-turn). */
    public void clear() {
        stack.clear();
    }
}
