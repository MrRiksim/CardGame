package game.server.cards.units;

import game.server.cards.Element;
import game.server.cards.Unit;

public class Archer extends Unit {

    private Archer() {
        super(
                "Archer",
                "nimble sniper",
                2,
                2,
                1,
                Element.EARTH,
                null
        );
    }

    private Archer(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    // Inside your standard Unit subclass (e.g., Archer.java or a concrete unit base):
    @Override
    public Unit cloneShallow() {
        Archer clone = new Archer(
                getName(),
                getDescription(),
                getEnergyCost(),
                getHealth(),
                getMaxHealth(),
                getDamage(),
                getElement(),
                getClan()
        );
        // Link the clone to this original unit
        clone.setShallowOriginal(this);
        return clone;
    }

    @Override
    public Unit cloneDeep() {
        // Return a new instance using current stats and a fresh ID
        return new Archer(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

    /*
     * Factory Method - CardFactory calls this instead of "new Knight()"
     * directly, so the creation details stay with the card that knows them.
     */
    public static Archer create() {
        return new Archer();
    }
}
