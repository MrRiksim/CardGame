package game.server.cards;
import game.server.cards.spells.*;
import game.server.cards.traps.*;
import game.server.cards.units.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/*
The abstract factory design patter is going to be used for streamlining the process of creating
different card types with different families
 */
public interface AbstractCardFactory {
    Unit createUnit();
    Spell createSpell();
}
