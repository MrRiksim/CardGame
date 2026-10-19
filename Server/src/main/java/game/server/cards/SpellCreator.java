package game.server.cards;

import game.server.Match;
import game.server.Player;

/**
 * The middle level for creators that make spells: a spell isn't placed on
 * the board, so WHERE here is simply the owner's hand - unless it's full, in
 * which case nothing is made.
 *
 * <p>Whether the hand has room is decided at the moment spawn() runs. Match
 * draws the owner's card for the turn first, so a hand with one free slot is
 * full by the time a spell creator gets its turn: the normal draw always
 * wins, and the spawn is the one that fails.
 */
public abstract class SpellCreator extends CardCreator {

    @Override
    public abstract Spell createCard();

    /*
     * `match` isn't needed - a hand belongs to the player alone - but the
     * signature is fixed by CardCreator.
     */
    @Override
    public final void spawn(Match match, Player owner) {
        if (owner.isHandFull()) {
            return;
        }
        owner.addToHand(createCard());
    }
}
