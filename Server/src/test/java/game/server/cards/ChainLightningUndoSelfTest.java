package game.server.cards;

import game.server.Match;
import game.server.MatchSettingsDirector;
import game.server.Player;
import game.server.PlayerConnection;
import game.server.cards.spells.ChainLightningSpell;
import game.server.cards.spells.DamagePotionSpell;
import game.server.cards.spells.HealthBoostSpell;
import game.server.cards.spells.ShieldSpell;
import game.server.cards.units.Jumpy;
import game.server.cards.units.Knight;
import game.server.cards.units.buffs.DamagePotionBuff;
import game.server.cards.units.buffs.HealthBoostBuff;
import game.server.cards.units.buffs.ShieldBuff;
import java.util.Arrays;

public final class ChainLightningUndoSelfTest {
    public static void main(String[] args) {
        stackedBuffsAndReplay();
        ridersAndLinkedCopies(false);
        ridersAndLinkedCopies(true);
        adaptedHealth();
        undoStackAndTurnBoundary();
        System.out.println("All Chain Lightning undo checks passed.");
    }

    private static void stackedBuffsAndReplay() {
        Match match = match();
        Player caster = caster(match);
        Player enemy = match.getOpponent(caster);
        Unit dead = injured();
        Unit shield = new ShieldBuff(Knight.create(), ShieldSpell.create());
        Unit base = injured();
        Unit health = new HealthBoostBuff(base, HealthBoostSpell.create());
        Unit damage = new DamagePotionBuff(health, DamagePotionSpell.create());
        enemy.getFrontZones()[0] = dead;
        enemy.getFrontZones()[1] = shield;
        enemy.getFrontZones()[2] = damage;
        ChainLightningSpell spell = prepare(caster);
        Snapshot before = new Snapshot(match);
        check(match.playSpell(caster, spell.getId(), dead), "Cast must succeed");
        check(enemy.getFrontZones()[0] == null && enemy.getFrontZones()[1].getShieldCount() == 0,
                "Cast must kill and consume a shield");
        check(((UnitBuff) damage).getWrapped() == base && damage.getHealth() == 1,
                "Spent inner health layer must be spliced out");
        String afterCast = match.buildStateFor(caster);
        before.undoAndCheck();
        check(((UnitBuff) damage).getWrapped() == health && health.getHealth() == 2,
                "Undo must restore inner wrapper links and health bonus");
        check(shield.getShieldCount() == 1 && dead.getHealth() == 1, "Undo must restore shield and pre-death health");
        check(match.playSpell(caster, spell.getId(), dead), "Restored card must be playable again");
        check(afterCast.equals(match.buildStateFor(caster)), "Replaying must produce exactly the same result");
        before.undoAndCheck();
    }

    private static void ridersAndLinkedCopies(boolean fullHand) {
        Match match = match();
        Player caster = caster(match);
        Player enemy = match.getOpponent(caster);
        Unit rider = Jumpy.create();
        Unit secondRider = Jumpy.create();
        Unit group = injured().attach(rider).attach(secondRider);
        Unit enemyCopy = group.cloneShallow();
        Unit ownCopy = group.cloneShallow();
        enemy.getFrontZones()[4] = group;
        enemy.getFrontZones()[0] = enemyCopy;
        enemy.getFrontZones()[1] = Knight.create();
        enemy.getFrontZones()[2] = Knight.create();
        caster.getFrontZones()[0] = ownCopy;
        if (fullHand) {
            while (!enemy.isHandFull()) enemy.addToHand(Knight.create());
        }
        ChainLightningSpell spell = prepare(caster);
        Snapshot before = new Snapshot(match);
        check(match.playSpell(caster, spell.getId(), group), "Group cast must succeed");
        check(enemy.getFrontZones()[4] == null && enemy.getFrontZones()[0] == null
                && caster.getFrontZones()[0] == null, "Death must remove group and linked copies on both sides");
        check((fullHand ? enemy.getGraveyard() : enemy.getHand()).contains(rider),
                "Rider must return to the hand or be discarded if full");
        check(enemy.getFrontZones()[1].getHealth() == 2 && enemy.getFrontZones()[2].getHealth() == 2,
                "Iterator must skip the removed copy and continue circular targeting");
        before.undoAndCheck();
        check(group.getRiderCount() == 2 && group.getHealth() == 1, "Undo must restore carried health and riders");
        check(enemyCopy.getShallowOriginal() == group && ownCopy.getShallowOriginal() == group,
                "Undo must preserve original copy links");
    }

