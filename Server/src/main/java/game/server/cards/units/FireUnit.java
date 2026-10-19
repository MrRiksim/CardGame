package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;

public class FireUnit extends Unit {

    public FireUnit() {
        super(
                "Pyro",
                "mmpphhgggffff",
                1,
                2,
                2,
                Element.FIRE,
                null
        );
    }

    public static FireUnit create() {
        return new FireUnit();
    }

    private FireUnit(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    @Override
    public Unit cloneShallow() {
        FireUnit clone = new FireUnit(
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
        return new FireUnit(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

}
