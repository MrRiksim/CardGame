package game.server.interpreter;

import game.server.cards.Card;
import java.util.Optional;

/** Nonterminal expression: give := '/give' cardName. Evaluates its child before granting the card. */
public record GiveExpression(Expression<Optional<Card>> cardName) implements Expression<InterpretationResult> {
    @Override
    public InterpretationResult interpret(InterpreterContext context) {
        Optional<Card> resolved = cardName.interpret(context);
        if (resolved.isEmpty()) {
            return new InterpretationResult("Unknown card. Use /cards to see available card names.", false);
        }
        Card card = resolved.get();
        if (!context.giveCard(card)) {
            String reason = context.player().isHandFull()
                    ? "Your hand is full (" + context.player().getHand().size() + "/"
                        + context.player().getMaxHandSize() + "). Play a card first."
                    : "Cards can only be given to a player in an active match.";
            return new InterpretationResult(reason, false);
        }
        return new InterpretationResult("Added " + card.getName() + " to your hand.", true);
    }
}