    private static void adaptedHealth() {
        Match match = match();
        Player caster = caster(match);
        Player enemy = match.getOpponent(caster);
        // No armor: the chain's one damage must reach the adapted health counter.
        Unit adapted = new SkeletonCardAdapter(new SkeletonCard(5, 1, 0, 2, 1, 2));
        enemy.getFrontZones()[0] = adapted;
        ChainLightningSpell spell = prepare(caster);
        Snapshot before = new Snapshot(match);
        check(match.playSpell(caster, spell.getId(), adapted) && enemy.getFrontZones()[0] == null,
                "Adapted unit must die");
        before.undoAndCheck();
        check(adapted.getHealth() == 1 && !adapted.isDead(), "Undo must restore adapted HP, not Unit's unused counter");
    }

    private static void undoStackAndTurnBoundary() {
        Match match = match();
        Player caster = caster(match);
        Player enemy = match.getOpponent(caster);
        Unit unit = Knight.create();
        enemy.getFrontZones()[0] = unit;
        ChainLightningSpell first = prepare(caster);
        ChainLightningSpell second = prepare(caster);
        Snapshot initial = new Snapshot(match);
        check(match.playSpell(caster, first.getId(), unit), "First cast");
        Snapshot afterFirst = new Snapshot(match);
        check(match.playSpell(caster, second.getId(), unit), "Second cast");
        afterFirst.undoAndCheck();
        initial.undoAndCheck();
        check(!match.undoLastSpell(caster), "Two undos must exhaust two casts");
        check(match.playSpell(caster, first.getId(), unit), "Cast before ending turn");
        check(match.endTurn(caster), "Turn must end");
        check(!match.undoLastSpell(caster) && !match.canUndoSpell(match.getOpponent(caster)),
                "Undo must remain scoped to the casting turn");
    }

    private static ChainLightningSpell prepare(Player caster) {
        caster.restoreEnergy(20);
        ChainLightningSpell spell = ChainLightningSpell.create();
        caster.getHand().add(spell);
        return spell;
    }

    private static Unit injured() {
        Unit unit = Knight.create();
        unit.takeDamage(2);
        return unit;
    }

    private static Match match() {
        Match match = new Match(1, new DummyConnection(), new DummyConnection(),
                new MatchSettingsDirector().buildStandardMatch());
        match.getPlayer1().getHand().clear();
        match.getPlayer2().getHand().clear();
        return match;
    }

    private static Player caster(Match match) {
        return match.getCurrentTurnPlayerNumber() == 1 ? match.getPlayer1() : match.getPlayer2();
    }

    private static final class Snapshot {
        private final Match match;
        private final String first;
        private final String second;
        private final Unit[] firstUnits;
        private final Unit[] secondUnits;

        Snapshot(Match match) {
            this.match = match;
            first = match.buildStateFor(match.getPlayer1());
            second = match.buildStateFor(match.getPlayer2());
            firstUnits = match.getPlayer1().getFrontZones().clone();
            secondUnits = match.getPlayer2().getFrontZones().clone();
        }

        void undoAndCheck() {
            check(match.undoLastSpell(caster(match)), "Undo must succeed");
            check(first.equals(match.buildStateFor(match.getPlayer1()))
                    && second.equals(match.buildStateFor(match.getPlayer2())),
                    "Both players must see exactly the pre-cast cards, health, buffs, energy and graveyards");
            check(Arrays.equals(firstUnits, match.getPlayer1().getFrontZones())
                    && Arrays.equals(secondUnits, match.getPlayer2().getFrontZones()),
                    "Undo must restore the original card objects, not new clones");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class DummyConnection extends PlayerConnection {
        DummyConnection() { super(null); }
        @Override public void send(String message) {}
        @Override public boolean isConnected() { return true; }
    }
}
