package game.server.interpreter;

import game.server.cards.CardFactory;

/** Terminal expression for the '/cards' catalog query. */
public final class CardListExpression implements Expression<InterpretationResult> {
    @Override
    public InterpretationResult interpret(InterpreterContext context) {
        return new InterpretationResult("Available cards: " + String.join(", ", CardFactory.getCardNames()), false);
    }
}
