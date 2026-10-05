package game.server.commands;

public interface GameCommand {
    boolean execute();
    boolean undo();
}
