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

        strategies.put(SpellTargetContext.TargetZone.ALLY_UNIT, new BuffAllyStrategy());
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
    public boolean canTarget(Match match, Player caster, Unit target) {
        // If a unit target is provided, it's valid (ally or enemy unit)
        if (target != null) {
            return match.ownerOf(target) == caster || match.getOpponent(caster) == match.ownerOf(target);
        }

        // If no unit target is provided, it must be dropped on Deck or Graveyard UI elements.
        // If your client sends explicit context for Deck/Graveyard drops, validate them here.
        // For now, returning true only if targeted correctly, but let's prevent empty board drops.
        return false;
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (target != null) {
            SpellTargetContext.TargetZone zone = (match.getOpponent(caster) == match.ownerOf(target))
                    ? SpellTargetContext.TargetZone.ENEMY_UNIT
                    : SpellTargetContext.TargetZone.ALLY_UNIT;
            applyAdaptable(match, caster, new SpellTargetContext(zone, target));
        } else {
            // REMOVE the automatic fallback if dropped on an empty field zone.
            // Only execute deck/graveyard strategies if explicitly dropped on those UI targets.
            // (Depending on how your client passes deck/graveyard targets, check context here).
            return;
        }
    }
}