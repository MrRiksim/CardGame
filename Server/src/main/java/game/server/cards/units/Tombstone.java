package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.Unit;
import game.server.cards.UnitCreator;
import game.server.cards.units.creators.SkellyCreator;

public class Tombstone extends Unit {

    /*
     * Held, not inherited - Tombstone HAS a creator rather than BEING one,
     * which is what lets this same onOwnerTurnStart hook work for any
     * future spawner card just by swapping in a different UnitCreator.
     */
    private final UnitCreator creator = new SkellyCreator();

    private Tombstone() {
        super(
                "Tombstone",
                "Each turn its owner takes it raises a free Skelly in an open zone",
                3,
                4,
                0,
                Element.EARTH,
                "Undead"
        );
    }

    private Tombstone(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    // Inside your standard Unit subclass (e.g., Archer.java or a concrete unit base):
    @Override
    public Unit cloneShallow() {
        Tombstone clone = new Tombstone(
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
        return new Tombstone(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

    public static Tombstone create() {
        return new Tombstone();
    }

    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        creator.spawn(match, owner);
    }
}
