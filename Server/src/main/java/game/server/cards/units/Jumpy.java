package game.server.cards.units;

import game.server.cards.Element;
import game.server.cards.Unit;

/**
 * A weak unit that can also be attached to one of its owner's units as a
 * rider, adding its damage to that unit (see Unit.attach and UnitGroup). A
 * rider goes back to its owner's hand when the unit it rides dies; a Jumpy
 * played on an empty field is just a normal unit and dies normally.
 */
public class Jumpy extends Unit {

    private Jumpy() {
        super(
                "Jumpy",
                "Play it as a unit or drop it on your own unit to add its damage. Jumps back to your hand when that unit dies.",
                1,
                1,
                1,
                Element.WIND,
                null
        );
    }

    private Jumpy(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }

    @Override
    public boolean canRide() {
        return true;
    }

    @Override
    public Unit cloneShallow() {
        Jumpy clone = new Jumpy(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
        clone.setShallowOriginal(this);
        return clone;
    }

    @Override
    public Unit cloneDeep() {
        return new Jumpy(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static Jumpy create() {
        return new Jumpy();
    }
}
