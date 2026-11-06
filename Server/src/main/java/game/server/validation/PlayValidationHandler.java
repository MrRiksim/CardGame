package game.server.validation;

/**
 * <b>Design pattern - Chain of Responsibility (Handler):</b> one link in the
 * chain that decides whether a card may be played. A link handles a request
 * by rejecting it; if it has no objection, it hands the request to the next
 * link itself. Whoever starts a chain only ever knows its first link, and
 * reaching the end of the chain means the play is accepted.
 *
 * <p>Links hold no per-play state (that all lives in {@link PlayRequest}), so
 * one built chain can safely be shared by every match - see
 * {@link PlayValidationChains}.
 */
public abstract class PlayValidationHandler {

    private PlayValidationHandler next;

    /* Returns the link it was given, so a chain can be built in one call chain. */
    public PlayValidationHandler linkTo(PlayValidationHandler next) {
        this.next = next;
        return next;
    }

    public abstract ValidationResult handle(PlayRequest request);

    protected final ValidationResult passToNext(PlayRequest request) {
        return next == null ? ValidationResult.ok() : next.handle(request);
    }
}
