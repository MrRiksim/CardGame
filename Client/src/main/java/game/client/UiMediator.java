package game.client;

/**
 * <b>Design pattern - Mediator (Mediator interface):</b> centralizes
 * communication between UI components ({@link BoardPanel}, {@link CardInfoPanel},
 * {@link StatusBar}) so none of them hold direct references to one another.
 */
public interface UiMediator {

    void notify(UiColleague sender, UiEvent event, ClientCard card);
}
