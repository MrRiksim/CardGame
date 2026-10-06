package game.server.cards;

import game.server.Match;
import game.server.Player;

public class DrawFromDeckStrategy implements SpellStrategy {

    private final int amountToDraw;

    public DrawFromDeckStrategy(int amountToDraw) {
        this.amountToDraw = amountToDraw;
    }

    @Override
    public void execute(Player caster, SpellTargetContext context, Match match) {
        for (int i = 0; i < amountToDraw; i++) {
            caster.drawCard();
        }
    }
}