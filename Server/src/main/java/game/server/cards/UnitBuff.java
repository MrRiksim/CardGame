package game.server.cards;

import game.server.Match;
import game.server.Player;

import java.util.List;

/**
 * Design pattern - Decorator (Component = Unit, Decorator = this class,
 * ConcreteDecorators = the individual buffs). Live-delegating: the base unit
 * owns the one real health counter and every layer adds its own contribution
 * on top when asked. Damage passes through the layers from the outermost
 * (newest) inward: each layer's absorb() takes what it can and hands the rest
 * on, and whatever gets all the way through hurts the base unit. A layer with
 * nothing left reports isSpent() and shedSpentBuffs() splices it out.
 */
public abstract class UnitBuff extends Unit {

    /* Not final: repointed past a spent layer beneath this one. */
    private Unit wrapped;
    /*
     * The spell card this buff was created from. What goes to the graveyard
     * (and so what can be pulled back out of it) is this spell, never the buff
     * itself, which is a Unit and would be playable as one.
     */
    private final Spell source;

    protected UnitBuff(Unit wrapped, Spell source) {
        super(source.getName(), source.getDescription(), source.getEnergyCost(),
                0, 0, wrapped.getElement(), wrapped.getClan());
        this.wrapped = wrapped;
        this.source = source;
    }

    public Unit getWrapped() {
        return wrapped;
    }

    /* What this particular layer contributes; a buff overrides only what it affects. */
    protected int healthBonus() {
        return 0;
    }

    protected int damageBonus() {
        return 0;
    }

    protected int shieldLayers() {
        return 0;
    }
    public Spell getSource() {
        return source;
    }

    /* Takes its share of a hit, returns what is left to pass inward. Only called with damage > 0. */
    protected int absorb(int damage) {
        return damage;
    }

    protected boolean isSpent() {
        return false;
    }

    @Override
    public int getHealth() {
        return wrapped.getHealth() + healthBonus();
    }

    @Override
    public int getMaxHealth() {
        return wrapped.getMaxHealth() + healthBonus();
    }

    @Override
    public int getDamage() {
        return wrapped.getDamage() + damageBonus();
    }

    /* A hit for 0 isn't a hit - otherwise Tombstone (0 damage) would pop a Shield. */
    @Override
    public void takeDamage(int amount) {
        if (amount <= 0) {
            return;
        }
        wrapped.takeDamage(absorb(amount));
    }

    @Override
    public void heal(int amount) {
        wrapped.heal(amount);
    }

    @Override
    public boolean isDead() {
        return wrapped.isDead();
    }

    @Override
    public boolean isHealthBuffed() {
        return healthBonus() > 0 || wrapped.isHealthBuffed();
    }

    @Override
    public boolean isDamageBuffed() {
        return damageBonus() > 0 || wrapped.isDamageBuffed();
    }

    @Override
    public int getShieldCount() {
        return wrapped.getShieldCount() + shieldLayers();
    }

    @Override
    public Unit getBaseUnit() {
        return wrapped.getBaseUnit();
    }

    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        wrapped.onOwnerTurnStart(match, owner);
    }

    /* Outermost first, so spent layers land in `discarded` newest to oldest. */
    @Override
    public Unit shedSpentBuffs(List<Card> discarded) {
        boolean spent = isSpent();
        if (spent) {
            discarded.add(this);
        }
        wrapped = wrapped.shedSpentBuffs(discarded);
        return spent ? wrapped : this;
    }

    @Override
    public Card prepareForGraveyard() {
        return source.prepareForGraveyard();
    }

    @Override
    public void reset() {
        wrapped.reset();
    }

    @Override
    public Unit cloneShallow() {
        try {
            // Uses reflection to invoke the subclass constructor (Unit wrapped, Spell source)
            return this.getClass()
                    .getConstructor(Unit.class, Spell.class)
                    .newInstance(this.wrapped, this.source);
        } catch (Exception e) {
            throw new RuntimeException("Failed to shallow clone buff: " + getClass().getName(), e);
        }
    }

    @Override
    public Unit cloneDeep() {
        try {
            // Recursively deep clones the wrapped unit stack beneath this buff
            Unit clonedWrapped = (this.wrapped != null) ? this.wrapped.cloneDeep() : null;
            return this.getClass()
                    .getConstructor(Unit.class, Spell.class)
                    .newInstance(clonedWrapped, this.source);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep clone buff: " + getClass().getName(), e);
        }
    }
}