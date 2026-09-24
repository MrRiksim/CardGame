package game.server.cards;

/**
 * A card that gets placed in one of the 5 front field zones. Units deal
 * damage to whatever is opposite them (or to the opposing player directly)
 * at the end of every turn but the very first.
 */
public abstract class Unit extends Card {

    private final int maxHealth;
    private int health;
    private final int damage;
    private final Element element;

    /*
     * Nullable - not every unit belongs to a clan.
     */
    private final String clan;

    protected Unit(
            String name,
            String description,
            int energyCost,
            int health,
            int damage,
            Element element,
            String clan) {

        super(name, description, energyCost);

        this.maxHealth = health;
        this.health = health;
        this.damage = damage;
        this.element = element;
        this.clan = clan;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getDamage() {
        return damage;
    }

    public Element getElement() {
        return element;
    }

    public String getClan() {
        return clan;
    }

    public boolean hasClan() {
        return clan != null;
    }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public boolean isDead() {
        return health <= 0;
    }
}
