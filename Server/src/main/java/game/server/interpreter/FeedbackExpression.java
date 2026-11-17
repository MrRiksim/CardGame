package game.server.interpreter;

/** Terminal expression for help and syntax errors; it never changes the match. */
public record FeedbackExpression(String text) implements Expression<InterpretationResult> {
    @Override
    public InterpretationResult interpret(InterpreterContext context) {
        return new InterpretationResult(text, false);
    }
}
