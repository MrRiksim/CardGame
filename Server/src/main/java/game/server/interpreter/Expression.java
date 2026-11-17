package game.server.interpreter;

/** Interpreter: common expression contract for terminal and nonterminal nodes. */
public interface Expression<T> {
    T interpret(InterpreterContext context);
}
