package game.server.validation;

import game.server.cards.Card;

/**
 * Concrete Handler - only checks the energy, never spends it. Match spends it
 * once the whole chain has passed, so a rejected play costs nothing. This is
 * also why it belongs last in a chain.
 */
public class EnoughEnergyHandler extends PlayValidationHandler {

    @Override
    public ValidationResult handle(PlayRequest request) {
        Card card = cardOf(request);
        if (card == null) {
            return notInHand();
        }
        int cost = card.getEnergyCost();
        int energy = request.getPlayer().getEnergy();
        if (energy < cost) {
            return ValidationResult.rejected("Not enough energy: needs " + cost + ", has " + energy);
        }
        return passToNext(request);
    }
}
