package game.server.cards.spells;

import game.server.cards.AdaptableSpell;
import game.server.cards.Card;
import game.server.cards.SpellType;

public class ShiftingSpellCard extends AdaptableSpell {

    public ShiftingSpellCard() {
        super(
                "Shifting Spell",
                "Adaptable: Buff ally or damage enemy or resurrect unit or draw cards based on target zone.",
                2
        );
    }

    public static Card create() {
        return new ShiftingSpellCard();
    }
}