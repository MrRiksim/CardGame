package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;

public class EarthUnit extends Unit {

    public EarthUnit() {
        super(
                "BroKarolis",
                "Rocking the trendiest broccoli haircut",
                1,
                2,
                1,
                Element.EARTH,
                null
        );
    }
    private EarthUnit(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    // Inside your standard Unit subclass (e.g., Archer.java or a concrete unit base):
    @Override
    public Unit cloneShallow() {
        EarthUnit clone = new EarthUnit(
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
        return new EarthUnit(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }
    public static EarthUnit create() {
        return new EarthUnit();
    }

    private EarthUnit(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    @Override
    public Unit cloneShallow() {
        EarthUnit clone = new EarthUnit(
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
        return new EarthUnit(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

}
