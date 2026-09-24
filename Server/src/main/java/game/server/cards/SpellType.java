package game.server.cards;

public enum SpellType {
    /*
     * Can only be played targeting an enemy unit.
     */
    DAMAGE,

    /*
     * Can only be played targeting a friendly unit.
     */
    BUFF,

    /*
     * Can be played without needing a unit target at all.
     */
    SPECIAL
}
