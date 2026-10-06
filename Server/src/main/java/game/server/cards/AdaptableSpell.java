package game.server.cards;

import game.server.Match;
import game.server.Player;
import java.util.EnumMap;
import java.util.Map;

public class AdaptableSpell extends Spell {

    private final Map<SpellTargetContext.TargetZone, SpellStrategy> strategies =
            new EnumMap<>(SpellTargetContext.TargetZone.class);

    public AdaptableSpell(String name, String description, int energyCost) {
        super(name, description, energyCost, SpellType.SPECIAL);

        strategies.put(SpellTargetContext.TargetZone.ALLY_UNIT, new BuffAllyStrategy(1));
        strategies.put(SpellTargetContext.TargetZone.ENEMY_UNIT, new DamageEnemyStrategy(2));
        strategies.put(SpellTargetContext.TargetZone.GRAVEYARD, new ResurrectToHandStrategy());
        strategies.put(SpellTargetContext.TargetZone.DECK, new DrawFromDeckStrategy(2));
    }

    public void applyAdaptable(Match match, Player caster, SpellTargetContext context) {
        SpellStrategy strategy = strategies.get(context.getZone());
        if (strategy != null) {
            strategy.execute(caster, context, match);
        }
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target != null) {
            SpellTargetContext.TargetZone zone = (match.getOpponent(caster) == match.ownerOf(target))
                    ? SpellTargetContext.TargetZone.ENEMY_UNIT
                    : SpellTargetContext.TargetZone.ALLY_UNIT;
            applyAdaptable(match, caster, new SpellTargetContext(zone, target));
        }
    }
}