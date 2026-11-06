package game.server.validation;

/**
 * Concrete Handler - only checks the energy, never spends it. Match spends it
 * once the whole chain has passed, so a rejected play costs nothing. This is
 * also why it belongs last in a chain.
 */
public class EnoughEnergyHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        int cost = request.getCard().getEnergyCost();
        int energy = request.getPlayer().getEnergy();
        if (energy < cost) {
            return ValidationResult.rejected("Not enough energy: needs " + cost + ", has " + energy);
        }
        return passToNext(request);
    }
}
