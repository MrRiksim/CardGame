package game.client;

/**
 * Common shape for anything the server told us about a card. The client and
 * server are separate projects, so this is a lightweight mirror of the
 * server's Card hierarchy for rendering and messaging purposes only - it
 * carries no game logic (that all lives server-side), just the data needed
 * to draw a card and to send its id back in a play message.
 */
public interface ClientCard {

    String id();

    String name();

    int energyCost();

    String description();

    CardKind kind();
}
