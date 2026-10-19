package game.server.cards;
import game.server.cards.factories.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
public class ElementalCardService {
    // The family of concrete factories:
    private static final List<AbstractCardFactory> FACTORIES = List.of(
            new FireFactory(),
            new WaterFactory(),
            new WindFactory(),
            new EarthFactory()
    );
    public static Card createRandomElementalCard() {
        // 1. Pick a random concrete factory (e.g., Fire, Wind, etc.)
        AbstractCardFactory factory = FACTORIES.get(
                ThreadLocalRandom.current().nextInt(FACTORIES.size())
        );
        // 2. Use that factory polymorphically to create either a Unit or a Spell
        boolean pickUnit = ThreadLocalRandom.current().nextBoolean();
        return pickUnit ? factory.createUnit() : factory.createSpell();
    }
}