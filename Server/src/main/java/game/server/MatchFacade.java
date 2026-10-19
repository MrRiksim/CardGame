package game.server;

import game.server.cards.Unit;

public class MatchFacade {

    private final Match match;

    public MatchFacade(Match match) {
        this.match = match;
    }

    public synchronized boolean playUnit(Player player, String cardId, int zoneIndex) {
        return match.playUnit(player, cardId, zoneIndex);
    }

    public synchronized boolean playSpell(Player player, String cardId, Unit targetUnit) {
        return match.playSpell(player, cardId, targetUnit);
    }

    public synchronized boolean playTrap(Player player, String cardId, int zoneIndex) {
        return match.playTrap(player, cardId, zoneIndex);
    }

    public synchronized boolean endTurn(Player player) {
        return match.endTurn(player);
    }

    public synchronized boolean chooseArcherTarget(Player player, String archerId, int targetSlot) {
        return match.chooseArcherTarget(player, archerId, targetSlot);
    }

    public synchronized boolean restoreArcherTarget(Player player, String archerId, Integer targetSlot) {
        return match.restoreArcherTarget(player, archerId, targetSlot);
    }

    public synchronized Integer getArcherTarget(Player player, String archerId) {
        return match.getArcherTarget(player, archerId);
    }

    public synchronized int getTurnNumber() {
        return match.getTurnNumber();
    }

    public synchronized String getGameStateFor(Player viewer) {
        return match.buildStateFor(viewer);
    }

    public synchronized boolean isActive() {
        return match.isActive();
    }

    public synchronized void endMatch() {
        match.endMatch();
    }

    public synchronized int getId() {
        return match.getId();
    }

    public synchronized Player getPlayerFor(PlayerConnection connection) {
        return match.getPlayerFor(connection);
    }

    public synchronized Player getOpponent(Player player) {
        return match.getOpponent(player);
    }

    public synchronized int getPlayerNumber(Player player) {
        return match.getPlayerNumber(player);
    }

    public synchronized int getCurrentTurnPlayerNumber() {
        return match.getCurrentTurnPlayerNumber();
    }

    public synchronized Unit findUnitOnField(String unitId) {
        return match.findUnitOnField(unitId);
    }

    public synchronized Player getDefeatedPlayer() {
        return match.getDefeatedPlayer();
    }

    public synchronized boolean hasTimer() {
        return match.hasTimer();
    }

    public synchronized Player getPlayer1() {
        return match.getPlayer1();
    }

    public synchronized Player getPlayer2() {
        return match.getPlayer2();
    }
}