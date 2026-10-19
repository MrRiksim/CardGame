package game.server.cards;

public class SkeletonCard {

    private final int maxHP;
    private final int armor;
    private int HP;
    private final int DMG;
    private final int hitCount;
    private final int manaCost;
    //private final Element element;

    public String title() { return "Skeleton Warrior"; }
    public int getHP() {
        return HP;
    }
    public int getArmor(){ return armor;}
    public int getMaxHealth() {
        return maxHP;
    }
    public int getDamage() {
        return DMG;
    }
    public int getHitCount(){return hitCount;}
    public int manaCost() { return manaCost; }
    public void takeHit(int amount) {
        HP = Math.max(0, HP - Math.max(0,amount-getArmor()));
    }
    public void healHP(int amount) {
        HP = Math.min(maxHP, HP + amount);
    }
    public boolean isDeadCheck() {return HP <= 0;}

    public SkeletonCard(int maxHP, int HP,int armor, int dmg, int hitCount, int manaCost) {
        this.maxHP = maxHP;
        this.HP = HP;
        this.armor = armor;
        this.DMG = dmg;
        this.hitCount = hitCount;
        this.manaCost = manaCost;
    }
}
class SkeletonCardAdapter extends Unit {
    private final SkeletonCard card;

    public SkeletonCardAdapter(SkeletonCard card) {
        super(
                card.title(),
                String.format(
                        "Skeleton card: hits %d times and takes %d less damage per hit",
                        card.getHitCount(), card.getArmor()
                ),
                card.manaCost(),
                card.getHP(),
                card.getDamage(),
                null,
                null
        );
        this.card = card;
    }

    @Override
    public int getHealth() {
        return card.getHP();
    }
    @Override
    public int getMaxHealth() {
        return card.getMaxHealth();
    }
    @Override
    public int getDamage() {
        return card.getDamage()*card.getHitCount();
    }
    @Override
    public Element getElement() {
        return Element.EARTH;
    }
    @Override
    public String getClan() {
        return null;
    }
    @Override
    public boolean hasClan() {
        return false;
    }
    @Override
    public void takeDamage(int amount) {
        card.takeHit(amount);
    }
    @Override
    public void heal(int amount) {
        card.healHP(amount);
    }
    @Override
    public boolean isDead() {
        return card.isDeadCheck();}

}
