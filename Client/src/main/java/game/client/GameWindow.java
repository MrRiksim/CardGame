package game.client;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameWindow {

    private final JFrame frame;
    private final JLabel matchLabel;
    private final JLabel turnLabel;
    private final JLabel statsLabel;
    private final JLabel statusLabel;
    private final JButton endTurnButton;
    private final JPanel modeSelectPanel;
    private final BoardPanel boardPanel;

    private GameClient client;
    private boolean gameOver = false;
    private boolean inMatch = false;

    public GameWindow() {
        frame = new JFrame("Card Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(820, 1040);
        frame.setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new GridLayout(4, 1));
        matchLabel = new JLabel("", SwingConstants.CENTER);
        turnLabel = new JLabel("", SwingConstants.CENTER);
        statsLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel = new JLabel("Connecting to server...", SwingConstants.CENTER);

        Font font = new Font("Arial", Font.BOLD, 16);
        matchLabel.setFont(font);
        turnLabel.setFont(font);
        statsLabel.setFont(font);
        statusLabel.setFont(font);

        topPanel.add(matchLabel);
        topPanel.add(turnLabel);
        topPanel.add(statsLabel);
        topPanel.add(statusLabel);

        modeSelectPanel = buildModeSelectPanel();

        boardPanel = new BoardPanel(this::sendPlayAction);
        boardPanel.setVisible(false);

        endTurnButton = new JButton("End Turn");
        endTurnButton.setEnabled(false);
        endTurnButton.addActionListener(e -> {
            if (client != null && !gameOver) {
                client.endTurn();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(endTurnButton);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(modeSelectPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JPanel buildModeSelectPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        panel.add(createModeButton(
                "Standard Match",
                "25 health, 6 starting energy, +2 energy and a card each turn.",
                GameMode.STANDARD));
        panel.add(createModeButton(
                "Speed Match",
                "Standard rules, but each player has 10 minutes total - run out and you lose.",
                GameMode.SPEED));
        panel.add(createModeButton(
                "Chaos Match",
                "Health, energy, hand size - everything is randomized. Might even be timed.",
                GameMode.CHAOS));

        return panel;
    }

    private JButton createModeButton(String title, String description, GameMode mode) {
        JButton button = new JButton(
                "<html><div style='text-align:center'><b style='font-size:14px'>" + title
                        + "</b><br>" + description + "</div></html>");
        button.addActionListener(e -> chooseMode(mode));
        return button;
    }

    private void chooseMode(GameMode mode) {
        if (client == null) {
            return;
        }

        frame.remove(modeSelectPanel);
        frame.add(boardPanel, BorderLayout.CENTER);

        client.joinQueue(mode);
        statusLabel.setText("Waiting for opponent...");

        frame.revalidate();
        frame.repaint();
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    /*
     * Shown once the socket actually connects - the player picks a mode
     * from modeSelectPanel (already on screen since construction) before
     * anything gets queued server-side.
     */
    public void showModeSelect() {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Choose a match type to begin."));
    }

    public void setWaiting() {
        SwingUtilities.invokeLater(() -> {
            gameOver = false;
            inMatch = false;
            matchLabel.setText("");
            turnLabel.setText("");
            statsLabel.setText("");
            statusLabel.setText("Waiting for opponent");
            endTurnButton.setEnabled(false);
            boardPanel.resetGame();
            boardPanel.setVisible(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showMatch(int matchId) {
        SwingUtilities.invokeLater(() -> {
            gameOver = false;
            inMatch = true;
            matchLabel.setText("Match " + matchId);
            statusLabel.setText("Match started");
            turnLabel.setText("Waiting for game state...");
            boardPanel.resetGame();
            boardPanel.setVisible(true);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void updateGame(
            int matchId,
            int ownHealth,
            int opponentHealth,
            int ownEnergy,
            List<ClientCard> ownHand,
            int opponentHandCount,
            List<ClientUnit> ownFront,
            List<ClientUnit> opponentFront,
            List<ClientTrap> ownBack,
            List<Integer> opponentBackZones,
            Integer ownSecondsLeft,
            Integer opponentSecondsLeft,
            boolean yourTurn) {

        SwingUtilities.invokeLater(() -> {
            gameOver = false;
            inMatch = true;
            matchLabel.setText("Match " + matchId);
            statsLabel.setText(buildStatsText(
                    ownHealth, ownEnergy, opponentHealth, ownSecondsLeft, opponentSecondsLeft));

            boardPanel.setVisible(true);
            boardPanel.updateState(
                    ownHand, opponentHandCount, ownFront, opponentFront,
                    ownBack, opponentBackZones, yourTurn);

            if (yourTurn) {
                turnLabel.setText("YOUR TURN");
                statusLabel.setText("Play cards, then end your turn");
                endTurnButton.setEnabled(true);
            } else {
                turnLabel.setText("OPPONENT'S TURN");
                statusLabel.setText("Waiting for opponent...");
                endTurnButton.setEnabled(false);
            }

            frame.revalidate();
            frame.repaint();
        });
    }

    private String buildStatsText(
            int ownHealth, int ownEnergy, int opponentHealth,
            Integer ownSecondsLeft, Integer opponentSecondsLeft) {

        String text = "You: " + ownHealth + " HP, " + ownEnergy + " energy"
                + formatClock(ownSecondsLeft)
                + "    |    Opponent: " + opponentHealth + " HP" + formatClock(opponentSecondsLeft);
        return text;
    }

    /*
     * Empty string for an untimed match - ownSecondsLeft/opponentSecondsLeft
     * are only ever non-null when this match actually has a clock.
     */
    private String formatClock(Integer secondsLeft) {
        if (secondsLeft == null) {
            return "";
        }
        return String.format(", %d:%02d left", secondsLeft / 60, secondsLeft % 60);
    }

    public void showWin() {
        SwingUtilities.invokeLater(() -> {
            gameOver = true;
            boardPanel.setGameOver();
            boardPanel.setVisible(true);
            turnLabel.setText("YOU WIN");
            statusLabel.setText("Your opponent has been defeated.");
            endTurnButton.setEnabled(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showLose() {
        SwingUtilities.invokeLater(() -> {
            gameOver = true;
            boardPanel.setGameOver();
            boardPanel.setVisible(true);
            turnLabel.setText("YOU LOSE");
            statusLabel.setText("Your health - or your clock - reached zero.");
            endTurnButton.setEnabled(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showCannotConnect() {
        SwingUtilities.invokeLater(() -> {
            gameOver = true;
            matchLabel.setText("");
            turnLabel.setText("");
            statsLabel.setText("");
            statusLabel.setText("Can't connect to server");
            endTurnButton.setEnabled(false);
            boardPanel.setVisible(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showDisconnected() {
        SwingUtilities.invokeLater(() -> {
            gameOver = true;
            matchLabel.setText("");
            turnLabel.setText("");
            statsLabel.setText("");
            statusLabel.setText("Disconnected from server");
            endTurnButton.setEnabled(false);
            boardPanel.setVisible(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    private void sendPlayAction(PlayAction action) {
        if (client == null || gameOver || !inMatch) {
            return;
        }

        if (action instanceof PlayAction.UnitPlacement placement) {
            client.playUnit(placement.cardId(), placement.zoneIndex());
        } else if (action instanceof PlayAction.TrapPlacement placement) {
            client.playTrap(placement.cardId(), placement.zoneIndex());
        } else if (action instanceof PlayAction.SpellCast cast) {
            client.playSpell(cast.cardId(), cast.targetUnitId());
        }
    }
}
