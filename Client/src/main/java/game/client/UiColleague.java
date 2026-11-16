package game.client;

import javax.swing.JPanel;
import java.awt.LayoutManager;

/**
 * <b>Design pattern - Mediator (Abstract Colleague):</b> base class for Swing
 * UI panels that coordinate through a {@link UiMediator}.
 */
public abstract class UiColleague extends JPanel {

    protected UiMediator mediator;

    protected UiColleague() {
        super();
    }

    protected UiColleague(LayoutManager layout) {
        super(layout);
    }

    public void setMediator(UiMediator mediator) {
        this.mediator = mediator;
    }
}
