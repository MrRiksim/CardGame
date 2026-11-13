package game.server.spectating;

/**
 * One player's half of a {@link MatchSnapshot}, already in the wire formats
 * {@code Match.buildStateFor} uses for the same pieces (hand entries, front
 * entries, own-back entries, occupied zone indices). {@code hand} and
 * {@code back} are null when they have been hidden - see
 * {@link HiddenInformationProxy}. Everything else is public information.
 */
public record SideSnapshot(
        int health,
        int handCount,
        String hand,
        String front,
        String back,
        String backZones,
        String secondsLeft,
        String graveyardTop) {

    /* The same side with the hand and the trap identities removed. */
    public SideSnapshot withoutHiddenInformation() {
        return new SideSnapshot(health, handCount, null, front, null, backZones, secondsLeft, graveyardTop);
    }

    public boolean isHandHidden() {
        return hand == null;
    }

    public boolean areTrapsHidden() {
        return back == null;
    }
}
