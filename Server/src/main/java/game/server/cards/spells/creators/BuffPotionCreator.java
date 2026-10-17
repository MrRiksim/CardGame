package game.server.cards.spells.creators;

import game.server.cards.Spell;
import game.server.cards.SpellCreator;
import game.server.cards.spells.DamagePotionSpell;
import game.server.cards.spells.HealthBoostSpell;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ConcreteCreator - creates BearTrap trap cards
 */
public class BuffPotionCreator extends SpellCreator {

    @Override
    public Spell createCard() {
        int random = ThreadLocalRandom.current().nextInt(0, 2);
        if(random == 0) {
            return HealthBoostSpell.create();
        } else {
            return DamagePotionSpell.create();
        }
    }
}
