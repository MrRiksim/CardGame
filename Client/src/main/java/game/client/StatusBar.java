package game.client;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * <b>Design pattern - Mediator (Concrete Colleague 2):</b> displays match,
 * turn, player stats, and contextual hints coordinated by {@link UiMediator}.
 */
public class StatusBar extends UiColleague {

    private final JLabel matchLabel;
    private final JLabel turnLabel;
    private final JLabel statsLabel;
    private final JLabel statusLabel;
    private String defaultStatusText = "Connecting to server...";

    public StatusBar() {
        super(new GridLayout(4, 1));
        matchLabel = new JLabel("", SwingConstants.CENTER);
        turnLabel = new JLabel("", SwingConstants.CENTER);
        statsLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel = new JLabel(defaultStatusText, SwingConstants.CENTER);

        Font font = new Font("Arial", Font.BOLD, 16);
        matchLabel.setFont(font);
        turnLabel.setFont(font);
        statsLabel.setFont(font);
        statusLabel.setFont(font);

        add(matchLabel);
        add(turnLabel);
        add(statsLabel);
        add(statusLabel);
    }

    public void setMatchText(String text) {
        matchLabel.setText(text);
    }

    public void setTurnText(String text) {
        turnLabel.setText(text);
    }

    public void setStatsText(String text) {
        statsLabel.setText(text);
    }

    public void setStatusText(String text) {
        defaultStatusText = text;
        statusLabel.setText(text);
    }

    public void setHint(String hint) {
        if (hint == null || hint.isEmpty()) {
            statusLabel.setText(defaultStatusText);
        } else {
            statusLabel.setText(hint);
        }
    }
}
