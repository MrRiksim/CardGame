package game.server.cards;

import game.server.cards.spells.*;
import game.server.cards.traps.*;
import game.server.cards.units.*;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * <b>Design pattern - Factory Method:</b> this is the real factory in the card
 * hierarchy (Card/Unit/Spell/Trap themselves are just abstraction, not a
 * factory - see the note in {@link Card}).
 *
 * <p>Every concrete card (see {@link Knight}, {@link LightningStrike},
 * {@link BearTrap} and friends) exposes its own static {@code create()}
 * method - a Factory Method - that knows how to build itself. This class is
 * the "creator" that calls one of those methods without needing to know the
 * concrete constructors. Adding a new card to the game means writing the
 * class and adding its creator to {@link #STANDARD_CARDS}; named creation
 * and random draws then share that creator. Elemental and starting-only
 * cards are also registered by {@link #createCatalog()} for chat commands.
 * Note that buff spells never hand out a {@code UnitBuff} here -
 * that's a decorator created only when the spell is actually cast (see
 * {@code HealthBoostSpell.apply}), never something drawn into a hand.
 *
 * <p>For now every card is equally likely and hands/draws simply pick at
 * random from the cards below; a real deck-building system can replace
 * {@link #CARD_POOL} later without touching any calling code.
 */
public class CardFactory {

    private static final List<Supplier<Card>> STANDARD_CARDS = List.of(
            Knight::create,
            LightningStrike::create,
            ChainLightningSpell::create,
            HealingWaveSpell::create,
            RaiseTheFallenSpell::create,
            BearTrap::create,
            HealthBoostSpell::create,
            DamagePotionSpell::create,
            () -> new SkeletonCardAdapter(
                    new SkeletonCard(5,5, 1, 2, 2, 3)
            ),
            Archer::create,
            ShiftingSpellCard::create,
            Skelly::create,
            Tombstone::create,
            ShieldSpell::create,
            CrippleSpell::create,
            HealSpell::create,
            Hunter::create,
            Brewer::create,
            Jumpy::create
    );

    private static final List<Supplier<Card>> CARD_POOL = createRandomPool();
    private static final Map<String, CardDefinition> CARD_CATALOG = createCatalog();

    private record CardDefinition(String name, Supplier<Card> creator) {}

    private static List<Supplier<Card>> createRandomPool() {
        List<Supplier<Card>> pool = new ArrayList<>(STANDARD_CARDS);
        for (int i = 0; i < 8; i++) {
            pool.add(ElementalCardService::createRandomElementalCard);
        }
        return List.copyOf(pool);
    }

    private static Map<String, CardDefinition> createCatalog() {
        List<Supplier<Card>> creators = new ArrayList<>(STANDARD_CARDS);
        creators.add(PhantomMirrorSpell::create);
        creators.add(EssenceReplicationSpell::create);
        creators.add(FireUnit::new);
        creators.add(WaterUnit::new);
        creators.add(EarthUnit::new);
        creators.add(WindUnit::new);
        creators.add(FireSpell::create);
        creators.add(WaterSpell::create);
        creators.add(EarthSpell::create);
        creators.add(WindSpell::create);
        Map<String, CardDefinition> catalog = new LinkedHashMap<>();
        for (Supplier<Card> creator : creators) {
            String name = creator.get().getName();
            catalog.put(normalizeName(name), new CardDefinition(name, creator));
        }
        return Map.copyOf(catalog);
    }

    private static String normalizeName(String name) {
        return name.strip().replace('_', ' ').replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Named creation shares the existing card creators; every call produces a fresh card ID. */
    public static Optional<Card> createByName(String name) {
        CardDefinition definition = CARD_CATALOG.get(normalizeName(name));
        return definition == null ? Optional.empty() : Optional.of(definition.creator().get());
    }

    public static List<String> getCardNames() {
        return CARD_CATALOG.values().stream().map(CardDefinition::name).sorted().toList();
    }

    private CardFactory() {
    }

    public static Card createRandomCard() {
        int index = ThreadLocalRandom.current().nextInt(CARD_POOL.size());
        return CARD_POOL.get(index).get();
    }
}
