package game.server;

import game.server.cards.Card;
import game.server.cards.Spell;
import game.server.cards.Trap;
import game.server.cards.Unit;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Runs a single game between two players: playing units/spells/traps,
 * ending turns, resolving the end-of-turn attack phase and checking trap
 * activations.
 *
 * <p>Rules implemented here: 25 starting health, 6 starting energy, 5
 * starting hand cards (7 max), +2 energy and a card draw at the start of
 * each turn, and units attacking whatever is opposite them (or the player,
 * if the opposite zone is empty) at the end of every turn except the very
 * first.
 */
public class Match {

    public static final int NUMBER_OF_ZONES = Player.NUMBER_OF_ZONES;

    private final int id;
    private final Player player1;
    private final Player player2;

    private Player currentTurn;
    private int turnNumber = 1;
    private boolean active = true;

    public Match(int id, PlayerConnection connection1, PlayerConnection connection2) {
        this.id = id;
        this.player1 = new Player(connection1);
        this.player2 = new Player(connection2);

        /*
         * Randomly select who goes first.
         */
        currentTurn = ThreadLocalRandom.current().nextBoolean() ? player1 : player2;
    }

    /*
     * Attempts to place a unit from hand into one of the player's front
     * zones. Playing a unit is trap-relevant, so the opponent's face-down
     * traps get a chance to react (see checkTraps).
     */
    public synchronized boolean playUnit(Player player, String cardId, int zoneIndex) {
        if (!active || currentTurn != player) {
            return false;
        }
        if (zoneIndex < 0 || zoneIndex >= NUMBER_OF_ZONES) {
            return false;
        }
        if (player.getFrontZones()[zoneIndex] != null) {
            return false;
        }

        Card card = player.findInHand(cardId);
        if (!(card instanceof Unit unit)) {
            return false;
        }
        if (!player.spendEnergy(unit.getEnergyCost())) {
            return false;
        }

        player.getHand().remove(card);
        player.getFrontZones()[zoneIndex] = unit;

        checkTraps(getOpponent(player), new GameEvent(GameEvent.Type.UNIT_PLAYED, player, unit));

        return true;
    }

    /*
     * Attempts to cast a spell from hand. targetUnit must belong to the
     * opponent for a DAMAGE spell, to the caster for a BUFF spell, and is
     * optional for a SPECIAL spell.
     */
    public synchronized boolean playSpell(Player player, String cardId, Unit targetUnit) {
        if (!active || currentTurn != player) {
            return false;
        }

        Card card = player.findInHand(cardId);
        if (!(card instanceof Spell spell)) {
            return false;
        }
        if (!isValidSpellTarget(spell, player, targetUnit)) {
            return false;
        }
        if (!player.spendEnergy(spell.getEnergyCost())) {
            return false;
        }

        player.getHand().remove(card);

        /*
         * Strategy pattern in action: Match has no idea what this spell
         * actually does, it just runs the spell's own apply() logic.
         */
        spell.apply(this, player, targetUnit);

        if (targetUnit != null && targetUnit.isDead()) {
            destroyUnit(targetUnit);
        }

        player.sendToGraveyard(spell);
        return true;
    }

    private boolean isValidSpellTarget(Spell spell, Player caster, Unit target) {
        return switch (spell.getSpellType()) {
            case DAMAGE -> target != null && ownerOf(target) == getOpponent(caster);
            case BUFF -> target != null && ownerOf(target) == caster;
            case SPECIAL -> true;
        };
    }

    /*
     * Attempts to place a trap face-down from hand into one of the player's
     * back zones. Traps stay hidden from the opponent until they activate -
     * the network layer should only ever reveal a back-zone count to the
     * opponent, never a trap's identity.
     */
    public synchronized boolean playTrap(Player player, String cardId, int zoneIndex) {
        if (!active || currentTurn != player) {
            return false;
        }
        if (zoneIndex < 0 || zoneIndex >= NUMBER_OF_ZONES) {
            return false;
        }
        if (player.getBackZones()[zoneIndex] != null) {
            return false;
        }

        Card card = player.findInHand(cardId);
        if (!(card instanceof Trap trap)) {
            return false;
        }
        if (!player.spendEnergy(trap.getEnergyCost())) {
            return false;
        }

        player.getHand().remove(card);
        player.getBackZones()[zoneIndex] = trap;
        return true;
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

        if (turnNumber > 1) {
            runAttackPhase(player);
        }

        currentTurn = getOpponent(player);
        turnNumber++;
        currentTurn.gainTurnResources();

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

            Unit defendingUnit = defender.getFrontZones()[4 - zone];

            if (defendingUnit != null) {
                defendingUnit.takeDamage(attackingUnit.getDamage());
                if (defendingUnit.isDead()) {
                    destroyUnit(defendingUnit);
                }
            } else {
                defender.takeDamage(attackingUnit.getDamage());
            }
        }
    }

    /*
     * Removes a unit from whichever front zone it occupies and sends it to
     * its owner's graveyard. Used by combat, damage spells, and traps like
     * Bear Trap that destroy a unit outright.
     */
    public synchronized void destroyUnit(Unit unit) {
        if (unit == null) {
            return;
        }
        removeFromZones(player1, unit);
        removeFromZones(player2, unit);
    }

    private void removeFromZones(Player owner, Unit unit) {
        Unit[] zones = owner.getFrontZones();
        for (int i = 0; i < zones.length; i++) {
            if (zones[i] == unit) {
                zones[i] = null;
                owner.sendToGraveyard(unit);
                return;
            }
        }
    }

    private Player ownerOf(Unit unit) {
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
        Trap[] zones = owner.getBackZones();

        for (int i = zones.length - 1; i >= 0; i--) {
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

    public synchronized Player getDefeatedPlayer() {
        if (player1.isDefeated()) {
            return player1;
        }
        if (player2.isDefeated()) {
            return player2;
        }
        return null;
    }

    public synchronized void endMatch() {
        active = false;
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
     *         viewerFront|opponentFront|viewerBack|opponentBackZones
     *
     * Card entries within a list are ";"-separated, fields within an
     * entry are ","-separated (description is always last so a
     * period/space in it can't be mistaken for a delimiter).
     *
     *   hand entry   - UNIT,id,name,cost,health,maxHealth,damage,element,clan,description
     *                  SPELL,id,name,cost,spellType,description
     *                  TRAP,id,name,cost,condition,description
     *   front entry  - zoneIndex,id,name,cost,health,maxHealth,damage,element,clan
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
                + "|" + serializeOccupiedZones(opponent.getBackZones());
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
            result.append(zone).append(",").append(unit.getId()).append(",").append(unit.getName())
                    .append(",").append(unit.getEnergyCost())
                    .append(",").append(unit.getHealth()).append(",").append(unit.getMaxHealth())
                    .append(",").append(unit.getDamage()).append(",").append(unit.getElement())
                    .append(",").append(unit.hasClan() ? unit.getClan() : "NONE");
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
}
