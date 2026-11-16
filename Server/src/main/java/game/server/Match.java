package game.server;

import game.server.cards.Card;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Trap;
import game.server.cards.Unit;
import game.server.cards.UnitBuff;
import game.server.cards.UnitContainer;
import game.server.memento.SpellMemento;
import game.server.memento.SpellMementoHistory;
import game.server.spectating.MatchSnapshot;
import game.server.spectating.SideSnapshot;

import java.util.concurrent.ThreadLocalRandom;

import game.server.cards.units.Archer;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import game.server.validation.PlayRequest;
import game.server.validation.PlayValidationChains;
import game.server.validation.PlayValidationHandler;
import game.server.validation.ValidationResult;

/**
 * Runs a single game between two players: playing units/spells/traps,
 * ending turns, resolving the end-of-turn attack phase and checking trap
 * activations.
 *
 * <p>The numeric rules (starting health, energy, hand size, and whether
 * there's a turn clock at all) come in per-match via {@link MatchSettings}
 * - see {@link MatchSettingsBuilder} - rather than being fixed here. What IS fixed
 * for every mode: +1 card and this match's energy-per-turn at the start of
 * each turn, and units attacking whatever is opposite them (or the player,
 * if the opposite zone is empty) at the end of every turn but the very
 * first.
 */
public class Match {

    public static final int NUMBER_OF_ZONES = Player.NUMBER_OF_ZONES;

    private final int id;
    private final MatchSettings settings;
    private final Player player1;
    private final Player player2;

    private Player currentTurn;
    private int turnNumber = 1;
    private boolean active = true;

    /*
     * Null whenever settings.hasTimer() is false. Otherwise, when
     * currentTurn's clock last started ticking - see the turn-clock
     * methods below.
     */
    private Long turnStartedAtMillis;

    private final Map<String, Integer> archerTargets = new HashMap<>();
    private final List<MatchObserver> observers = new ArrayList<>();

    /*
     * Goes up by one after every successful action that can change what
     * the match looks like (see stateChanged). Lets CachingStateProxy tell
     * whether its cached snapshot is still current without capturing a new
     * one to compare. The clocks aren't counted - they move on their own.
     */
    private long stateVersion = 0;

    /*
     * Design pattern note – Memento (Caretaker reference): the history object
     * is the Caretaker; Match is the Originator.  One stack per match –
     * cleared at the end of every turn so undo is scoped to the current turn
     * only.
     */
    private final SpellMementoHistory spellUndoHistory = new SpellMementoHistory();

    public Match(int id, PlayerConnection connection1, PlayerConnection connection2, MatchSettings settings) {
        this.id = id;
        this.settings = settings;
        this.player1 = new Player(connection1, settings);
        this.player2 = new Player(connection2, settings);
        addObserver(connection1);
        addObserver(connection2);

        GameStatistics.getInstance().recordMatchStarted();
        /*
         * Randomly select who goes first.
         */
        currentTurn = ThreadLocalRandom.current().nextBoolean() ? player1 : player2;
        startTurnClock();
    }

