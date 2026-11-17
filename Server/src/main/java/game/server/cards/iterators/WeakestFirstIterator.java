package game.server.cards.iterators;

import game.server.cards.Unit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Iterator: snapshots healable allies in ascending health order (zone order breaks ties). */
public final class WeakestFirstIterator implements Iterator<Unit> {
    private final List<Unit> targets = new ArrayList<>();
    private int index;

    public WeakestFirstIterator(Unit[] zones) {
        for (Unit unit : zones) {
            if (eligible(unit)) {
                targets.add(unit);
            }
        }
        targets.sort(Comparator.comparingInt(Unit::getHealth));
    }

    private static boolean eligible(Unit unit) {
        return unit != null && !unit.isDead()
                && unit.getHealth() < unit.getMaxHealth() && unit.getState().canHeal();
    }

    @Override
    public boolean hasNext() {
        while (index < targets.size() && !eligible(targets.get(index))) {
            index++;
        }
        return index < targets.size();
    }

    @Override
    public Unit next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return targets.get(index++);
    }
}
