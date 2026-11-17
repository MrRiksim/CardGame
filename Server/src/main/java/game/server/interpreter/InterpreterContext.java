package game.server.interpreter;

import game.server.MatchFacade;
import game.server.Player;
import game.server.cards.Card;
import game.server.cards.CardFactory;
import java.util.Optional;

/** Interpreter context: binds expressions to the authenticated sender's match and card catalog. */
public record InterpreterContext(MatchFacade match, Player player) {
    public Optional<Card> findCard(String name) {
        return CardFactory.createByName(name);
    }

    public boolean giveCard(Card card) {
        return match.giveCard(player, card);
    }
}
