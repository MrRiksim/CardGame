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
     * Deliberately NO image path / file name / asset folder here. The
     * server only needs to referee the game - it never draws anything - so
     * it has no reason to know how a card looks. It sends the client a
     * card's kind + name (see Match.buildStateFor), and the client's own
     * CardArt class maps that to an asset path on its side. That keeps the
     * two projects properly decoupled: a re-skin or new piece of artwork
     * never touches server code, and the server has no filesystem/asset
     * concerns baked into its model at all.
     */
}
