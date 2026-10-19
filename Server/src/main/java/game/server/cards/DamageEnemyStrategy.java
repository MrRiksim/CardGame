package game.server.cards;

import game.server.GameStatistics;
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
            int healthBefore = target.getHealth();

            target.takeDamage(damageAmount);

            GameStatistics.getInstance().recordDamageDealt(
                    healthBefore - target.getHealth()
            );

            if (target.isDead()) {
                match.destroyUnit(target);
            }
        }
    }
}