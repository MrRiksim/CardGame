package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.TrapCreator;
import game.server.cards.Unit;
import game.server.cards.traps.creators.BearTrapCreator;

public class Hunter extends Unit {
    private final TrapCreator creator = new BearTrapCreator();

    private Hunter() {
        super(
                "Hunter",
                "At the beginning of a turn places a bear trap to snare enemy units",
                2,
                1,
                1,
                Element.EARTH,
                null
        );
    }

    private Hunter(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    // Inside your standard Unit subclass (e.g., Archer.java or a concrete unit base):
    @Override
    public Unit cloneShallow() {
        Hunter clone = new Hunter(
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
        return new Hunter(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

    public static Hunter create() {
        return new Hunter();
    }

    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        creator.spawn(match, owner);
    }

}
