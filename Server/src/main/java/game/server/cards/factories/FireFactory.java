package game.server.cards.factories;

import game.server.cards.AbstractCardFactory;
import game.server.cards.Spell;
import game.server.cards.Unit;
import game.server.cards.spells.EarthSpell;
import game.server.cards.spells.FireSpell;
import game.server.cards.units.EarthUnit;
import game.server.cards.units.FireUnit;

public class FireFactory implements AbstractCardFactory {
    @Override
    public Unit createUnit(){
        return new FireUnit();
    }

    @Override
    public Spell createSpell(){
        return new FireSpell();
    }
}
