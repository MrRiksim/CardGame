package game.server.interpreter;

import game.server.cards.Card;
import java.util.Optional;

/** Terminal expression: interprets a card-name literal using the context's catalog. */
public record CardNameExpression(String name) implements Expression<Optional<Card>> {
    @Override
    public Optional<Card> interpret(InterpreterContext context) {
        return context.findCard(name);
    }
}
