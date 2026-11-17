package game.client;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Design pattern - Visitor (Concrete Visitor 1):</b> extracts human-readable
 * attribute rows for a card to display in {@link CardInfoPanel} without using
 * {@code instanceof} checks.
 */
public class CardInfoVisitor implements ClientCardVisitor {

    private final List<String[]> rows = new ArrayList<>();
    private String title = "";
    private String description = "";
    private String hint = "";

    @Override
    public void visitUnit(ClientUnit unit) {
        title = unit.name() + " (Unit)";
        description = unit.description();
        rows.add(new String[]{"Type", "Unit"});
        rows.add(new String[]{"Name", unit.name()});
        rows.add(new String[]{"Energy Cost", String.valueOf(unit.energyCost())});
        rows.add(new String[]{"Health", unit.health() + " / " + unit.maxHealth()});
        rows.add(new String[]{"Damage", String.valueOf(unit.damage())});
        rows.add(new String[]{"Element", String.valueOf(unit.element())});
        rows.add(new String[]{"Clan", unit.hasClan() ? unit.clan() : "None"});
        rows.add(new String[]{"Health Buffed", unit.healthBuffed() ? "Yes" : "No"});
        rows.add(new String[]{"Damage Buffed", unit.damageBuffed() ? "Yes" : "No"});
        rows.add(new String[]{"Shields", String.valueOf(unit.shieldCount())});
        rows.add(new String[]{"Riders", String.valueOf(unit.riderCount())});
        hint = "Selected Unit: " + unit.name() + " — drag onto a highlighted front zone (or friendly unit to ride).";
    }

    @Override
    public void visitSpell(ClientSpell spell) {
        title = spell.name() + " (Spell)";
        description = spell.description();
        rows.add(new String[]{"Type", "Spell"});
        rows.add(new String[]{"Name", spell.name()});
        rows.add(new String[]{"Energy Cost", String.valueOf(spell.energyCost())});
        rows.add(new String[]{"Spell Type", String.valueOf(spell.spellType())});
        String validTargets = switch (spell.spellType()) {
            case DAMAGE -> "Enemy Front Units";
            case BUFF -> "Friendly Front Units";
            case SPECIAL -> "Any Empty Zone / Graveyard / Deck";
        };
        if (spell.name().equals("Healing Wave") || spell.name().equals("Raise the Fallen")) {
            validTargets = "Your Graveyard / Deck (no unit target needed)";
        }
        rows.add(new String[]{"Target", validTargets});
        hint = "Selected Spell: " + spell.name() + " — drag onto a highlighted target (" + validTargets + ").";
    }

    @Override
    public void visitTrap(ClientTrap trap) {
        title = trap.name() + " (Trap)";
        description = trap.description();
        rows.add(new String[]{"Type", "Trap"});
        rows.add(new String[]{"Name", trap.name()});
        rows.add(new String[]{"Energy Cost", String.valueOf(trap.energyCost())});
        rows.add(new String[]{"Activation", trap.activationCondition()});
        hint = "Selected Trap: " + trap.name() + " — drag onto a highlighted back zone.";
    }

    public List<String[]> getRows() {
        return rows;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public String getHint() {
        return hint;
    }
}
