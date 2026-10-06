package game.server.cards;

public class DamageBuffForStrategy extends UnitBuff{
    public DamageBuffForStrategy(Unit wrapped, int bonusDamage) {
        super(
                wrapped.getName() + " (+Buff)",
                wrapped.getDescription(),
                0,            // Buff energy cost
                wrapped,
                0,            // bonusHealth
                bonusDamage   // bonusDamage
        );
    }
}
