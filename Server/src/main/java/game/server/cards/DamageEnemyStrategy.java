package game.server.cards;

import game.server.Match;
import game.server.Player;

public class DamageEnemyStrategy implements SpellStrategy {
    private final int damageAmount;

    public DamageEnemyStrategy(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    @Override
    public void execute(Player caster, SpellTargetContext context, Match match) {
        Unit target = context.getTargetUnit();
        if (target != null) {
            target.takeDamage(damageAmount);
            if (target.isDead()) {
                match.destroyUnit(target);
            }
        }
    }
}