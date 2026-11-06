package game.server.validation;

import game.server.Player;
import game.server.cards.Card;
import game.server.cards.Spell;
import game.server.cards.Trap;
import game.server.cards.Unit;

import java.util.function.Function;

/**
 * <b>Design pattern - Chain of Responsibility:</b> assembles the chains
 * {@link game.server.Match} starts its plays with, from the same links in
 * whatever combination each kind of play needs. The order matters: a link
 * that fails stops everything after it, and later links rely on what earlier
 * ones established (the card was found, it's the right kind, the zone exists).
 */
public final class PlayValidationChains {

    public static final PlayValidationHandler UNIT_PLAY =
            placementChain(Unit.class, Player::getFrontZones);

    public static final PlayValidationHandler TRAP_PLAY =
            placementChain(Trap.class, Player::getBackZones);

    public static final PlayValidationHandler SPELL_PLAY = spellChain();

    private PlayValidationChains() {
    }

    private static PlayValidationHandler placementChain(
            Class<? extends Card> kind, Function<Player, Card[]> row) {

        PlayValidationHandler first = new MatchActiveHandler();
        first.linkTo(new PlayersTurnHandler())
                .linkTo(new CardInHandHandler())
                .linkTo(new CardKindHandler(kind))
                .linkTo(new ZoneInRangeHandler())
                .linkTo(new ZoneFreeHandler(row))
                .linkTo(new EnoughEnergyHandler());
        return first;
    }

    private static PlayValidationHandler spellChain() {
        PlayValidationHandler first = new MatchActiveHandler();
        first.linkTo(new PlayersTurnHandler())
                .linkTo(new CardInHandHandler())
                .linkTo(new CardKindHandler(Spell.class))
                .linkTo(new SpellTargetTypeHandler())
                .linkTo(new SpellCardRuleHandler())
                .linkTo(new EnoughEnergyHandler());
        return first;
    }
}
