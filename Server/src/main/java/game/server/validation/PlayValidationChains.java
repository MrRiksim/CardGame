package game.server.validation;

import game.server.Player;
import game.server.cards.Card;
import game.server.cards.Spell;
import game.server.cards.Trap;
import game.server.cards.Unit;

/**
 * <b>Design pattern - Chain of Responsibility:</b> assembles the chains
 * {@link game.server.Match} starts its plays with, from the same links in
 * whatever combination each kind of play needs. The order matters: a link
 * that fails stops everything after it.
 *
 * <p>Three ways to validate:
 * <ul>
 *   <li><b>The ready-made chains</b> ({@link #UNIT_PLAY}, {@link #TRAP_PLAY},
 *       {@link #SPELL_PLAY}, {@link #RIDER_ATTACH}) - built once, shared by
 *       every match.</li>
 *   <li><b>A standalone validation</b> - one link on its own, e.g.
 *       {@code PlayValidationChains.enoughEnergy().handle(request)}. Every
 *       link works without the ones that normally come before it: links that
 *       need the card look it up themselves, and the zone-free check guards
 *       its own index.</li>
 *   <li><b>A custom chain</b> - {@link #chainOf} links any of those in the
 *       order given.</li>
 * </ul>
 *
 * <p>Each standalone method returns a fresh, unlinked handler. Linking
 * changes a handler, so don't pass one of the shared chains (or a link
 * already in a chain) to {@link #chainOf}.
 */
public final class PlayValidationChains {

    public static final PlayValidationHandler UNIT_PLAY = chainOf(
            matchActive(), playersTurn(), cardInHand(), cardKind(Unit.class),
            zoneInRange(), frontZoneFree(), enoughEnergy());

    public static final PlayValidationHandler TRAP_PLAY = chainOf(
            matchActive(), playersTurn(), cardInHand(), cardKind(Trap.class),
            zoneInRange(), backZoneFree(), enoughEnergy());

    public static final PlayValidationHandler SPELL_PLAY = chainOf(
            matchActive(), playersTurn(), cardInHand(), cardKind(Spell.class),
            spellTargetType(), spellCardRule(), enoughEnergy());

    public static final PlayValidationHandler RIDER_ATTACH = chainOf(
            matchActive(), playersTurn(), cardInHand(), cardKind(Unit.class),
            riderCard(), ownUnitTarget(), enoughEnergy());

    private PlayValidationChains() {
    }

    /* Links the handlers in the order given and returns the first one, which is where a chain is started. */
    public static PlayValidationHandler chainOf(PlayValidationHandler first, PlayValidationHandler... rest) {
        PlayValidationHandler last = first;
        for (PlayValidationHandler next : rest) {
            last = last.linkTo(next);
        }
        return first;
    }

    /*
     * ==========================================================
     * Standalone validations - each a single, unlinked link.
     * ==========================================================
     */

    public static PlayValidationHandler matchActive() {
        return new MatchActiveHandler();
    }

    public static PlayValidationHandler playersTurn() {
        return new PlayersTurnHandler();
    }

    public static PlayValidationHandler cardInHand() {
        return new CardInHandHandler();
    }

    public static PlayValidationHandler cardKind(Class<? extends Card> kind) {
        return new CardKindHandler(kind);
    }

    public static PlayValidationHandler zoneInRange() {
        return new ZoneInRangeHandler();
    }

    public static PlayValidationHandler frontZoneFree() {
        return new ZoneFreeHandler(Player::getFrontZones);
    }

    public static PlayValidationHandler backZoneFree() {
        return new ZoneFreeHandler(Player::getBackZones);
    }

    public static PlayValidationHandler spellTargetType() {
        return new SpellTargetTypeHandler();
    }

    public static PlayValidationHandler spellCardRule() {
        return new SpellCardRuleHandler();
    }

    public static PlayValidationHandler riderCard() {
        return new RiderCardHandler();
    }

    public static PlayValidationHandler ownUnitTarget() {
        return new OwnUnitTargetHandler();
    }

    public static PlayValidationHandler enoughEnergy() {
        return new EnoughEnergyHandler();
    }
}
