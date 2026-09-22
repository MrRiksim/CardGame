package game.client;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameWindow {

    private final JFrame frame;
    private final JLabel matchLabel;
    private final JLabel turnLabel;
    private final JLabel statusLabel;
    private final JButton endTurnButton;
    private final BoardPanel boardPanel;

    private GameClient client;
    private boolean gameOver = false;

    public GameWindow() {
        frame = new JFrame("Card Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(820, 1000);
        frame.setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        matchLabel = new JLabel("", SwingConstants.CENTER);
        turnLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel = new JLabel("Connecting to server...", SwingConstants.CENTER);

        Font font = new Font("Arial", Font.BOLD, 18);
        matchLabel.setFont(font);
        turnLabel.setFont(font);
        statusLabel.setFont(font);

        topPanel.add(matchLabel);
        topPanel.add(turnLabel);
        topPanel.add(statusLabel);

        boardPanel = new BoardPanel(this::sendMove);
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
            List<ClientCard> ownHand,
            int opponentHandCount,
            List<ClientCard> ownPlayed,
            List<ClientCard> opponentPlayed,
            boolean yourTurn) {

        SwingUtilities.invokeLater(() -> {
            gameOver = false;
            matchLabel.setText("Match " + matchId);
            boardPanel.setVisible(true);
            boardPanel.updateState(ownHand, opponentHandCount, ownPlayed, opponentPlayed, yourTurn);

            if (yourTurn) {
                turnLabel.setText("YOUR TURN");
                statusLabel.setText("Play or move your cards");
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
            /*
             * Keep displaying the field.
             */
            boardPanel.setGameOver();
            boardPanel.setVisible(true);
            turnLabel.setText("YOU WIN");
            statusLabel.setText("Your opponent disconnected.");
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
            statusLabel.setText("Disconnected from server");
            endTurnButton.setEnabled(false);
            boardPanel.setVisible(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    private void sendMove(BoardPanel.CardMove move) {
        if (client == null || gameOver) {
            return;
        }
        client.moveCard(move);
    }
}
