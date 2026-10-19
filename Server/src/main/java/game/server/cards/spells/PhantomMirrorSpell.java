package game.server.cards.spells;

import game.server.Match;
import game.server.Player;
import game.server.cards.Card;
import game.server.cards.Spell;
import game.server.cards.SpellType;
import game.server.cards.Unit;

public class PhantomMirrorSpell extends Spell {

    public PhantomMirrorSpell() {
        super(
                "Phantom Mirror",
                "Targets an enemy unit and creates a linked shallow-copy illusion in the corresponding mirrored front zone.",
                3,
                SpellType.SPECIAL
        );
    }

    @Override
    public boolean canTarget(Match match, Player caster, Unit target) {
        if (target == null || !match.isEnemyUnit(caster, target)) {
            return false;
        }

        // Find which zone index the target occupies on the opponent's board
        Player opponent = match.getOpponent(caster);
        Unit[] opponentZones = opponent.getFrontZones();
        int targetIndex = -1;
        for (int i = 0; i < opponentZones.length; i++) {
            if (opponentZones[i] == target) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            return false;
        }

        // Calculate the mirrored zone index for the caster (e.g., index 4 maps to index 0 in a 5-zone setup)
        int mirroredIndex = Match.NUMBER_OF_ZONES - 1 - targetIndex;

        // Valid only if the mirrored front zone on the caster's side is empty
        return caster.getFrontZones()[mirroredIndex] == null;
    }

    @Override
    public void apply(Match match, Player caster, Unit target) {
        if (!canTarget(match, caster, target)) {
            return;
        }

        Player opponent = match.getOpponent(caster);
        Unit[] opponentZones = opponent.getFrontZones();
        int targetIndex = -1;
        for (int i = 0; i < opponentZones.length; i++) {
            if (opponentZones[i] == target) {
                targetIndex = i;
                break;
            }
        }

        int mirroredIndex = Match.NUMBER_OF_ZONES - 1 - targetIndex;

        // 1. Perform Shallow Copy via Prototype Pattern
        Unit shallowIllusion = target.cloneShallow();

        // 2. Place the illusion directly into the mirrored zone on the caster's side
        match.placeUnitForFree(caster, mirroredIndex, shallowIllusion);
    }
    public static Card create() {
        return new PhantomMirrorSpell();
    }
}