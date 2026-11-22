package game.server.cards.state;

/**
 * Design pattern – State.
 *
 * Represents the 4 distinct lifecycle/combat states a Unit card can be in:
 * - IN_HAND: Card is in player's hand; unaffected by field effects, can only be played.
 * - NORMAL: On the field at full health with no active modifiers.
 * - AFFECTED: On the field with active modifiers (damaged, buffed, cursed, shielded).
 * - DEAD: Health depleted (&le; 0); destroyed and sent to graveyard.
 */
public enum UnitStateKind {
    IN_HAND,
    NORMAL,
    AFFECTED,
    DEAD
}
