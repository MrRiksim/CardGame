package game.server.cards.state;

import game.server.cards.Unit;

/**
 * Design pattern – State.
 *
 * <p>Defines the state-dependent behavior and transitions for a {@link Unit} on the field.
 *
 * <p>Concrete states:
 * <ul>
 *   <li>{@link NormalState}: Default state (full health, unbuffed).</li>
 *   <li>{@link InjuredState}: Current health is below maximum health; can be targeted by Heal.</li>
 *   <li>{@link BuffedState}: Unit has active buffs; can be targeted by Cripple.</li>
 *   <li>{@link DeadState}: Health depleted; card is eliminated from the field.</li>
 * </ul>
 */
public interface UnitState {

    UnitStateKind getKind();

    /**
     * Whether the unit in this state can be targeted by healing spells (only INJURED units).
     */
    boolean canHeal();

    /**
     * Whether the unit in this state can be targeted by buff-stripping spells like Cripple (only BUFFED units).
     */
    boolean canCripple();

    /**
     * Handles taking damage and transitioning to the appropriate next state.
     */
    void takeDamage(Unit unit, int amount);

    /**
     * Handles receiving healing and transitioning to the appropriate next state.
     */
    void heal(Unit unit, int amount);
}
