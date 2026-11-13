package game.server.spectating;

/**
 * Everything a spectator could be shown about one match at one moment, seen
 * from neither player's side: player 1 and player 2 rather than "you" and
 * "the opponent". Immutable, so one snapshot can be sent to any number of
 * spectators (and cached - see {@link CachingStateProxy}).
 */
public record MatchSnapshot(int matchId, int turnPlayer, SideSnapshot player1, SideSnapshot player2) {

    public MatchSnapshot withoutHiddenInformation() {
        return new MatchSnapshot(matchId, turnPlayer,
                player1.withoutHiddenInformation(), player2.withoutHiddenInformation());
    }
}
