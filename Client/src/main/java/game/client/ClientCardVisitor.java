package game.client;

/**
 * <b>Design pattern - Visitor (Visitor interface):</b> one method per concrete
 * card type. A new operation on cards (building the info table, working out
 * where a card can be played, ...) is a new visitor class - ClientUnit,
 * ClientSpell and ClientTrap never have to change, and nobody has to write
 * {@code instanceof} chains to tell the card types apart.
 *
 * <p>Double dispatch: the caller does {@code card.accept(visitor)}, and the
 * card itself picks the right {@code visitXxx(this)} method.
 */
public interface ClientCardVisitor {

    void visitUnit(ClientUnit unit);

    void visitSpell(ClientSpell spell);

    void visitTrap(ClientTrap trap);
}
