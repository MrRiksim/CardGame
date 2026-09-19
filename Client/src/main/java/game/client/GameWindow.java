package game.client;

import javax.swing.*;
import java.awt.*;

public class GameWindow {

    private final JFrame frame;

    private final JLabel matchLabel;
    private final JLabel turnLabel;
    private final JLabel statusLabel;

    private final JButton endTurnButton;

    private final BoardPanel boardPanel;

    private GameClient client;

    public GameWindow() {

        frame = new JFrame("Card Game");

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setSize(
                700,
                520
        );

        frame.setLocationRelativeTo(null);

        /*
         * Top information area.
         */
        JPanel topPanel =
                new JPanel(new GridLayout(3, 1));

        matchLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        turnLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        statusLabel =
                new JLabel(
                        "Connecting to server...",
                        SwingConstants.CENTER
                );

        Font labelFont =
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                );

        matchLabel.setFont(labelFont);
        turnLabel.setFont(labelFont);
        statusLabel.setFont(labelFont);

        topPanel.add(matchLabel);
        topPanel.add(turnLabel);
        topPanel.add(statusLabel);

        /*
         * Board.
         */
        boardPanel =
                new BoardPanel(this::sendMove);

        /*
         * The board MUST NOT be visible until a match exists.
         */
        boardPanel.setVisible(false);

        /*
         * End turn button.
         */
        endTurnButton =
                new JButton("End Turn");

        endTurnButton.setEnabled(false);

        endTurnButton.addActionListener(e -> {

            if (client != null) {
                client.endTurn();
            }
        });

        JPanel bottomPanel =
                new JPanel();

        bottomPanel.add(endTurnButton);

        frame.setLayout(
                new BorderLayout()
        );

        frame.add(
                topPanel,
                BorderLayout.NORTH
        );

        frame.add(
                boardPanel,
                BorderLayout.CENTER
        );

        frame.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        /*
         * Start in the waiting/connection state.
         */
        setInitialState();

        frame.setVisible(true);
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    private void setInitialState() {

        matchLabel.setText("");
        turnLabel.setText("");

        statusLabel.setText(
                "Connecting to server..."
        );

        endTurnButton.setEnabled(false);

        boardPanel.setVisible(false);
    }

    public void setWaiting() {

        SwingUtilities.invokeLater(() -> {

            matchLabel.setText("");

            turnLabel.setText("");

            statusLabel.setText(
                    "Waiting for opponent"
            );

            endTurnButton.setEnabled(false);

            /*
             * No match = no board.
             */
            boardPanel.setVisible(false);

            frame.revalidate();
            frame.repaint();
        });
    }

    public void showMatch(int matchId) {

        SwingUtilities.invokeLater(() -> {

            matchLabel.setText(
                    "Match " + matchId
            );

            statusLabel.setText(
                    "Match started"
            );

            /*
             * A match now exists, so show the board.
             */
            boardPanel.setVisible(true);

            boardPanel.resetGame();

            frame.revalidate();
            frame.repaint();
        });
    }

    public void updateGame(
            int myX,
            int myY,
            int enemyX,
            int enemyY,
            boolean yourTurn) {

        SwingUtilities.invokeLater(() -> {

            /*
             * The server has confirmed that we are
             * actually in a match.
             */
            boardPanel.setVisible(true);

            boardPanel.updateState(
                    myX,
                    myY,
                    enemyX,
                    enemyY,
                    yourTurn
            );

            if (yourTurn) {

                turnLabel.setText(
                        "YOUR TURN"
                );

                statusLabel.setText(
                        "Move your green card or end your turn"
                );

                endTurnButton.setEnabled(true);

            } else {

                turnLabel.setText(
                        "OPPONENT'S TURN"
                );

                statusLabel.setText(
                        "Waiting for opponent..."
                );

                endTurnButton.setEnabled(false);
            }

            frame.revalidate();
            frame.repaint();
        });
    }

    public void showWin() {

        SwingUtilities.invokeLater(() -> {

            /*
             * IMPORTANT:
             * Do NOT hide the board.
             */
            boardPanel.setGameOver();
            boardPanel.setVisible(true);

            turnLabel.setText(
                    "YOU WIN!"
            );

            statusLabel.setText(
                    "Your opponent disconnected."
            );

            endTurnButton.setEnabled(false);

            frame.revalidate();
            frame.repaint();
        });
    }

    public void showCannotConnect() {

        SwingUtilities.invokeLater(() -> {

            matchLabel.setText("");

            turnLabel.setText("");

            statusLabel.setText(
                    "Can't connect to server"
            );

            endTurnButton.setEnabled(false);

            /*
             * There is no match, therefore no board.
             */
            boardPanel.setVisible(false);

            frame.revalidate();
            frame.repaint();
        });
    }

    public void showDisconnected() {

        SwingUtilities.invokeLater(() -> {

            matchLabel.setText("");

            turnLabel.setText("");

            statusLabel.setText(
                    "Disconnected from server"
            );

            endTurnButton.setEnabled(false);

            /*
             * The server connection itself was lost,
             * so there is no playable board anymore.
             */
            boardPanel.setVisible(false);

            frame.revalidate();
            frame.repaint();
        });
    }

    private void sendMove(Point point) {

        if (client != null) {

            client.sendMove(
                    point.x,
                    point.y
            );
        }
    }
}