package game.server.cards.state;

import game.server.Match;
import game.server.MatchSettings;
import game.server.MatchSettingsBuilder;
import game.server.Player;
import game.server.PlayerConnection;
import game.server.StandardMatchSettingsBuilder;
import game.server.cards.spells.CrippleSpell;
import game.server.cards.spells.EssenceReplicationSpell;
import game.server.cards.spells.HealSpell;
import game.server.cards.spells.HealthBoostSpell;
import game.server.cards.spells.PhantomMirrorSpell;
import game.server.cards.spells.ShiftingSpellCard;
import game.server.cards.units.Knight;
import game.server.cards.Unit;
import game.server.cards.units.buffs.HealthBoostBuff;

public class StatePatternSelfTest {

    public static void main(String[] args) {
        System.out.println("Starting State Pattern Verification...");

        // 1. Initial creation -> IN_HAND
        Unit knight = Knight.create(); // 3 HP, max 3 HP
        assertCondition(knight.getState().getKind() == UnitStateKind.IN_HAND, "New unit must be in IN_HAND state");
        assertCondition(knight.getState().canPlay(), "IN_HAND unit can be played");
        assertCondition(!knight.canHeal(), "IN_HAND unit cannot be healed");
        assertCondition(!knight.canCripple(), "IN_HAND unit cannot be crippled");

        // Transition to field -> NORMAL
        knight.setState(new NormalState());
        assertCondition(knight.getState().getKind() == UnitStateKind.NORMAL, "Unit on field must be in NORMAL state");
        assertCondition(!knight.getState().canPlay(), "NORMAL unit cannot be played from field");
        assertCondition(!knight.canHeal(), "NORMAL unit cannot be healed (already full HP)");
        assertCondition(!knight.canCripple(), "NORMAL unit cannot be crippled");

        // 2. Non-lethal damage -> AFFECTED (damaged)
        knight.takeDamage(1); // 2 HP / 3 max
        assertCondition(knight.getState().getKind() == UnitStateKind.AFFECTED, "Damaged unit must be in AFFECTED state");
        assertCondition(knight.canHeal(), "AFFECTED unit with damage must be healable");
        assertCondition(!knight.canCripple(), "AFFECTED unit without buffs cannot be crippled");

        // 3. Heal -> Back to NORMAL
        knight.heal(2); // 3 HP / 3 max
        assertCondition(knight.getHealth() == knight.getMaxHealth(), "Health must be capped at max");
        assertCondition(knight.getState().getKind() == UnitStateKind.NORMAL, "Fully healed unit must return to NORMAL state");
        assertCondition(!knight.canHeal(), "Fully healed unit cannot be healed anymore");

        // 4. Buffed -> AFFECTED (buffed)
        HealthBoostSpell boost = HealthBoostSpell.create();
        Unit buffedKnight = new HealthBoostBuff(knight, boost);
        assertCondition(buffedKnight.getState().getKind() == UnitStateKind.AFFECTED, "Unit with buff must be in AFFECTED state");
        assertCondition(buffedKnight.canCripple(), "AFFECTED unit with buff must be crippleable");
        assertCondition(!buffedKnight.canHeal(), "AFFECTED unit with full HP cannot be targeted by Heal");

        // 5. Fatal damage -> DEAD
        knight.takeDamage(10);
        assertCondition(knight.isDead(), "Unit with 0 health must report isDead");
        assertCondition(knight.getState().getKind() == UnitStateKind.DEAD, "Depleted unit must be in DEAD state");
        assertCondition(!knight.canHeal(), "DEAD unit cannot be healed");
        assertCondition(!knight.canCripple(), "DEAD unit cannot be crippled");

        // 6. Reset (graveyard/revival to hand) -> IN_HAND
        knight.reset();
        assertCondition(knight.getHealth() == knight.getMaxHealth(), "Reset health must equal maxHealth");
        assertCondition(knight.getState().getKind() == UnitStateKind.IN_HAND, "Revived unit returned to hand must be in IN_HAND state");
        assertCondition(knight.getState().canPlay(), "Revived unit in hand can be played again");

        // 7. Verify isUndoable is true for ALL spells
        assertCondition(new ShiftingSpellCard().isUndoable(), "ShiftingSpellCard must be undoable");
        assertCondition(new PhantomMirrorSpell().isUndoable(), "PhantomMirrorSpell (Mirage) must be undoable");
        assertCondition(new EssenceReplicationSpell().isUndoable(), "EssenceReplicationSpell must be undoable");
        assertCondition(HealSpell.create().isUndoable(), "HealSpell must be undoable");
        assertCondition(CrippleSpell.create().isUndoable(), "CrippleSpell must be undoable");

        // 8. Match Simulation: Test Undo for Shifting Spell, Phantom Mirror, and Essence Replication
        testMatchSpellUndos();

        System.out.println("ALL TESTS (STATE, SPELLS, UNDO) PASSED SUCCESSFULLY!");
    }

