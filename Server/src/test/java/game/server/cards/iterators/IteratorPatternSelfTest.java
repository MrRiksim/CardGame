package game.server.cards.iterators;

import game.server.Match;
import game.server.MatchSettingsDirector;
import game.server.Player;
import game.server.PlayerConnection;
import game.server.cards.Card;
import game.server.cards.Spell;
import game.server.cards.Unit;
import game.server.cards.spells.ChainLightningSpell;
import game.server.cards.spells.HealSpell;
import game.server.cards.spells.HealingWaveSpell;
import game.server.cards.spells.RaiseTheFallenSpell;
import game.server.cards.spells.ShieldSpell;
import game.server.cards.traps.BearTrap;
import game.server.cards.units.Knight;
import game.server.cards.units.buffs.ShieldBuff;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Standalone verification, following the project's existing self-test convention. */
public final class IteratorPatternSelfTest {
    public static void main(String[] args) {
        circularTraversal();
        priorityTraversal();
        reverseTraversal();
        chainLightning();
        healingWave();
        raiseTheFallen();
        System.out.println("All Iterator pattern and spell checks passed.");
    }

    private static void circularTraversal() {
        Unit first = Knight.create();
        Unit middle = Knight.create();
        Unit start = Knight.create();
        Unit dead = Knight.create();
        dead.takeDamage(99);
        Iterator<Unit> iterator = new CircularTargetIterator(new Unit[]{first, dead, middle, start, null}, 3);
        check(iterator.hasNext() && iterator.hasNext(), "hasNext must not consume a target");
        check(iterator.next() == start && iterator.next() == first && iterator.next() == middle,
                "Circular order must wrap, skip dead/empty zones, and visit once");
        exhausted(iterator);
        exhausted(new CircularTargetIterator(new Unit[5], 0));
    }

    private static void priorityTraversal() {
        Unit weak = injured(1);
        Unit tied = injured(1);
        Unit stronger = injured(2);
        Unit full = Knight.create();
        Unit dead = Knight.create();
        dead.takeDamage(99);
        Unit buffed = new ShieldBuff(injured(1), ShieldSpell.create());
        Iterator<Unit> iterator = new WeakestFirstIterator(new Unit[]{stronger, full, weak, tied, null, dead, buffed});
        check(iterator.next() == weak && iterator.next() == tied && iterator.next() == stronger,
                "Priority order must use health, stable zone ties, and existing healing eligibility");
        exhausted(iterator);
    }

    private static void reverseTraversal() {
        Unit oldest = Knight.create();
        Unit newest = Knight.create();
        List<Card> graveyard = new ArrayList<>(List.of(oldest, HealSpell.create(), newest, BearTrap.create()));
        Iterator<Unit> iterator = new RecentCardsIterator(graveyard);
        check(iterator.next() == newest, "Newest unit must be first, skipping a trap");
        graveyard.remove(newest);
        check(iterator.next() == oldest, "Snapshot traversal must tolerate transfers and skip spells");
        exhausted(iterator);
        exhausted(new RecentCardsIterator(List.of(HealSpell.create())));
    }

    private static void chainLightning() {
        Match match = match();
        Player caster = current(match);
        Player enemy = match.getOpponent(caster);
        Unit first = injured(1);
        Unit second = Knight.create();
        Unit shield = new ShieldBuff(Knight.create(), ShieldSpell.create());
        Unit start = injured(1);
        Unit outside = Knight.create();
        enemy.getFrontZones()[0] = first;
        enemy.getFrontZones()[1] = second;
        enemy.getFrontZones()[2] = outside;
        enemy.getFrontZones()[3] = shield;
        enemy.getFrontZones()[4] = start;
        // 4 -> 0 -> 1: resolves every victim, including the secondary kill.
        cast(match, caster, ChainLightningSpell.create(), start);
        check(enemy.getFrontZones()[4] == null && enemy.getFrontZones()[0] == null,
                "Primary and secondary deaths must be removed from the board");
        check(second.getHealth() == 2 && outside.getHealth() == 3, "Only three targets must be hit");
        check(enemy.getGraveyard().containsAll(List.of(start, first)), "Killed units must reach the graveyard");
        Arrays.fill(enemy.getFrontZones(), null);
        enemy.getFrontZones()[3] = shield;
        cast(match, caster, ChainLightningSpell.create(), shield);
        check(enemy.getFrontZones()[3].getShieldCount() == 0 && shield.getHealth() == 3,
                "A shield must absorb damage and its spent layer must be removed");
        check(match.canUndoSpell(caster) && match.undoLastSpell(caster), "Chain Lightning must be undoable");
        check(enemy.getFrontZones()[3] == shield && shield.getShieldCount() == 1,
                "Undo must restore the original shield decorator and its unused state");
    }

