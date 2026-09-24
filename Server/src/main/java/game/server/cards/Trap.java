package game.server.cards;

import game.server.GameEvent;
import game.server.Match;
import game.server.Player;

/**
 * A card placed face-down in one of the 5 back field zones. Traps are always
 * hidden from the opponent (the network protocol should only ever tell an
 * opponent how many back-zone cards exist, never what they are), and move to
 * the graveyard the moment they activate.
 *
 * <p><b>Design pattern note - Strategy / Observer:</b> {@link Match} fires a
 * {@link GameEvent} whenever something trap-relevant happens and asks every
 * face-down trap on the field whether it cares ({@link #isTriggeredBy}) -
 * that's traps acting as observers of the game state. Whichever trap says
 * yes then runs its own {@link #activate} strategy, so Match never needs to
 * know what any individual trap actually does.
 */
public abstract class Trap extends Card {

    private final String activationCondition;

    protected Trap(String name, String description, int energyCost, String activationCondition) {
        super(name, description, energyCost);
        this.activationCondition = activationCondition;
    }

    public String getActivationCondition() {
        return activationCondition;
    }

    public abstract boolean isTriggeredBy(GameEvent event, Player owner);

    public abstract void activate(Match match, Player owner, GameEvent event);
}
