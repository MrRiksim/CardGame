package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.SpellCreator;
import game.server.cards.Unit;
import game.server.cards.spells.creators.BuffPotionCreator;

public class Brewer extends Unit {
    private final SpellCreator creator = new BuffPotionCreator();

    private Brewer() {
        super(
                "Brewer",
                "At the beginning of a turn brews a potion to help your units",
                2,
                1,
                1,
                Element.FIRE,
                null
        );
    }

    private Brewer(String name, String description, int energyCost, int health, int maxHealth, int damage, Element element, String clan) {
        super(name, description, energyCost, health, maxHealth, damage, element, clan);
    }
    // Inside your standard Unit subclass (e.g., Archer.java or a concrete unit base):
    @Override
    public Unit cloneShallow() {
        Brewer clone = new Brewer(
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
        return new Brewer(getName(), getDescription(), getEnergyCost(), getHealth(), getMaxHealth(), getDamage(), getElement(), getClan());
    }

    public static Brewer create() {
        return new Brewer();
    }

    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        creator.spawn(match, owner);
    }
}
