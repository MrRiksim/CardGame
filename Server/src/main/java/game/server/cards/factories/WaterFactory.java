package game.server.cards.factories;

import game.server.cards.AbstractCardFactory;
import game.server.cards.Spell;
import game.server.cards.Unit;
import game.server.cards.spells.WaterSpell;
import game.server.cards.units.WaterUnit;

public class WaterFactory implements AbstractCardFactory {
    @Override
    public Unit createUnit(){
        return new WaterUnit();
    }

    @Override
    public Spell createSpell(){
        return new WaterSpell();
    }
}
