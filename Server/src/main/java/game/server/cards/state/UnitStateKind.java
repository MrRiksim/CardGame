package game.server.cards.state;

/**
 * Design pattern – State.
 *
 * Represents the 4 distinct combat states a Unit can be in:
 * - NORMAL: Default state, at full health, no buffs.
 * - INJURED: Current health is less than maximum health. Can be healed.
 * - BUFFED: Unit has active buffs. Can be crippled.
 * - DEAD: Health is depleted (&le; 0). Card is destroyed / placed in graveyard.
 */
public enum UnitStateKind {
    NORMAL,
    INJURED,
    BUFFED,
    DEAD
}