    private static void healingWave() {
        Match match = match();
        Player caster = current(match);
        Unit strongest = injured(2);
        Unit weak = injured(1);
        Unit tied = injured(1);
        Unit fourth = injured(2);
        caster.getFrontZones()[0] = strongest;
        caster.getFrontZones()[1] = weak;
        caster.getFrontZones()[2] = tied;
        caster.getFrontZones()[3] = fourth;
        // Make an earlier undoable action, then check the wave forms an undo barrier.
        cast(match, caster, HealSpell.create(), fourth);
        check(match.canUndoSpell(caster), "Existing Heal should still create undo history");
        fourth.takeDamage(1);
        cast(match, caster, HealingWaveSpell.create(), null);
        check(weak.getHealth() == 2 && tied.getHealth() == 2 && strongest.getHealth() == 3
                        && fourth.getHealth() == 2,
                "Three charges must heal distinct weakest allies, with stable zone ties");
        check(!match.undoLastSpell(caster), "An older undo must not cross Healing Wave");
        Arrays.fill(caster.getFrontZones(), null);
        Spell noTargets = HealingWaveSpell.create();
        caster.getHand().add(noTargets);
        int energy = caster.getEnergy();
        check(!match.playSpell(caster, noTargets.getId(), null) && caster.getEnergy() == energy,
                "A wave with no eligible allies must be rejected without spending energy");
    }

    private static void raiseTheFallen() {
        Match match = match();
        Player caster = current(match);
        Unit oldest = Knight.create();
        Unit middle = Knight.create();
        Unit newest = Knight.create();
        caster.getGraveyard().addAll(List.of(oldest, HealSpell.create(), middle, BearTrap.create(), newest));
        cast(match, caster, RaiseTheFallenSpell.create(), null);
        check(caster.getHand().equals(List.of(newest, middle)), "Recovery must return two units newest first");
        check(caster.getGraveyard().contains(oldest) && !caster.getGraveyard().contains(newest)
                && !caster.getGraveyard().contains(middle), "Recovery must move cards without duplication");
        check(!match.canUndoSpell(caster), "Recovery must not create an invalid undo snapshot");

        caster.getHand().clear();
        Spell recovery = RaiseTheFallenSpell.create();
        caster.getHand().add(recovery);
        while (!caster.isHandFull()) {
            caster.addToHand(HealSpell.create());
        }
        caster.restoreEnergy(20);
        check(match.playSpell(caster, recovery.getId(), null), "Casting from a full hand frees one slot");
        check(caster.isHandFull() && caster.getHand().contains(oldest), "Recovery must honor hand capacity");
        check(!RaiseTheFallenSpell.create().canTarget(match, caster, null), "No units left means no recovery");
    }

    private static Unit injured(int health) {
        Unit unit = Knight.create();
        unit.takeDamage(unit.getMaxHealth() - health);
        return unit;
    }

    private static Match match() {
        Match match = new Match(1, new DummyConnection(), new DummyConnection(),
                new MatchSettingsDirector().buildStandardMatch());
        match.getPlayer1().getHand().clear();
        match.getPlayer2().getHand().clear();
        return match;
    }

    private static Player current(Match match) {
        return match.getCurrentTurnPlayerNumber() == 1 ? match.getPlayer1() : match.getPlayer2();
    }

    private static void cast(Match match, Player caster, Spell spell, Unit target) {
        caster.restoreEnergy(20);
        caster.getHand().add(spell);
        check(match.playSpell(caster, spell.getId(), target), spell.getName() + " must be playable");
        check(caster.getEnergy() == 20 - spell.getEnergyCost(), "Casting must spend the advertised cost");
    }

    private static void exhausted(Iterator<?> iterator) {
        check(!iterator.hasNext() && !iterator.hasNext(), "Exhausted iterator must stay exhausted");
        try {
            iterator.next();
            throw new AssertionError("Exhausted next() must throw");
        } catch (NoSuchElementException expected) {
            // Standard java.util.Iterator contract.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class DummyConnection extends PlayerConnection {
        DummyConnection() { super(null); }
        @Override public void send(String message) {}
        @Override public boolean isConnected() { return true; }
    }
}
