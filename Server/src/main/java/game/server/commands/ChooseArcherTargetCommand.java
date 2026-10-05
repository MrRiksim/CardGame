package game.server.commands;

import game.server.Match;
import game.server.Player;

public final class ChooseArcherTargetCommand implements GameCommand {
    private final Match match;
    private final Player player;
    private final String archerId;
    private final int targetSlot;
    private Integer previousTarget;
    private int executionTurn;
    private boolean executed;

    public ChooseArcherTargetCommand(Match match, Player player,
                                    String archerId, int targetSlot) {
        this.match = match;
        this.player = player;
        this.archerId = archerId;
        this.targetSlot = targetSlot;
    }

    @Override
    public boolean execute() {
        synchronized (match) {
            if (executed) {
                return false;
            }
            previousTarget = match.getArcherTarget(player, archerId);
            if (!match.chooseArcherTarget(player, archerId, targetSlot)) {
                return false;
            }
            executionTurn = match.getTurnNumber();
            executed = true;
            return true;
        }
    }

    @Override
    public boolean undo() {
        synchronized (match) {
            if (!executed || executionTurn != match.getTurnNumber()) {
                return false;
            }
            if (!match.restoreArcherTarget(player, archerId, previousTarget)) {
                return false;
            }
            executed = false;
            return true;
        }
    }
}
