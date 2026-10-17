package game.server.cards.units;

import game.server.Match;
import game.server.Player;
import game.server.cards.Element;
import game.server.cards.SpellCreator;
import game.server.cards.Unit;
import game.server.cards.spells.creators.BuffPotionCreator;

public class Brewer extends Unit {
    private final SpellCreator creator = new BuffPotionCreator();

    private Brewer() {
        super(
                "Brewer",
                "At the beginning of a turn brews a potion to help your units",
                2,
                1,
                1,
                Element.FIRE,
                null
        );
    }

    public static Brewer create() {
        return new Brewer();
    }

    @Override
    public void onOwnerTurnStart(Match match, Player owner) {
        creator.spawn(match, owner);
    }
}
