package game.server.interpreter;

import java.util.Locale;

/** Builds the Interpreter expression tree; parsing does not execute gameplay actions.
 * Grammar: command := '/give' cardName | '/cards' | '/help'.
 * cardName := unquoted words | '"' words '"'.
 */
public final class CommandParser {
    private static final String HELP = "Commands: /give <card name>, /cards, /help. Example: /give Chain Lightning. "
            + "Names ignore case; quotes are optional. Your hand must have room.";

    public Expression<InterpretationResult> parse(String input) {
        String[] parts = input.strip().split("\\s+", 2);
        String keyword = parts[0].toLowerCase(Locale.ROOT);
        String argument = parts.length == 2 ? parts[1].strip() : "";
        return switch (keyword) {
            case "/give" -> parseGive(argument);
            case "/cards" -> argument.isEmpty() ? new CardListExpression() : new FeedbackExpression("Usage: /cards");
            case "/help" -> new FeedbackExpression(HELP);
            default -> new FeedbackExpression("Unknown command. " + HELP);
        };
    }

    private Expression<InterpretationResult> parseGive(String name) {
        if (name.isEmpty()) {
            return new FeedbackExpression("Usage: /give <card name>. Use /cards for names.");
        }
        if (name.startsWith("\"") || name.endsWith("\"")) {
            if (name.length() < 2 || !name.startsWith("\"") || !name.endsWith("\"")) {
                return new FeedbackExpression("Close both quotes around the card name. Example: /give \"Chain Lightning\"");
            }
            name = name.substring(1, name.length() - 1).strip();
        }
        return new GiveExpression(new CardNameExpression(name));
    }
}