    public synchronized void addObserver(MatchObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public synchronized void removeObserver(MatchObserver observer) {
        observers.remove(observer);
    }

    public synchronized void notifyObservers() {
        for (MatchObserver observer : new ArrayList<>(observers)) {
            if (observer instanceof PlayerConnection connection) {
                Player viewer = getPlayerFor(connection);
                if (viewer != null) {
                    observer.onMatchUpdate(new StatePacket(buildStateFor(viewer), new PlainTextSerializer()));
                }
            } else {
                observer.onMatchUpdate(new StatePacket(buildStateFor(player1), new PlainTextSerializer()));
            }
        }
    }

    public synchronized void notifyObservers(GamePacket packet) {
        for (MatchObserver observer : new ArrayList<>(observers)) {
            observer.onMatchUpdate(packet);
        }
    }

    public synchronized boolean hasTimer() {
        return settings.hasTimer();
    }

    /*
     * ==========================================================
     * Turn clock (Speed and Chaos Matches)
     * ==========================================================
     */

    private void startTurnClock() {
        if (settings.hasTimer()) {
            turnStartedAtMillis = System.currentTimeMillis();
        }
    }

    /*
     * Charges `player` for however long their turn actually took, right
     * before play passes away from them.
     */
    private void chargeElapsedTurnTime(Player player) {
        if (!settings.hasTimer() || turnStartedAtMillis == null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - turnStartedAtMillis;
        player.spendTime(elapsed);
    }

    /*
     * How much time `player` has left right now - their saved total, minus
     * however long their turn has been running so far if it's currently
     * their turn. Used both for the "did someone just run out of time"
     * check and for reporting a live-ticking clock to the client.
     */
    private long liveRemainingMillis(Player player) {
        long remaining = player.getRemainingTimeMillis();
        if (player == currentTurn && turnStartedAtMillis != null) {
            remaining -= System.currentTimeMillis() - turnStartedAtMillis;
        }
        return remaining;
    }

    /*
     * Attempts to place a unit from hand into one of the player's front
     * zones. Playing a unit is trap-relevant, so the opponent's face-down
     * traps get a chance to react (see checkTraps).
     */
    public synchronized boolean playUnit(Player player, String cardId, int zoneIndex) {
        PlayRequest request = PlayRequest.placement(this, player, cardId, zoneIndex);
        if (!passesValidation(PlayValidationChains.UNIT_PLAY, request)) {
            return false;
        }

        Unit unit = (Unit) request.getCard();
        player.spendEnergy(unit.getEnergyCost());
        player.getHand().remove(unit);
        player.getFrontZones()[zoneIndex] = unit;

        checkTraps(getOpponent(player), new GameEvent(GameEvent.Type.UNIT_PLAYED, player, unit));
        GameStatistics.getInstance().recordCardPlayed();
        stateChanged();
        return true;
    }

    /*
     * Attempts to cast a spell from hand. targetUnit must belong to the
     * opponent for a DAMAGE spell, to the caster for a BUFF spell, and is
     * optional for a SPECIAL spell.
     */
    public synchronized boolean playSpell(Player player, String cardId, Unit targetUnit) {
        PlayRequest request = PlayRequest.spellCast(this, player, cardId, targetUnit);
        if (!passesValidation(PlayValidationChains.SPELL_PLAY, request)) {
            return false;
        }

        Spell spell = (Spell) request.getCard();

        /*
         * Design pattern note – Memento (Originator): save a complete snapshot of
         * the match state before this spell spends energy, leaves the hand,
         * or mutates any field zones or graveyards. The Caretaker (SpellMementoHistory)
         * holds it on the stack; Match restores it in undoLastSpell().
         */
        spellUndoHistory.push(createSpellMemento(player));

        player.spendEnergy(spell.getEnergyCost());
        player.getHand().remove(spell);

        /*
         * Strategy pattern in action: Match has no idea what this spell
         * actually does, it just runs the spell's own apply() logic.
         */
        spell.apply(this, player, targetUnit);

        if (targetUnit != null) {
            resolveUnitState(targetUnit);
        }

        /*
         * A BUFF spell doesn't get discarded on cast like every other
         * spell - its effect (see Spell.apply above) wraps the target in
         * a new UnitBuff decorator layer that stays attached to the unit.
         * It only reaches the graveyard later, alongside the unit itself,
         * when destroyUnit unwinds that stack of layers - see the note
         * there for why that matters.
         */
        if (spell.getSpellType() != SpellType.BUFF) {
            player.sendToGraveyard(spell);
        }

        GameStatistics.getInstance().recordCardPlayed();
        stateChanged();
        return true;
    }

    /*
     * Attempts to attach a rider (see Jumpy) from hand to one of the
     * player's own units. No new unit lands on a field, so unlike playUnit
     * this isn't trap-relevant. Unit.attach decides what the zone holds
     * afterwards: the first rider turns a plain unit into a UnitGroup, any
     * later one just joins the group already there.
     */
    public synchronized boolean attachRider(Player player, String cardId, Unit targetUnit) {
        PlayRequest request = PlayRequest.attachment(this, player, cardId, targetUnit);
        if (!passesValidation(PlayValidationChains.RIDER_ATTACH, request)) {
            return false;
        }

        Unit rider = (Unit) request.getCard();
        player.spendEnergy(rider.getEnergyCost());
        player.getHand().remove(rider);
        replaceUnit(targetUnit, targetUnit.attach(rider));

        GameStatistics.getInstance().recordCardPlayed();
        stateChanged();
        return true;
    }

    /*
     * Design pattern note - Chain of Responsibility: every play starts its
     * chain here and only sees the final answer. The chain only checks, so
     * nothing has been spent or moved yet if it says no. The reason is
     * server-side logging only - the client just gets the unchanged STATE
     * broadcast, which is what snaps its card back.
     */
    private boolean passesValidation(PlayValidationHandler chain, PlayRequest request) {
        ValidationResult result = chain.handle(request);
        if (!result.accepted()) {
            System.out.println("Play rejected: " + result.reason());
        }
        return result.accepted();
    }

    /*
     * Attempts to place a trap face-down from hand into one of the player's
     * back zones. Traps stay hidden from the opponent until they activate -
     * the network layer should only ever reveal a back-zone count to the
     * opponent, never a trap's identity.
     */
    public synchronized boolean playTrap(Player player, String cardId, int zoneIndex) {
        PlayRequest request = PlayRequest.placement(this, player, cardId, zoneIndex);
        if (!passesValidation(PlayValidationChains.TRAP_PLAY, request)) {
            return false;
        }

        Trap trap = (Trap) request.getCard();
        player.spendEnergy(trap.getEnergyCost());
        player.getHand().remove(trap);
        player.getBackZones()[zoneIndex] = trap;
        GameStatistics.getInstance().recordCardPlayed();
        stateChanged();
        return true;
    }

    /*
     * Every public action that changes the match calls this once it has
     * succeeded. A new action that changes the board must call it too, or
     * spectators behind CachingStateProxy keep seeing the old state.
     */
    private void stateChanged() {
        stateVersion++;
    }

    /*
     * ==========================================================
     * Memento – spell undo
     * ==========================================================
     */

    /**
     * Design pattern – Memento (Concrete Memento, Wide Interface).
     *
     * <p>Private inner class implementing the narrow {@link SpellMemento} interface.
     * Because this class and its fields are strictly private to {@link Match},
     * no other class in the application (including the Caretaker {@link SpellMementoHistory})
     * can inspect, access, or modify the saved state. This guarantees secure data
     * restoration ("saugus duomenų atstatymas").
     */
    private static final class MatchSpellMemento implements SpellMemento {
        private final Player caster;
        private final int casterEnergyBefore;
        private final List<Card> casterHandBefore;
        private final int casterGraveyardSizeBefore;
        private final int opponentGraveyardSizeBefore;
        private final Unit[] player1UnitsBefore;
        private final int[] player1HealthBefore;
        private final game.server.cards.state.UnitState[] player1StatesBefore;
        private final Unit[] player2UnitsBefore;
        private final int[] player2HealthBefore;
        private final game.server.cards.state.UnitState[] player2StatesBefore;

        private MatchSpellMemento(
                Player caster,
                int casterEnergyBefore,
                List<Card> casterHandBefore,
                int casterGraveyardSizeBefore,
                int opponentGraveyardSizeBefore,
                Unit[] player1UnitsBefore,
                int[] player1HealthBefore,
                game.server.cards.state.UnitState[] player1StatesBefore,
                Unit[] player2UnitsBefore,
                int[] player2HealthBefore,
                game.server.cards.state.UnitState[] player2StatesBefore) {
            this.caster = caster;
            this.casterEnergyBefore = casterEnergyBefore;
            this.casterHandBefore = casterHandBefore;
            this.casterGraveyardSizeBefore = casterGraveyardSizeBefore;
            this.opponentGraveyardSizeBefore = opponentGraveyardSizeBefore;
            this.player1UnitsBefore = player1UnitsBefore;
            this.player1HealthBefore = player1HealthBefore;
            this.player1StatesBefore = player1StatesBefore;
            this.player2UnitsBefore = player2UnitsBefore;
            this.player2HealthBefore = player2HealthBefore;
            this.player2StatesBefore = player2StatesBefore;
        }
    }

    /**
     * Design pattern – Memento (Originator, snapshot creation).
     *
     * <p>Captures a complete snapshot of the match state right before a spell is resolved:
     * <ul>
     *   <li>the caster's pre-cast energy</li>
     *   <li>a copy of the caster's hand (including the spell card)</li>
     *   <li>caster and opponent graveyard sizes</li>
     *   <li>the exact units, health, and states across all front zones for both players</li>
     * </ul>
     */
    private SpellMemento createSpellMemento(Player caster) {
        Player opponent = getOpponent(caster);

        int casterEnergyBefore = caster.getEnergy();
        List<Card> casterHandBefore = new ArrayList<>(caster.getHand());
        int casterGraveyardBefore = caster.getGraveyard().size();
        int opponentGraveyardBefore = opponent.getGraveyard().size();

        Unit[] p1Units = new Unit[NUMBER_OF_ZONES];
        int[] p1Health = new int[NUMBER_OF_ZONES];
        game.server.cards.state.UnitState[] p1States = new game.server.cards.state.UnitState[NUMBER_OF_ZONES];
        captureZoneSnapshot(player1.getFrontZones(), p1Units, p1Health, p1States);

        Unit[] p2Units = new Unit[NUMBER_OF_ZONES];
        int[] p2Health = new int[NUMBER_OF_ZONES];
        game.server.cards.state.UnitState[] p2States = new game.server.cards.state.UnitState[NUMBER_OF_ZONES];
        captureZoneSnapshot(player2.getFrontZones(), p2Units, p2Health, p2States);

        return new MatchSpellMemento(
                caster,
                casterEnergyBefore,
                casterHandBefore,
                casterGraveyardBefore,
                opponentGraveyardBefore,
                p1Units,
                p1Health,
                p1States,
                p2Units,
                p2Health,
                p2States
        );
    }

    private void captureZoneSnapshot(
            Unit[] sourceZones,
            Unit[] destUnits,
            int[] destHealth,
            game.server.cards.state.UnitState[] destStates) {

        for (int i = 0; i < NUMBER_OF_ZONES; i++) {
            Unit u = sourceZones[i];
            destUnits[i] = u;
            if (u != null) {
                destHealth[i] = u.getHealth();
                destStates[i] = u.getState();
            }
        }
    }

    /**
     * Design pattern – Memento (Originator, state restore).
     *
     * <p>Pops the most recent spell memento and reverses every change made by that spell:
     * <ol>
     *   <li>Energy is restored to its pre-cast value.</li>
     *   <li>The caster's hand is restored (spell is returned to hand; any drawn/resurrected/cloned cards removed).</li>
     *   <li>Caster and opponent graveyards are trimmed to remove the cast spell, spent buffs, or dead units.</li>
     *   <li>Both players' front field zones are restored (mirages/illusions removed, damage/buffs undone, killed units revived with exact pre-cast stats).</li>
     * </ol>
     *
     * @return {@code true} if the undo succeeded
     */
    public synchronized boolean undoLastSpell(Player player) {
        if (!active || currentTurn != player) {
            return false;
        }

        SpellMemento rawMemento = spellUndoHistory.pop();
        if (!(rawMemento instanceof MatchSpellMemento memento)) {
            return false;
        }

        Player caster = memento.caster;
        Player opponent = getOpponent(caster);

        // 1. Restore caster energy.
        caster.restoreEnergy(memento.casterEnergyBefore);

        // 2. Restore caster hand exactly as it was.
        caster.getHand().clear();
        caster.getHand().addAll(memento.casterHandBefore);

        // 3. Trim caster graveyard back to pre-cast size.
        List<Card> casterGraveyard = caster.getGraveyard();
        while (casterGraveyard.size() > memento.casterGraveyardSizeBefore) {
            casterGraveyard.remove(casterGraveyard.size() - 1);
        }

        // 4. Trim opponent graveyard back to pre-cast size.
        List<Card> opponentGraveyard = opponent.getGraveyard();
        while (opponentGraveyard.size() > memento.opponentGraveyardSizeBefore) {
            opponentGraveyard.remove(opponentGraveyard.size() - 1);
        }

        // 5. Restore all field zones for both players.
        restoreZoneSnapshot(player1.getFrontZones(), memento.player1UnitsBefore, memento.player1HealthBefore, memento.player1StatesBefore);
        restoreZoneSnapshot(player2.getFrontZones(), memento.player2UnitsBefore, memento.player2HealthBefore, memento.player2StatesBefore);

        stateChanged();
        return true;
    }

    private void restoreZoneSnapshot(
            Unit[] targetZones,
            Unit[] savedUnits,
            int[] savedHealth,
            game.server.cards.state.UnitState[] savedStates) {

        for (int i = 0; i < NUMBER_OF_ZONES; i++) {
            Unit u = savedUnits[i];
            targetZones[i] = u;
            if (u != null) {
                u.setInternalHealth(savedHealth[i]);
                u.setState(savedStates[i]);
            }
        }
    }

    /** Whether the current player has a spell they can undo this turn. */
    public synchronized boolean canUndoSpell(Player player) {
        return active && currentTurn == player && spellUndoHistory.hasUndo();
    }

    public synchronized long getStateVersion() {
        return stateVersion;
    }

    /*
     * Ends the current player's turn: units attack (skipped on the very
     * first turn of the match), then play passes to the opponent, who
     * immediately gains their turn resources (+2 energy, one card draw).
     */
    public synchronized boolean endTurn(Player player) {
        if (!active || currentTurn != player) {
            return false;
        }

        chargeElapsedTurnTime(player);

        if (turnNumber > 1) {
            runAttackPhase(player);
        } else {
            System.out.println("Attack phase skipped: first turn of the match.");
        }

        currentTurn = getOpponent(player);
        turnNumber++;
        currentTurn.gainTurnResources();
        runSpawnPhase(currentTurn);
        startTurnClock();

        archerTargets.clear();
        spellUndoHistory.clear();   // undo is scoped to the current turn only
        stateChanged();
        return true;
    }

    /*
     * Every unit belonging to `attacker` hits whatever is directly opposite
     * it, or the defending player directly if that zone is empty.
     */
    private void runAttackPhase(Player attacker) {
        Player defender = getOpponent(attacker);

        for (int zone = 0; zone < NUMBER_OF_ZONES; zone++) {
            Unit attackingUnit = attacker.getFrontZones()[zone];
            if (attackingUnit == null) {
                continue;
            }


            Unit base = attackingUnit.getBaseUnit();
            int targetSlot = NUMBER_OF_ZONES - 1 - zone;

            if (base instanceof Archer) {
                Integer selectedSlot = archerTargets.get(base.getId());

                if (selectedSlot == null) {
                    continue; // skip this archers attack
                }

                targetSlot = selectedSlot;
            }

            Unit defendingUnit = defender.getFrontZones()[targetSlot];

            if (base instanceof Archer) {
                System.out.println("Archer attacks enemy slot " + (targetSlot + 1)
                        + " for " + attackingUnit.getDamage() + " damage; target: "
                        + (defendingUnit == null ? "player" : defendingUnit.getName()));
            }

            int damage = attackingUnit.getDamage();

            if (defendingUnit != null) {
                int healthBefore = defendingUnit.getHealth();

                defendingUnit.takeDamage(damage);

                int actualDamage = healthBefore - defendingUnit.getHealth();
                GameStatistics.getInstance().recordDamageDealt(actualDamage);

                resolveUnitState(defendingUnit);
            } else {
                int healthBefore = defender.getHealth();

                defender.takeDamage(damage);

                int actualDamage = healthBefore - defender.getHealth();
                GameStatistics.getInstance().recordDamageDealt(actualDamage);
            }
        }
    }

    /*
     * Swaps a unit already on the field for a new (buffed) version of
     * itself, in the same zone it already occupies. Used when a buff
     * spell wraps a unit in a new UnitBuff layer - the field's reference
     * to the unit changes, but its position doesn't, and nothing about
     * playing a buff is trap-relevant the way playing a unit is.
     */
    public synchronized void replaceUnit(Unit oldUnit, Unit newUnit) {
        replaceInZones(player1, oldUnit, newUnit);
        replaceInZones(player2, oldUnit, newUnit);
    }

    private void replaceInZones(Player owner, Unit oldUnit, Unit newUnit) {
        Unit[] zones = owner.getFrontZones();
        for (int i = 0; i < zones.length; i++) {
            if (zones[i] == oldUnit) {
                zones[i] = newUnit;
                return;
            }
        }
    }

    /*
     * Removes a unit from whichever front zone it occupies and sends it,
     * and every buff stacked on it, to its owner's graveyard. Used by
     * combat, damage spells, and traps like Bear Trap that destroy a unit
     * outright.
     */
    public synchronized void destroyUnit(Unit unit) {
        if (unit == null) {
            return;
        }
        // Destroy any shallow copies linked to this unit first
        destroyShallowCopiesOf(unit);

        removeFromZones(player1, unit);
        removeFromZones(player2, unit);
    }

    private void destroyShallowCopiesOf(Unit original) {
        destroyShallowCopiesInZones(player1, original);
        destroyShallowCopiesInZones(player2, original);
    }

    private void destroyShallowCopiesInZones(Player owner, Unit original) {
        Unit[] zones = owner.getFrontZones();
        for (int i = 0; i < zones.length; i++) {
            Unit zoneUnit = zones[i];
            // If this unit is a shallow copy of the destroyed original, eliminate it too
            if (zoneUnit != null && zoneUnit.getShallowOriginal() == original) {
                zones[i] = null;
                sendToGraveyardLayered(owner, zoneUnit);
                // Recursively clear out any chains of copies
                destroyShallowCopiesOf(zoneUnit);
            }
        }
    }

    private void removeFromZones(Player owner, Unit unit) {
        Unit[] zones = owner.getFrontZones();
        for (int i = 0; i < zones.length; i++) {
            if (zones[i] == unit) {
                zones[i] = null;
                sendToGraveyardLayered(owner, unit);
                return;
            }
        }
    }

    /*
     * Design pattern note - Decorator: a buffed unit is a stack of
     * UnitBuff wrappers around a base unit card. Dying unwinds that stack
     * from the outside in - the newest buff's own card goes to the
     * graveyard first, then the one beneath it, and so on, with the
     * original unit card always discarded last. A buff never reaches the
     * graveyard any earlier than this, by design - see the note on
     * playSpell for why.
     */
    private void sendToGraveyardLayered(Player owner, Unit unit) {
        Unit current = unit;
        while (current instanceof UnitBuff buff) {
            owner.sendToGraveyard(buff);
            current = buff.getWrapped();
        }

        /*
         * Design pattern note - Composite: under the buffs may be a
         * UnitGroup. Its first child is the unit it carries, which is
         * discarded like any other; the rest are riders, which go back to
         * the hand instead. An empty child list means a leaf - that's how a
         * plain unit answers in the transparent variant (see UnitContainer).
         */
        if (current instanceof UnitContainer group && !group.getChildren().isEmpty()) {
            List<Unit> children = group.getChildren();
            sendToGraveyardLayered(owner, children.get(0));
            for (Unit rider : children.subList(1, children.size())) {
                returnRiderToHand(owner, rider);
            }
            return;
        }

        owner.sendToGraveyard(current);
    }

    /* A full hand has no room for it, so it's discarded instead - like a spawned spell. */
    private void returnRiderToHand(Player owner, Unit rider) {
        rider.reset();
        if (!owner.addToHand(rider)) {
            owner.sendToGraveyard(rider);
        }
    }

    public Player ownerOf(Unit unit) {
        for (Unit candidate : player1.getFrontZones()) {
            if (candidate == unit) {
                return player1;
            }
        }
        for (Unit candidate : player2.getFrontZones()) {
            if (candidate == unit) {
                return player2;
            }
        }
        return null;
    }

    /*
     * Design pattern note - Observer: every face-down trap on `owner`'s
     * field is asked whether this event triggers it, checked from the
     * lowest zone index up (the player's own left-to-right order). Only
     * the first matching trap activates - not all of them - then goes
     * face-up to the graveyard.
     */
    private void checkTraps(Player owner, GameEvent event) {
        notifyObservers(new EventPacket(event, new JsonSerializer()));
        Trap[] zones = owner.getBackZones();

        for (int i = 0; i < zones.length; i++) {
            Trap trap = zones[i];
            if (trap != null && trap.isTriggeredBy(event, owner)) {
                trap.activate(this, owner, event);
                zones[i] = null;
                owner.sendToGraveyard(trap);
                return;
            }
        }
    }

    public synchronized Player getOpponent(Player player) {
        return player == player1 ? player2 : player1;
    }

    public synchronized int getPlayerNumber(Player player) {
        if (player == player1) {
            return 1;
        }
        if (player == player2) {
            return 2;
        }
        return -1;
    }

    public synchronized boolean isCurrentTurn(Player player) {
        return currentTurn == player;
    }

    public synchronized int getCurrentTurnPlayerNumber() {
        return getPlayerNumber(currentTurn);
    }

    /*
     * Looks up which of this match's two Players wraps a given network
     * connection, so MatchmakingService can go from "message arrived on
     * this socket" to "here's the game-state Player that sent it".
     */
    public synchronized Player getPlayerFor(PlayerConnection connection) {
        if (player1.getConnection() == connection) {
            return player1;
        }
        if (player2.getConnection() == connection) {
            return player2;
        }
        return null;
    }

    /*
     * Looks up a unit currently on the field (either side) by its card id,
     * used to resolve a spell's target id coming in from the network.
     */
    public synchronized Unit findUnitOnField(String unitId) {
        if (unitId == null) {
            return null;
        }
        for (Unit unit : player1.getFrontZones()) {
            if (unit != null && unit.getId().equals(unitId)) {
                return unit;
            }
        }
        for (Unit unit : player2.getFrontZones()) {
            if (unit != null && unit.getId().equals(unitId)) {
                return unit;
            }
        }
        return null;
    }

    /*
     * A player is defeated either by health (the existing rule for every
     * mode) or, in a timed match, by their clock running out - including
     * while it's still ticking down mid-turn, not just once they finally
     * act, so a player who simply never moves still loses on schedule.
     */
    public synchronized Player getDefeatedPlayer() {
        if (player1.isDefeated()) {
            return player1;
        }
        if (player2.isDefeated()) {
            return player2;
        }
        if (settings.hasTimer() && liveRemainingMillis(currentTurn) <= 0) {
            return currentTurn;
        }
        return null;
    }

    public synchronized boolean isEnemyUnit(Player player, Unit unit) {
        return ownerOf(unit) == getOpponent(player);
    }

    /* Cripple: every buff off, newest first, into the unit owner's graveyard. */
    public synchronized void stripBuffs(Unit target) {
        Player owner = ownerOf(target);
        if (owner == null || !(target instanceof UnitBuff)) {
            return;
        }

        Unit current = target;
        while (current instanceof UnitBuff buff) {
            owner.sendToGraveyard(buff);
            current = buff.getWrapped();
        }
        replaceInZones(owner, target, current);
    }

    /*
     * After anything that can hurt a unit: send spent buffs to the graveyard
     * (newest first), put the unit that is left back in its zone, then run the
     * death check. Does nothing if the unit is no longer on the field, which
     * is the case after a buff spell or Cripple replaced it.
     */
    private void resolveUnitState(Unit unit) {
        Player owner = ownerOf(unit);
        if (owner == null) {
            return;
        }

        List<Card> discarded = new ArrayList<>();
        Unit current = unit.shedSpentBuffs(discarded);
        for (Card card : discarded) {
            owner.sendToGraveyard(card);
        }

        replaceInZones(owner, unit, current);

        if (current.isDead()) {
            destroyUnit(current);
        }
    }

    private String serializeGraveyardTop(Player player) {
        List<Card> graveyard = player.getGraveyard();
        if (graveyard.isEmpty()) {
            return "-";
        }
        return serializeHandCard(graveyard.get(graveyard.size() - 1));
    }

    public synchronized void endMatch() {
        active = false;
        stateChanged();
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized int getId() {
        return id;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    /*
     * ==========================================================
     * State serialization - protocol v2.
     *
     * Field format, top level split by "|":
     *   STATE|matchId|turnPlayerNumber|viewerHealth|opponentHealth|
     *         viewerEnergy|viewerHand|opponentHandCount|
     *         viewerFront|opponentFront|viewerBack|opponentBackZones|
     *         viewerSecondsLeft|opponentSecondsLeft
     *
     * The last two are "-" whenever this match has no clock (hasTimer() is
     * false); otherwise each is the live, currently-ticking remaining time
     * for whichever of the two players it's still their turn, and their
     * time as of the end of their last turn otherwise.
     *
     * Card entries within a list are ";"-separated, fields within an
     * entry are ","-separated (description is always last so a
     * period/space in it can't be mistaken for a delimiter).
     *
     *   hand entry   - UNIT,id,name,cost,health,maxHealth,damage,element,clan,description
     *                  SPELL,id,name,cost,spellType,description
     *                  TRAP,id,name,cost,condition,description
     *   front entry  - zoneIndex,id,name,cost,health,maxHealth,damage,element,clan,
     *                  healthBuffed,damageBuffed,shieldCount,riderCount
     *   own-back entry - zoneIndex,id,name,cost,condition,description (fully
     *                    revealed - it's the viewer's own trap)
     *
     * opponentBackZones is just a comma-separated list of occupied zone
     * indices ("0,2,4" or "-") - the opponent's traps stay hidden, but the
     * slots they occupy are still visible on the board, same as any TCG.
     * ==========================================================
     */

    public synchronized String buildStateFor(Player viewer) {
        Player opponent = getOpponent(viewer);

        return "STATE|" + id
                + "|" + getCurrentTurnPlayerNumber()
                + "|" + viewer.getHealth()
                + "|" + opponent.getHealth()
                + "|" + viewer.getEnergy()
                + "|" + serializeHand(viewer)
                + "|" + opponent.getHand().size()
                + "|" + serializeFront(viewer.getFrontZones())
                + "|" + serializeFront(opponent.getFrontZones())
                + "|" + serializeOwnBack(viewer.getBackZones())
                + "|" + serializeOccupiedZones(opponent.getBackZones())
                + "|" + serializeRemainingSeconds(viewer)
                + "|" + serializeRemainingSeconds(opponent)
                + "|" + serializeGraveyardTop(viewer)
                + "|" + serializeGraveyardTop(opponent)
                + "|" + serializeArcherTargets(viewer);
    }

    /*
     * Everything about the match at this moment, seen from neither player's
     * side and with nothing hidden - both hands and every trap included.
     * Only the real subject of the spectator proxies (LiveMatchState) calls
     * this; HiddenInformationProxy is what removes the hidden parts before
     * a spectator sees them. Archer targets are left out entirely: not even
     * the opponent can see those.
     */
    public synchronized MatchSnapshot captureSnapshot() {
        return new MatchSnapshot(id, getCurrentTurnPlayerNumber(),
                captureSide(player1), captureSide(player2));
    }

    private SideSnapshot captureSide(Player player) {
        return new SideSnapshot(
                player.getHealth(),
                player.getHand().size(),
                serializeHand(player),
                serializeFront(player.getFrontZones()),
                serializeOwnBack(player.getBackZones()),
                serializeOccupiedZones(player.getBackZones()),
                serializeRemainingSeconds(player),
                serializeGraveyardTop(player));
    }

    /* Whole seconds left on `player`'s clock right now, or -1 when this match has no clock. */
    public synchronized int getSecondsLeft(Player player) {
        if (!settings.hasTimer()) {
            return -1;
        }
        return (int) Math.max(0, liveRemainingMillis(player) / 1000);
    }

    private String serializeArcherTargets(Player viewer) {
        StringBuilder result = new StringBuilder();
        for (int zone = 0; zone < NUMBER_OF_ZONES; zone++) {
            Unit unit = viewer.getFrontZones()[zone];
            if (unit == null) {
                continue;
            }
            Integer target = archerTargets.get(unit.getBaseUnit().getId());
            if (target != null) {
                if (!result.isEmpty()) {
                    result.append(";");
                }
                result.append(zone).append(",").append(target);
            }
        }
        return result.isEmpty() ? "-" : result.toString();
    }

    private String serializeRemainingSeconds(Player player) {
        if (!settings.hasTimer()) {
            return "-";
        }
        long seconds = Math.max(0, liveRemainingMillis(player) / 1000);
        return String.valueOf(seconds);
    }

    private String serializeHand(Player viewer) {
        if (viewer.getHand().isEmpty()) {
            return "-";
        }

        StringBuilder result = new StringBuilder();
        for (Card card : viewer.getHand()) {
            if (!result.isEmpty()) {
                result.append(";");
            }
            result.append(serializeHandCard(card));
        }
        return result.toString();
    }

    private String serializeHandCard(Card card) {
        if (card instanceof Unit unit) {
            return "UNIT," + unit.getId() + "," + unit.getName() + "," + unit.getEnergyCost()
                    + "," + unit.getHealth() + "," + unit.getMaxHealth() + "," + unit.getDamage()
                    + "," + unit.getElement() + "," + (unit.hasClan() ? unit.getClan() : "NONE")
                    + "," + unit.getDescription();
        }
        if (card instanceof Spell spell) {
            return "SPELL," + spell.getId() + "," + spell.getName() + "," + spell.getEnergyCost()
                    + "," + spell.getSpellType() + "," + spell.getDescription();
        }
        Trap trap = (Trap) card;
        return "TRAP," + trap.getId() + "," + trap.getName() + "," + trap.getEnergyCost()
                + "," + trap.getActivationCondition() + "," + trap.getDescription();
    }

    private String serializeFront(Unit[] zones) {
        StringBuilder result = new StringBuilder();

        for (int zone = 0; zone < zones.length; zone++) {
            Unit unit = zones[zone];
            if (unit == null) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(";");
            }

            /*
             * Name and cost come from the original card underneath any
             * buffs - a buffed Knight is still named and costed as a
             * Knight. Health/maxHealth/damage come from the outermost
             * layer, since those are exactly what a buff changes.
             */
            Unit base = unit.getBaseUnit();
            result.append(zone).append(",").append(unit.getId()).append(",").append(base.getName())
                    .append(",").append(base.getEnergyCost())
                    .append(",").append(unit.getHealth()).append(",").append(unit.getMaxHealth())
                    .append(",").append(unit.getDamage()).append(",").append(unit.getElement())
                    .append(",").append(unit.hasClan() ? unit.getClan() : "NONE")
                    .append(",").append(unit.isHealthBuffed() ? 1 : 0)
                    .append(",").append(unit.isDamageBuffed() ? 1 : 0)
                    .append(",").append(unit.getShieldCount())
                    .append(",").append(unit.getRiderCount());
        }

        return result.isEmpty() ? "-" : result.toString();
    }

    /*
     * The viewer's own traps are fully revealed to them - only an opponent
     * needs to be kept in the dark (see serializeOccupiedZones below).
     */
    private String serializeOwnBack(Trap[] zones) {
        StringBuilder result = new StringBuilder();

        for (int zone = 0; zone < zones.length; zone++) {
            Trap trap = zones[zone];
            if (trap == null) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(";");
            }
            result.append(zone).append(",").append(trap.getId()).append(",").append(trap.getName())
                    .append(",").append(trap.getEnergyCost()).append(",")
                    .append(trap.getActivationCondition()).append(",").append(trap.getDescription());
        }

        return result.isEmpty() ? "-" : result.toString();
    }

    private String serializeOccupiedZones(Trap[] zones) {
        StringBuilder result = new StringBuilder();

        for (int zone = 0; zone < zones.length; zone++) {
            if (zones[zone] != null) {
                if (!result.isEmpty()) {
                    result.append(",");
                }
                result.append(zone);
            }
        }

        return result.isEmpty() ? "-" : result.toString();
    }

    public synchronized boolean chooseArcherTarget(
            Player player, String archerId, int targetSlot) {
        return restoreArcherTarget(player, archerId, targetSlot);
    }

    public synchronized int getTurnNumber() {
        return turnNumber;
    }

    public synchronized Integer getArcherTarget(Player player, String archerId) {
        Unit archer = findOwnedArcher(player, archerId);
        return archer == null ? null : archerTargets.get(archer.getId());
    }

    // A null target restores the normal attack on the opposite slot.
    public synchronized boolean restoreArcherTarget(
            Player player, String archerId, Integer targetSlot) {

        if (!active || currentTurn != player) {
            return false;
        }

        if (targetSlot != null && (targetSlot < 0 || targetSlot >= NUMBER_OF_ZONES)) {
            return false;
        }

        Unit archer = findOwnedArcher(player, archerId);
        if (archer == null) {
            return false;
        }
        if (targetSlot == null) {
            archerTargets.remove(archer.getId());
        } else {
            archerTargets.put(archer.getId(), targetSlot);
        }
        stateChanged();
        return true;
    }

    private Unit findOwnedArcher(Player player, String archerId) {
        if (player != player1 && player != player2) {
            return null;
        }

        for (Unit unit : player.getFrontZones()) {
            if (unit == null) {
                continue;
            }

            Unit base = unit.getBaseUnit();

            if (base instanceof Archer
                    && (unit.getId().equals(archerId) || base.getId().equals(archerId))) {
                return base;
            }
        }

        return null;
    }

    /*
     * The lowest-index empty front zone for `owner`, or -1 if the row is
     * full. Written as its own method because it needs to be re-queried
     * fresh for every individual creator's spawn attempt - including when
     * an earlier creator in the same turn just filled the only open zone.
     */
    public synchronized int findEmptyFrontZone(Player owner) {
        Unit[] zones = owner.getFrontZones();
        for (int i = zones.length - 1; i >= 0; i--) {
            if (zones[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public synchronized int findEmptyBackZone(Player owner) {
        Trap[] zones = owner.getBackZones();
        for (int i = zones.length - 1; i >= 0; i--) {
            if (zones[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Places a unit directly into a zone with no energy cost and no hand
     * involved - used by UnitCreator.spawn for passive-effect cards like
     * Tombstone. Fires the same trap check as playUnit, so a unit appearing
     * on the field is trap-relevant regardless of how it got there. (Worth
     * a second look: BearTrap's condition was written for the enemy playing
     * a unit CARD - whether a spawned unit should count is a judgment call,
     * not something the earlier conversation settled either way.)
     */
    public synchronized void placeUnitForFree(Player owner, int zoneIndex, Unit unit) {
        if (zoneIndex < 0 || zoneIndex >= NUMBER_OF_ZONES || owner.getFrontZones()[zoneIndex] != null) {
            return;
        }
        owner.getFrontZones()[zoneIndex] = unit;
        checkTraps(getOpponent(owner), new GameEvent(GameEvent.Type.UNIT_PLAYED, owner, unit));
    }

    /*
     * Places a trap face-down for free, for creators. Unlike a unit, a trap
     * appearing isn't an event anything reacts to, so there is no trap check.
     */
    public synchronized void placeTrapForFree(Player owner, int zoneIndex, Trap trap) {
        if (zoneIndex < 0 || zoneIndex >= NUMBER_OF_ZONES || owner.getBackZones()[zoneIndex] != null) {
            return;
        }
        owner.getBackZones()[zoneIndex] = trap;
    }

    /*
     * Lets every unit belonging to `owner` act on its onOwnerTurnStart hook,
     * in left-to-right zone order - a fixed snapshot, so a unit a creator
     * spawns mid-phase doesn't also get a turn in the same phase it was
     * just created, and so two creators fairly race for the same empty
     * zone rather than racing against a mutating array.
     */
    private void runSpawnPhase(Player owner) {
        Unit[] snapshot = owner.getFrontZones().clone();
        for (Unit unit : snapshot) {
            if (unit != null) {
                unit.onOwnerTurnStart(this, owner);
            }
        }
    }
}
