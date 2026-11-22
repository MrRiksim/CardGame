package game.server.cards.state;

import game.server.cards.Unit;

/**
 * Design pattern – State (Concrete State: InHand).
 *
 * <p>Initial state for unit cards held in a player's hand.
 * No field effects, combat damage, healing, or field spells can affect the unit.
 * It can only be played to the board (or attached as a rider).
 *
 * <p>Transitions:
 * <ul>
 *   <li>Played onto a front zone &rarr; {@link NormalState}</li>
 * </ul>
 */
public class InHandState implements UnitState {

    @Override
    public UnitStateKind getKind() {
        return UnitStateKind.IN_HAND;
    }

    @Override
    public boolean canPlay() {
        return true;
    }

    @Override
    public boolean canHeal(Unit unit) {
        return false;
    }

    @Override
    public boolean canCripple(Unit unit) {
        return false;
    }

    @Override
    public void takeDamage(Unit unit, int amount) {
        // Cannot take field damage while in hand
    }

    @Override
    public void heal(Unit unit, int amount) {
        // Cannot be healed while in hand
    }

    @Override
    public String toString() {
        return "IN_HAND";
    }
}
