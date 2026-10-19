package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;

public class WaterUnit extends Unit {

    public WaterUnit() {
        super(
                "Antaniukas",
                "He loves drinking water",
                1,
                1,
                1,
                Element.WATER,
                null
        );
    }

    public static WaterUnit create() {
        return new WaterUnit();
    }

    private WaterUnit(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    @Override
    public Unit cloneShallow() {
        WaterUnit clone = new WaterUnit(
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
        return new WaterUnit(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

}
