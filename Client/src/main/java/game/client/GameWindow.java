package game.client;

import javax.swing.*;
import java.awt.*;

public class GameWindow {

    private final JFrame frame;
    private final JLabel statusLabel;

    public GameWindow() {

        frame = new JFrame("Card Game");

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setSize(400, 200);

        frame.setLocationRelativeTo(null);

        statusLabel = new JLabel(
                "Connecting to server...",
                SwingConstants.CENTER
        );

        statusLabel.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        24
                )
        );

        frame.add(statusLabel);

        frame.setVisible(true);
    }

    public void setStatus(String status) {

        SwingUtilities.invokeLater(() ->
                statusLabel.setText(status)
        );
    }
}