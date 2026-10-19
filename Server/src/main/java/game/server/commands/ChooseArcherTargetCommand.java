package game.server.commands;

import game.server.MatchFacade;
import game.server.Player;

public final class ChooseArcherTargetCommand implements GameCommand {
    private final MatchFacade matchFacade;
    private final Player player;
    private final String archerId;
    private final int targetSlot;
    private Integer previousTarget;
    private int executionTurn;
    private boolean executed;

    public ChooseArcherTargetCommand(MatchFacade matchFacade, Player player,
                                     String archerId, int targetSlot) {
        this.matchFacade = matchFacade;
        this.player = player;
        this.archerId = archerId;
        this.targetSlot = targetSlot;
    }

    @Override
    public boolean execute() {
        synchronized (matchFacade) {
            if (executed) {
                return false;
            }
            previousTarget = matchFacade.getArcherTarget(player, archerId);
            if (!matchFacade.chooseArcherTarget(player, archerId, targetSlot)) {
                return false;
            }
            executionTurn = matchFacade.getTurnNumber();
            executed = true;
            return true;
        }
    }

    @Override
    public boolean undo() {
        synchronized (matchFacade) {
            if (!executed || executionTurn != matchFacade.getTurnNumber()) {
                return false;
            }
            if (!matchFacade.restoreArcherTarget(player, archerId, previousTarget)) {
                return false;
            }
            executed = false;
            return true;
        }
    }
}