    private static void testMatchSpellUndos() {
        System.out.println("Testing Match undo for special spells (Shifting, Mirage, Essence)...");

        MatchSettings settings = new game.server.MatchSettingsDirector().buildStandardMatch();
        PlayerConnection c1 = new DummyConnection();
        PlayerConnection c2 = new DummyConnection();
        Match match = new Match(1, c1, c2, settings);

        Player current = match.getCurrentTurnPlayerNumber() == 1 ? match.getPlayer1() : match.getPlayer2();
        Player opponent = match.getOpponent(current);

        // Place a knight on current player's field (slot 0) and opponent's field (slot 0)
        Unit myUnit = Knight.create();
        Unit oppUnit = Knight.create();
        current.getFrontZones()[0] = myUnit;
        opponent.getFrontZones()[0] = oppUnit;

        // --- TEST 1: Undo Shifting Spell (Buff Ally) ---
        ShiftingSpellCard shiftingSpell = new ShiftingSpellCard();
        current.getHand().add(shiftingSpell);
        int energyBefore = current.getEnergy();
        int handSizeBefore = current.getHand().size();

        boolean played = match.playSpell(current, shiftingSpell.getId(), myUnit);
        assertCondition(played, "Shifting spell must be played successfully");
        assertCondition(current.getFrontZones()[0].isDamageBuffed(), "My unit should now be buffed by shifting spell");

        boolean undone = match.undoLastSpell(current);
        assertCondition(undone, "Shifting spell must be successfully undone!");
        assertCondition(!current.getFrontZones()[0].isDamageBuffed(), "My unit should NOT be buffed after undo");
        assertCondition(current.getEnergy() == energyBefore, "Energy must be fully refunded after shifting spell undo");
        assertCondition(current.getHand().size() == handSizeBefore, "Hand size must be restored after shifting spell undo");

        // --- TEST 2: Undo Phantom Mirror (Mirage) ---
        // Mirrored index for opp unit at slot 0 is 5 - 1 - 0 = slot 4
        PhantomMirrorSpell mirrorSpell = new PhantomMirrorSpell();
        current.getHand().add(mirrorSpell);
        energyBefore = current.getEnergy();
        handSizeBefore = current.getHand().size();

        assertCondition(current.getFrontZones()[4] == null, "Caster zone 4 must be empty before mirror");
        played = match.playSpell(current, mirrorSpell.getId(), oppUnit);
        assertCondition(played, "Phantom Mirror must be played successfully");
        assertCondition(current.getFrontZones()[4] != null, "Illusion should be placed at mirrored zone 4");

        undone = match.undoLastSpell(current);
        assertCondition(undone, "Phantom Mirror must be successfully undone!");
        assertCondition(current.getFrontZones()[4] == null, "Mirrored zone 4 must be null (illusion removed) after undo");
        assertCondition(current.getEnergy() == energyBefore, "Energy must be refunded after mirror undo");
        assertCondition(current.getHand().size() == handSizeBefore, "Hand restored after mirror undo");

        // --- TEST 3: Undo Essence Replication ---
        EssenceReplicationSpell essenceSpell = new EssenceReplicationSpell();
        current.getHand().add(essenceSpell);
        energyBefore = current.getEnergy();
        handSizeBefore = current.getHand().size();

        played = match.playSpell(current, essenceSpell.getId(), oppUnit);
        assertCondition(played, "Essence Replication must be played successfully");

        undone = match.undoLastSpell(current);
        assertCondition(undone, "Essence Replication must be successfully undone!");
        assertCondition(current.getEnergy() == energyBefore, "Energy must be refunded after essence replication undo");
        assertCondition(current.getHand().size() == handSizeBefore, "Hand restored after essence replication undo");

        System.out.println("All special spell undos verified in Match simulation!");
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("TEST FAILED: " + message);
        }
        System.out.println("PASS: " + message);
    }

    private static class DummyConnection extends PlayerConnection {
        public DummyConnection() {
            super(null);
        }
        @Override
        public void send(String message) {}
        @Override
        public boolean isConnected() { return true; }
    }
}
