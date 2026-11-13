package game.server.cards;

import java.util.List;

/**
 * <b>Design pattern - Composite (child management):</b> the add/remove/children
 * methods of the pattern, kept in their own interface so the code can switch
 * between the two textbook variants with a one-line change:
 *
 * <ul>
 *   <li><b>Safe (current):</b> only {@link UnitGroup} implements this, so a
 *       plain unit simply has no add/remove methods to misuse.</li>
 *   <li><b>Transparent:</b> add {@code implements UnitContainer} to
 *       {@link Unit}'s declaration. Every unit then has these methods, and a
 *       leaf falls back on the defaults below (refuses children, reports
 *       none).</li>
 * </ul>
 *
 * <p>Callers check {@code instanceof UnitContainer} and treat an empty child
 * list as a leaf, which is correct in both variants, so nothing else needs to
 * change when switching. Callers also step past any {@link UnitBuff} layers
 * first: buffs are decorators around a unit, not part of the tree.
 */
public interface UnitContainer {

    default void add(Unit child) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " can't hold other units");
    }

    default void remove(Unit child) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " can't hold other units");
    }

    default List<Unit> getChildren() {
        return List.of();
    }
}
