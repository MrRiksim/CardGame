package game.client;

/**
 * One match a player could spectate, from the server's MATCHES list.
 */
public record MatchListing(int matchId, String mode, int turnNumber) {

    @Override
    public String toString() {
        return "Match " + matchId + "  -  " + mode + "  -  turn " + turnNumber;
    }
}
