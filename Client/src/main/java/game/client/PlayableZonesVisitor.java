package game.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>Design pattern - Visitor (Concrete Visitor 2):</b> computes which board
 * zones should be highlighted as valid drop targets when a card in hand is
 * selected.
 */
public class PlayableZonesVisitor implements ClientCardVisitor {

    private final List<ClientUnit> ownFront;
    private final List<ClientUnit> opponentFront;
    private final List<ClientTrap> ownBack;
    private final Set<ZoneRef> zones = new HashSet<>();

    public PlayableZonesVisitor(
            List<ClientUnit> ownFront,
            List<ClientUnit> opponentFront,
            List<ClientTrap> ownBack) {
        this.ownFront = ownFront;
        this.opponentFront = opponentFront;
        this.ownBack = ownBack;
    }

    @Override
    public void visitUnit(ClientUnit unit) {
        for (int i = 0; i < 5; i++) {
            zones.add(new ZoneRef(ZoneRef.Row.OWN_FRONT, i));
        }
    }

    @Override
    public void visitSpell(ClientSpell spell) {
        if (spell.name().equals("Healing Wave") || spell.name().equals("Raise the Fallen")) {
            // Global effects need no unit target. Use the player's utility zones to cast.
            zones.add(new ZoneRef(ZoneRef.Row.OWN_UTILITY, 0));
            zones.add(new ZoneRef(ZoneRef.Row.OWN_UTILITY, 1));
            return;
        }
        switch (spell.spellType()) {
            case DAMAGE -> {
                for (ClientUnit enemy : opponentFront) {
                    zones.add(new ZoneRef(ZoneRef.Row.OPPONENT_FRONT, enemy.zoneIndex()));
                }
            }
            case BUFF -> {
                for (ClientUnit ally : ownFront) {
                    zones.add(new ZoneRef(ZoneRef.Row.OWN_FRONT, ally.zoneIndex()));
                }
            }
            case SPECIAL -> {
                for (int i = 0; i < 5; i++) {
                    if (!isUnitAt(ownFront, i)) {
                        zones.add(new ZoneRef(ZoneRef.Row.OWN_FRONT, i));
                    }
                    if (!isTrapAt(ownBack, i)) {
                        zones.add(new ZoneRef(ZoneRef.Row.OWN_BACK, i));
                    }
                }
                zones.add(new ZoneRef(ZoneRef.Row.OWN_UTILITY, 0));
                zones.add(new ZoneRef(ZoneRef.Row.OWN_UTILITY, 1));
                zones.add(new ZoneRef(ZoneRef.Row.OPPONENT_UTILITY, 0));
                zones.add(new ZoneRef(ZoneRef.Row.OPPONENT_UTILITY, 1));
            }
        }
    }

    @Override
    public void visitTrap(ClientTrap trap) {
        for (int i = 0; i < 5; i++) {
            if (!isTrapAt(ownBack, i)) {
                zones.add(new ZoneRef(ZoneRef.Row.OWN_BACK, i));
            }
        }
    }

    public Set<ZoneRef> getZones() {
        return zones;
    }

    private boolean isUnitAt(List<ClientUnit> units, int zone) {
        for (ClientUnit u : units) {
            if (u.zoneIndex() == zone) {
                return true;
            }
        }
        return false;
    }

    private boolean isTrapAt(List<ClientTrap> traps, int zone) {
        for (ClientTrap t : traps) {
            if (t.zoneIndex() == zone) {
                return true;
            }
        }
        return false;
    }
}
