package game.server.cards;
import game.server.Match;
import game.server.Player;

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

        this(name, description, energyCost, health, health, damage, element, clan);
    }

    /*
     * Lets a current health different from max health be set up front -
     * used by UnitBuff, which snapshots a unit that may already be
     * damaged when the buff is applied.
     */
    protected Unit(
            String name,
            String description,
            int energyCost,
            int health,
            int maxHealth,
            int damage,
            Element element,
            String clan) {

        super(name, description, energyCost);

        this.health = health;
        this.maxHealth = maxHealth;
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

    /*
     * For a plain unit, itself. UnitBuff overrides this to walk back
     * through every stacked layer to the original card underneath - see
     * the note there and on Match.serializeFront for why that matters:
     * a buff's OWN name/cost (e.g. "Armor", cost 1) is correct for its
     * eventual graveyard entry, but wrong for what the unit should be
     * displayed and identified as while the buff is still active.
     */
    public Unit getBaseUnit() {
        return this;
    }

    /*
     * Hook for a unit with passive, start-of-turn behavior (see Tombstone).
     * Called once for every unit belonging to whoever's turn just started;
     * most units have nothing to do here, hence the no-op default.
     */
    public void onOwnerTurnStart(Match match, Player owner) {}
}
