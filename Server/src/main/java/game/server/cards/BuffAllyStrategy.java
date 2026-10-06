package game.server.cards;

import game.server.Match;
import game.server.Player;

public class BuffAllyStrategy implements SpellStrategy {
    private final int attackBuff;

    public BuffAllyStrategy(int attackBuff) {
        this.attackBuff = attackBuff;
    }

    @Override
    public void execute(Player caster, SpellTargetContext context, Match match) {
        Unit target = context.getTargetUnit();
        if (target != null) {
            Unit buffedUnit = new DamageBuffForStrategy(target, attackBuff);
            match.replaceUnit(target, buffedUnit);
        }
    }
}