package game.server.cards.factories;

import game.server.cards.AbstractCardFactory;
import game.server.cards.Spell;
import game.server.cards.Unit;
import game.server.cards.spells.EarthSpell;
import game.server.cards.units.EarthUnit;

public class EarthFactory implements AbstractCardFactory {
    @Override
    public Unit createUnit(){
        return new EarthUnit();
    }

    @Override
    public Spell createSpell(){
        return new EarthSpell();
    }
}
