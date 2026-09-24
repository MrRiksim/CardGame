package game.server.cards;

import game.server.Match;
import game.server.Player;

/**
 * A card resolved immediately when played, then sent to the graveyard.
 * Where it can be targeted depends on {@link #getSpellType()}: DAMAGE only
 * at enemy units, BUFF only at friendly units, SPECIAL anywhere (including
 * no target at all).
 *
 * <p><b>Design pattern note - Strategy:</b> {@link #apply} is each spell's own
 * self-contained algorithm for what it does. {@link Match} doesn't need an
 * if/else per spell name - it just calls {@code apply(...)} on whichever
 * spell was played and lets that object's own logic run.
 */
public abstract class Spell extends Card {

    private final SpellType spellType;

    protected Spell(String name, String description, int energyCost, SpellType spellType) {
        super(name, description, energyCost);
        this.spellType = spellType;
    }

    public SpellType getSpellType() {
        return spellType;
    }

    /*
     * Resolves this spell's effect. `target` is the unit the spell was
     * played on - required for DAMAGE/BUFF spells, may be null for SPECIAL
     * spells that don't target a single unit.
     */
    public abstract void apply(Match match, Player caster, Unit target);
}
