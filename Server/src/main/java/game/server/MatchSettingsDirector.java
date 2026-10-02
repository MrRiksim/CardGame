package game.server;

/**
 * <b>Design pattern - Builder (Director).</b> Knows the one fixed sequence
 * every match's settings get assembled in; knows nothing about how any
 * individual step is filled in - that's entirely up to whichever concrete
 * {@link MatchBuilder} it's handed. Adding a fourth game mode never
 * touches this class, only {@link MatchmakingService}'s mode-to-builder
 * mapping.
 */
public class MatchSettingsDirector {

    public MatchSettings direct(MatchBuilder builder) {
        return builder
                .setHealth()
                .setEnergy()
                .setHandSizes()
                .setTimer()
                .build();
    }
}
