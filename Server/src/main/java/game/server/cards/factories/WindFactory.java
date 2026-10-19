package game.server.cards.factories;

import game.server.cards.AbstractCardFactory;
import game.server.cards.Spell;
import game.server.cards.Unit;
import game.server.cards.spells.WindSpell;
import game.server.cards.units.WindUnit;

public class WindFactory implements AbstractCardFactory {
    @Override
    public Unit createUnit(){
        return new WindUnit();
    }

    @Override
    public Spell createSpell(){
        return new WindSpell();
    }
}
