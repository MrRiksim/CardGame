package game.server.cards;
import game.server.Player;
import game.server.Match;

public interface SpellStrategy {
    void execute(Player caster, SpellTargetContext context, Match match);
}