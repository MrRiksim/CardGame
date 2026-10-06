package game.server.cards;

import game.server.Match;
import game.server.Player;

public class ResurrectToHandStrategy implements SpellStrategy {

    @Override
    public void execute(Player caster, SpellTargetContext context, Match match) {
        var graveyard = caster.getGraveyard();

        // If the current spell is already in the graveyard, the "previous" card is at size() - 2.
        // If there are at least 2 cards in the graveyard, pull the second-to-last card.
        if (graveyard.size() >= 2) {
            Card targetCard = graveyard.remove(graveyard.size() - 2);
            caster.getHand().add(targetCard);
        }
    }
}