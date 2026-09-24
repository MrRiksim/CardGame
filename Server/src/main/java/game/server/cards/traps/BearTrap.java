package game.server.cards.traps;

import game.server.GameEvent;
import game.server.Match;
import game.server.Player;
import game.server.cards.Trap;

public class BearTrap extends Trap {

    private BearTrap() {
        super(
                "Bear Trap",
                "A hidden trap that snaps shut on the first unit the enemy plays",
                2,
                "Enemy plays a unit card"
        );
    }

    /*
     * Factory Method - see the note on CardFactory.
     */
    public static BearTrap create() {
        return new BearTrap();
    }

    @Override
    public boolean isTriggeredBy(GameEvent event, Player owner) {
        return event.getType() == GameEvent.Type.UNIT_PLAYED
                && event.getTriggeringPlayer() != owner;
    }

    @Override
    public void activate(Match match, Player owner, GameEvent event) {
        match.destroyUnit(event.getUnit());
    }
}
