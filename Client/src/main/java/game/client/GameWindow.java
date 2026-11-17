package game.client;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GameWindow {

    private final JFrame frame;
    private final StatusBar statusBar;
    private final JButton endTurnButton;
    private final JButton undoSpellButton;
    private final JPanel modeSelectPanel;
    private final BoardPanel boardPanel;
    private final CardInfoPanel cardInfoPanel;
    private final ChatPanel chatPanel;
    private final JPanel sidePanel;
    private final UiMediator uiMediator;

    private GameClient client;
    private boolean gameOver = false;
    private boolean inMatch = false;

    public GameWindow() {
        frame = new JFrame("Card Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1080, 1040);
        frame.setLocationRelativeTo(null);

        statusBar = new StatusBar();

        modeSelectPanel = buildModeSelectPanel();

        boardPanel = new BoardPanel(this::sendPlayAction);
        boardPanel.setVisible(false);

        cardInfoPanel = new CardInfoPanel();
        cardInfoPanel.setVisible(false);

        chatPanel = new ChatPanel(this::sendChat);
        sidePanel = new JPanel(new BorderLayout(0, 8));
        sidePanel.setPreferredSize(new Dimension(300, 0));
        sidePanel.add(cardInfoPanel, BorderLayout.CENTER);
        sidePanel.add(chatPanel, BorderLayout.SOUTH);
        sidePanel.setVisible(false);

        uiMediator = new GameUiMediator(boardPanel, cardInfoPanel, statusBar);

        endTurnButton = new JButton("End Turn");
        endTurnButton.setEnabled(false);
        endTurnButton.addActionListener(e -> {
            if (client != null && !gameOver) {
                uiMediator.notify(boardPanel, UiEvent.TURN_ENDED, null);
                client.endTurn();
            }
        });

        undoSpellButton = new JButton("Undo Spell");
        undoSpellButton.setEnabled(false);
        undoSpellButton.addActionListener(e -> {
            if (client != null && !gameOver) {
                client.undoSpell();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(undoSpellButton);
        bottomPanel.add(endTurnButton);

        frame.setLayout(new BorderLayout());
        frame.add(statusBar, BorderLayout.NORTH);
        frame.add(modeSelectPanel, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JPanel buildModeSelectPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 12, 12));
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
                "Health, energy, hand size - and a 5 to 15 minute clock - are all randomized.",
                GameMode.CHAOS));

        JButton spectateButton = new JButton(
                "<html><div style='text-align:center'><b style='font-size:14px'>Spectate a Match</b>"
                        + "<br>Watch a match that's already being played. Hands and traps stay hidden.</div></html>");
        spectateButton.addActionListener(e -> {
            if (client != null) {
                statusBar.setStatusText("Looking for matches...");
                client.requestMatchList();
            }
        });
        panel.add(spectateButton);

        return panel;
    }

    /* The server's answer to the Spectate button. Picking one swaps the menu for the board. */
    public void showMatchList(List<MatchListing> matches) {
        SwingUtilities.invokeLater(() -> {
            if (matches.isEmpty()) {
                statusBar.setStatusText("Choose a match type to begin.");
                JOptionPane.showMessageDialog(frame, "No matches are being played right now.",
                        "Spectate", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JComboBox<MatchListing> selector = new JComboBox<>(matches.toArray(new MatchListing[0]));
            int choice = JOptionPane.showConfirmDialog(frame, new Object[]{"Choose a match to watch:", selector},
                    "Spectate", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            MatchListing picked = (MatchListing) selector.getSelectedItem();
            if (choice != JOptionPane.OK_OPTION || picked == null) {
                statusBar.setStatusText("Choose a match type to begin.");
                return;
            }

            frame.remove(modeSelectPanel);
            frame.add(boardPanel, BorderLayout.CENTER);
            statusBar.setStatusText("Joining Match " + picked.matchId() + "...");
            client.spectate(picked.matchId());
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showSpectating(int matchId) {
        SwingUtilities.invokeLater(() -> {
            sidePanel.setVisible(true);
            chatPanel.reset("Chat is available to the two players in the match.");
            chatPanel.setChatEnabled(false);
            gameOver = false;
            inMatch = false;
            statusBar.setMatchText("Spectating Match " + matchId);
            statusBar.setTurnText("Waiting for game state...");
            statusBar.setStatusText("You are spectating - click any visible card to inspect it.");
            endTurnButton.setEnabled(false);
            undoSpellButton.setEnabled(false);
            boardPanel.resetGame();
            boardPanel.setVisible(true);
            cardInfoPanel.clear();
            cardInfoPanel.setVisible(true);
            frame.revalidate();
            frame.repaint();
        });
    }

    /* The match ended (or never existed) before the server could add us - back to the menu. */
    public void showSpectateRejected() {
        SwingUtilities.invokeLater(() -> {
            sidePanel.setVisible(false);
            chatPanel.setChatEnabled(false);
            frame.remove(boardPanel);
            frame.add(modeSelectPanel, BorderLayout.CENTER);
            cardInfoPanel.setVisible(false);
            statusBar.setMatchText("");
            statusBar.setTurnText("");
            statusBar.setStatsText("");
            statusBar.setStatusText("Choose a match type to begin.");
            JOptionPane.showMessageDialog(frame, "That match can't be watched anymore.",
                    "Spectate", JOptionPane.INFORMATION_MESSAGE);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void updateSpectator(SpectatorState state) {
        SwingUtilities.invokeLater(() -> {
            if (gameOver) {
                return;
            }
            statusBar.setMatchText("Spectating Match " + state.matchId());
            statusBar.setTurnText("PLAYER " + state.turnPlayer() + "'S TURN");
            statusBar.setStatsText("Player 1: " + state.player1Health() + " HP" + formatClock(state.player1SecondsLeft())
                    + "    |    Player 2: " + state.player2Health() + " HP" + formatClock(state.player2SecondsLeft()));
            boardPanel.setVisible(true);
            cardInfoPanel.setVisible(true);
            boardPanel.updateSpectatorState(state);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showMatchOver(int winnerNumber) {
        SwingUtilities.invokeLater(() -> {
            chatPanel.setChatEnabled(false);
            gameOver = true;
            boardPanel.setGameOver();
            uiMediator.notify(boardPanel, UiEvent.SELECTION_CLEARED, null);
            statusBar.setTurnText("PLAYER " + winnerNumber + " WINS");
            statusBar.setStatusText("The match is over.");
            frame.revalidate();
            frame.repaint();
        });
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
        statusBar.setStatusText("Waiting for opponent...");

        frame.revalidate();
        frame.repaint();
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    public void showModeSelect() {
        SwingUtilities.invokeLater(() -> statusBar.setStatusText("Choose a match type to begin."));
    }

    public void setWaiting() {
        SwingUtilities.invokeLater(() -> {
            sidePanel.setVisible(false);
            chatPanel.reset("Waiting for a match.");
            chatPanel.setChatEnabled(false);
            gameOver = false;
            inMatch = false;
            statusBar.setMatchText("");
            statusBar.setTurnText("");
            statusBar.setStatsText("");
            statusBar.setStatusText("Waiting for opponent");
            endTurnButton.setEnabled(false);
            undoSpellButton.setEnabled(false);
            boardPanel.resetGame();
            boardPanel.setVisible(false);
            cardInfoPanel.clear();
            cardInfoPanel.setVisible(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showMatch(int matchId) {
        SwingUtilities.invokeLater(() -> {
            sidePanel.setVisible(true);
            chatPanel.reset("Press Enter to chat. Use /give <card name> to get a card, or /cards to list names.");
            chatPanel.setChatEnabled(true);
            gameOver = false;
            inMatch = true;
            statusBar.setMatchText("Match " + matchId);
            statusBar.setStatusText("Match started");
            statusBar.setTurnText("Waiting for game state...");
            boardPanel.resetGame();
            boardPanel.setVisible(true);
            cardInfoPanel.clear();
            cardInfoPanel.setVisible(true);
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
            ClientCard ownGraveyardTop,
            ClientCard opponentGraveyardTop,
            boolean yourTurn,
            Map<Integer, Integer> archerTargets) {

        SwingUtilities.invokeLater(() -> {
            gameOver = false;
            inMatch = true;
            statusBar.setMatchText("Match " + matchId);
            statusBar.setStatsText(buildStatsText(
                    ownHealth, ownEnergy, opponentHealth, ownSecondsLeft, opponentSecondsLeft));

            if (yourTurn) {
                statusBar.setTurnText("YOUR TURN");
                statusBar.setStatusText("Click a card to inspect, or drag to play");
                endTurnButton.setEnabled(true);
                undoSpellButton.setEnabled(true);
            } else {
                statusBar.setTurnText("OPPONENT'S TURN");
                statusBar.setStatusText("Waiting for opponent (click any card to inspect)...");
                endTurnButton.setEnabled(false);
                undoSpellButton.setEnabled(false);
            }

            boardPanel.setVisible(true);
            cardInfoPanel.setVisible(true);
            boardPanel.updateState(
                    ownHand, opponentHandCount, ownFront, opponentFront,
                    ownBack, opponentBackZones, ownGraveyardTop, opponentGraveyardTop,
                    yourTurn, archerTargets);

            frame.revalidate();
            frame.repaint();
        });
    }

    private String buildStatsText(
            int ownHealth, int ownEnergy, int opponentHealth,
            Integer ownSecondsLeft, Integer opponentSecondsLeft) {

        return "You: " + ownHealth + " HP, " + ownEnergy + " energy"
                + formatClock(ownSecondsLeft)
                + "    |    Opponent: " + opponentHealth + " HP" + formatClock(opponentSecondsLeft);
    }

    private String formatClock(Integer secondsLeft) {
        if (secondsLeft == null) {
            return "";
        }
        return String.format(", %d:%02d left", secondsLeft / 60, secondsLeft % 60);
    }

    public void showWin() {
        SwingUtilities.invokeLater(() -> {
            chatPanel.setChatEnabled(false);
            chatPanel.addMessage("System", "Match ended.");
            gameOver = true;
            boardPanel.setGameOver();
            boardPanel.setVisible(true);
            uiMediator.notify(boardPanel, UiEvent.SELECTION_CLEARED, null);
            statusBar.setTurnText("YOU WIN");
            statusBar.setStatusText("Your opponent has been defeated.");
            endTurnButton.setEnabled(false);
            undoSpellButton.setEnabled(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showLose() {
        SwingUtilities.invokeLater(() -> {
            chatPanel.setChatEnabled(false);
            chatPanel.addMessage("System", "Match ended.");
            gameOver = true;
            boardPanel.setGameOver();
            boardPanel.setVisible(true);
            uiMediator.notify(boardPanel, UiEvent.SELECTION_CLEARED, null);
            statusBar.setTurnText("YOU LOSE");
            statusBar.setStatusText("Your health - or your clock - reached zero.");
            endTurnButton.setEnabled(false);
            undoSpellButton.setEnabled(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showCannotConnect() {
        SwingUtilities.invokeLater(() -> {
            sidePanel.setVisible(false);
            chatPanel.setChatEnabled(false);
            gameOver = true;
            statusBar.setMatchText("");
            statusBar.setTurnText("");
            statusBar.setStatsText("");
            statusBar.setStatusText("Can't connect to server");
            endTurnButton.setEnabled(false);
            undoSpellButton.setEnabled(false);
            boardPanel.setVisible(false);
            cardInfoPanel.setVisible(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    public void showDisconnected() {
        SwingUtilities.invokeLater(() -> {
            chatPanel.setChatEnabled(false);
            chatPanel.addMessage("System", "Disconnected from server.");
            gameOver = true;
            statusBar.setMatchText("");
            statusBar.setTurnText("");
            statusBar.setStatsText("");
            statusBar.setStatusText("Disconnected from server");
            endTurnButton.setEnabled(false);
            undoSpellButton.setEnabled(false);
            boardPanel.setVisible(false);
            cardInfoPanel.setVisible(false);
            frame.revalidate();
            frame.repaint();
        });
    }

    private boolean sendChat(String text) {
        if (client != null && inMatch && !gameOver && client.sendChat(text)) {
            return true;
        }
        chatPanel.addMessage("System", "Message not sent. Chat requires an active connection and match.");
        return false;
    }

    public void showChatMessage(String author, String text) {
        SwingUtilities.invokeLater(() -> chatPanel.addMessage(author, text));
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
        } else if (action instanceof PlayAction.RiderAttach attach) {
            client.attachRider(attach.cardId(), attach.targetUnitId());
        } else if (action instanceof PlayAction.ArcherTarget target) {
            client.chooseArcherTarget(target.archerId(), target.targetSlot());
        } else if (action instanceof PlayAction.UndoArcherTarget) {
            client.undoArcherTarget();
        } else if (action instanceof PlayAction.UndoSpell) {
            client.undoSpell();
        }
    }

    public void showArcherCommandStatus(String text) {
        SwingUtilities.invokeLater(() -> {
            if (inMatch && !gameOver) {
                statusBar.setStatusText(text);
            }
        });
    }
}
