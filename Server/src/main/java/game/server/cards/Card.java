package game.server.cards;

import java.util.UUID;

/**
 * Abstract base for every card in the game (units, spells and traps).
 *
 * <p><b>Design pattern note:</b> this class, together with {@link Unit}, {@link Spell} and
 * {@link Trap}, is plain inheritance/abstraction - an abstract base class with concrete
 * subclasses. That is NOT the Abstract Factory pattern on its own. The actual factory pattern
 * in this package is Factory Method, and it lives in {@link CardFactory} - see the comment
 * there for the full explanation.
 */
public abstract class Card {

    private final String id;
    private final String name;
    private final String description;
    private final int energyCost;

    protected Card(String name, String description, int energyCost) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.energyCost = energyCost;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    /*
     * The card that should actually land in the graveyard when this one is
     * discarded - normally just itself. Player.sendToGraveyard asks every card
     * this, so overriding it is all a card type has to do to change what a
     * graveyard receives (see Unit and UnitBuff).
     */
    public Card prepareForGraveyard() {
        return this;
    }
}
