package game.server.cards.iterators;

import game.server.cards.Unit;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Iterator: visits living targets once, starting at a selected zone and wrapping right. */
public final class CircularTargetIterator implements Iterator<Unit> {
    private final Unit[] zones;
    private final int start;
    private int visited;

    public CircularTargetIterator(Unit[] zones, int start) {
        if (start < 0 || start >= zones.length) {
            throw new IllegalArgumentException("Start must be a valid zone");
        }
        this.zones = zones.clone();
        this.start = start;
    }

    @Override
    public boolean hasNext() {
        while (visited < zones.length) {
            Unit unit = zones[(start + visited) % zones.length];
            if (unit != null && !unit.isDead()) {
                return true;
            }
            visited++;
        }
        return false;
    }

    @Override
    public Unit next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return zones[(start + visited++) % zones.length];
    }
}
