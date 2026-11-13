package game.server.cards;

import game.server.Match;
import game.server.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <b>Design pattern - Composite (Composite):</b> a unit on the field together
 * with any number of riders attached to it (see {@code Jumpy}). Component =
 * {@link Unit}, Leaves = ordinary units, child management =
 * {@link UnitContainer}. The rest of the game treats a group exactly like a
 * single unit - it sits in one zone, attacks, takes hits and dies.
 *
 * <p>The first child is always the carried unit, which the group stands in
 * for: name, cost, health, buffs-by-number and abilities like Archer
 * targeting all come from it. Every child, riders included, adds its damage
 * and gets its start-of-turn hook. Riders add no health and never take
 * damage - the carried unit takes every hit, and when it dies its riders go
 * back to their owner's hand (see Match.sendToGraveyardLayered).
 *
 * <p>Created only by {@link Unit#attach}, the moment a first rider lands on a
 * plain unit. Buffs always stay outside a group (UnitBuff passes attach
 * inward), so a group never has a buff inside it.
 */
public class UnitGroup extends Unit implements UnitContainer {

    private final List<Unit> children = new ArrayList<>();

    UnitGroup(Unit carried, Unit firstRider) {
        super(carried.getName(), carried.getDescription(), carried.getEnergyCost(),
                0, 0, carried.getElement(), carried.getClan());
        children.add(carried);
        children.add(firstRider);
    }

    private Unit carried() {
        return children.get(0);
    }

    @Override
    public void add(Unit child) {
        children.add(child);
    }

    /* A rider can leave; the carried unit can't, since the group is standing in for it. */
    @Override
    public void remove(Unit child) {
        if (child == carried()) {
            throw new IllegalArgumentException("A group can't lose the unit it carries");
        }
        children.remove(child);
    }

    @Override
    public List<Unit> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public Unit attach(Unit rider) {
        add(rider);
        return this;
    }

    @Override
    public int getRiderCount() {
        return children.size() - 1;
    }

    @Override
    public int getDamage() {
        int damage = 0;
        for (Unit child : children) {
            damage += child.getDamage();
        }
        return damage;
    }

    @Override
    public boolean isDamageBuffed() {
        if (getRiderCount() > 0) {
            return true;
        }
        for (Unit child : children) {
            if (child.isDamageBuffed()) {
                return true;
            }
        }
        return false;
    }

    /* A snapshot, in case a child's hook ever changes the group. */
    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        for (Unit child : List.copyOf(children)) {
            child.onOwnerTurnStart(match, owner);
        }
    }

    @Override
    public int getHealth() {
        return carried().getHealth();
    }

    @Override
    public int getMaxHealth() {
        return carried().getMaxHealth();
    }

    @Override
    public void takeDamage(int amount) {
        carried().takeDamage(amount);
    }

    @Override
    public void heal(int amount) {
        carried().heal(amount);
    }

    @Override
    public boolean isDead() {
        return carried().isDead();
    }

    @Override
    public boolean isHealthBuffed() {
        return carried().isHealthBuffed();
    }

    @Override
    public int getShieldCount() {
        return carried().getShieldCount();
    }

    @Override
    public Unit getBaseUnit() {
        return carried().getBaseUnit();
    }

    /* So an illusion carrying riders still vanishes with its original. */
    @Override
    public Unit getShallowOriginal() {
        return carried().getShallowOriginal();
    }

    @Override
    public void reset() {
        carried().reset();
    }

    /*
     * Match takes a dead group apart itself (riders to the hand, the carried
     * unit to the graveyard), so this is only a fallback: a group as a whole
     * is never a card anyone could get back.
     */
    @Override
    public Card prepareForGraveyard() {
        return carried().prepareForGraveyard();
    }

    /*
     * Copies leave the riders out: a copied rider would go to its owner's
     * hand when the copy died, turning one card into two. The copy is linked
     * to this group, the object that sits in the zone, so it disappears when
     * the group is destroyed.
     */
    @Override
    public Unit cloneShallow() {
        Unit copy = carried().cloneShallow();
        copy.setShallowOriginal(this);
        return copy;
    }

    @Override
    public Unit cloneDeep() {
        return carried().cloneDeep();
    }
}
