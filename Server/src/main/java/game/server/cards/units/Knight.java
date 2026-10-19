package game.server.cards.units;

import game.server.cards.Element;
import game.server.cards.Unit;

public class Knight extends Unit {

    private Knight() {
        super(
                "Knight",
                "A sturdy frontline warrior",
                2,
                3,
                1,
                Element.EARTH,
                null
        );
    }

    private Knight(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    // Inside your standard Unit subclass (e.g., Archer.java or a concrete unit base):
    @Override
    public Unit cloneShallow() {
        Knight clone = new Knight(
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
        return new Knight(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

    /*
     * Factory Method - CardFactory calls this instead of "new Knight()"
     * directly, so the creation details stay with the card that knows them.
     */
    public static Knight create() {
        return new Knight();
    }
}
