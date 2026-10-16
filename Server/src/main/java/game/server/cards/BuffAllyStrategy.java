package game.server.cards;

import game.server.Match;
import game.server.Player;
import game.server.cards.spells.ShiftingSpellCard;

public class BuffAllyStrategy implements SpellStrategy {
    @Override
    public void execute(Player caster, SpellTargetContext context, Match match) {
        Unit target = context.getTargetUnit();

        ShiftingSpellCard spell = new ShiftingSpellCard(); // sitas labai blogai, nezinau kaip fixint kol kas xddddd

        if (target != null) {
            Unit buffedUnit = new DamageBuffForStrategy(target, spell);
            match.replaceUnit(target, buffedUnit);
        }
    }
}