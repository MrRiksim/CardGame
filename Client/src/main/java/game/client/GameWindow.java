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
    private final BoardPanel boardPanel;

    private GameClient client;
    private boolean gameOver = false;

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
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    public void setWaiting() {
        SwingUtilities.invokeLater(() -> {
            gameOver = false;
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
            boolean yourTurn) {

        SwingUtilities.invokeLater(() -> {
            gameOver = false;
            matchLabel.setText("Match " + matchId);
            statsLabel.setText(
                    "You: " + ownHealth + " HP, " + ownEnergy + " energy    |    Opponent: "
                            + opponentHealth + " HP");

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
            statusLabel.setText("Your health reached zero.");
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
        if (client == null || gameOver) {
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
