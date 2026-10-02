package game.server;

/**
 * <b>Design pattern - Builder (Product).</b> Everything that used to be a
 * hardcoded constant on {@link Player} (25 health, 6 energy, and so on)
 * lives here instead, one instance per match rather than one fixed value
 * shared by every match in the process. See {@link MatchBuilder} for how
 * an instance of this actually gets assembled.
 *
 * <p>{@code timePerPlayerMillis} is nullable - most matches have no clock
 * at all, so "no timer" is a real, first-class state here rather than a
 * magic number like 0 or -1.
 */
public record MatchSettings(
        int startingHealth,
        int startingEnergy,
        int energyPerTurn,
        int startingHandSize,
        int maxHandSize,
        Long timePerPlayerMillis) {

    public boolean hasTimer() {
        return timePerPlayerMillis != null;
    }
}
