package game.server.cards.state;

import game.server.cards.Unit;

/**
 * Design pattern – State.
 *
 * <p>Defines the state-dependent behavior and transitions for a {@link Unit}.
 *
 * <p>Concrete states:
 * <ul>
 *   <li>{@link InHandState}: Sitting in a player's hand. No field effects apply; can only be played.</li>
 *   <li>{@link NormalState}: On the field at full health with no active modifiers.</li>
 *   <li>{@link AffectedState}: On the field with active modifiers (damaged, buffed, cursed, etc.).</li>
 *   <li>{@link DeadState}: Health depleted; card is eliminated from the field.</li>
 * </ul>
 */
public interface UnitState {

    UnitStateKind getKind();

    /** Whether the unit in this state can be played onto the board from hand. */
    boolean canPlay();

    /** Whether the unit in this state can be targeted by healing spells. */
    boolean canHeal(Unit unit);

    /** Whether the unit in this state can be targeted by buff-stripping spells like Cripple. */
    boolean canCripple(Unit unit);

    /** Handles taking damage and transitioning to the appropriate next state. */
    void takeDamage(Unit unit, int amount);

    /** Handles receiving healing and transitioning to the appropriate next state. */
    void heal(Unit unit, int amount);
}
