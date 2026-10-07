package game.server.cards;
import game.server.Match;
import game.server.Player;

/**
 * <b>Design pattern - Decorator.</b> Component = {@link Unit}, ConcreteComponent =
 * a drawable unit like Knight, Decorator = this class, ConcreteDecorator =
 * a specific buff (see {@code ArmorBuff}, {@code DamagePotionBuff}). A
 * buffed unit is just another {@code Unit} as far as the rest of the game
 * is concerned - it sits in the same front-zone slot, takes damage and
 * attacks exactly the same way - which is the whole point of decorating
 * instead of, say, adding a "buffs" list field to Unit that every call site
 * would need to remember to account for.
 *
 * <p>Unlike a textbook decorator that forwards every call live to the
 * wrapped object, this one takes a one-time snapshot of the wrapped unit's
 * current stats (plus its own bonus) at construction time and becomes the
 * new authoritative Unit from then on. That avoids a subtle bug a live
 * "wrapped.getHealth() + bonus" decorator would have: health is clamped at
 * 0, so a permanently-reapplied bonus on top of an already-dead wrapped
 * unit would make that unit impossible to actually kill. Taking a snapshot
 * means there is always exactly one real health counter in play - the
 * current outermost layer's own - no matter how many buffs are stacked.
 *
 * <p>{@code wrapped} is kept purely so a dying unit can be unwound layer by
 * layer - see {@code Match.destroyUnit} - newest buff first, oldest buff
 * second, and finally the original unit card, each sent to the graveyard
 * as its own card.
 */
public abstract class UnitBuff extends Unit {

    private final Unit wrapped;

    protected UnitBuff(
            String name,
            String description,
            int energyCost,
            Unit wrapped,
            int bonusHealth,
            int bonusDamage) {

        super(
                name,
                description,
                energyCost,
                wrapped.getHealth() + bonusHealth,
                wrapped.getMaxHealth() + bonusHealth,
                wrapped.getDamage() + bonusDamage,
                wrapped.getElement(),
                wrapped.getClan()
        );

        this.wrapped = wrapped;
    }

    public Unit getWrapped() {
        return wrapped;
    }

    @Override
    public Unit getBaseUnit() {
        return wrapped.getBaseUnit();
    }

    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        getWrapped().onOwnerTurnStart(match, owner);
    }
}
