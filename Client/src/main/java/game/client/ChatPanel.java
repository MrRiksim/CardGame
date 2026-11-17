package game.client;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.function.Predicate;

/** Match chat UI. Gameplay selection and undo remain independent of chat messages. */
public final class ChatPanel extends JPanel {
    private static final int MAX_MESSAGE_LENGTH = 300;
    private static final int MAX_MESSAGES = 200;
    private final JTextArea transcript = new JTextArea();
    private final JTextField input = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private final Predicate<String> sender;

    public ChatPanel(Predicate<String> sender) {
        super(new BorderLayout(6, 6));
        this.sender = sender;
        setPreferredSize(new Dimension(300, 270));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Match chat"),
                BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        transcript.setEditable(false);
        transcript.setLineWrap(true);
        transcript.setWrapStyleWord(true);
        ((DefaultCaret) transcript.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        add(new JScrollPane(transcript), BorderLayout.CENTER);

        input.setToolTipText("Message your opponent, or use /give <card name>, /cards, /help (up to 300 characters)");
        input.getAccessibleContext().setAccessibleName("Chat message");
        ((AbstractDocument) input.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributes)
                    throws BadLocationException {
                replace(fb, offset, 0, text, attributes);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributes)
                    throws BadLocationException {
                String singleLine = text == null ? "" : text.replaceAll("\\p{Cntrl}", " ");
                int available = MAX_MESSAGE_LENGTH - (fb.getDocument().getLength() - length);
                if (singleLine.length() > available) {
                    singleLine = singleLine.substring(0, Math.max(0, available));
                }
                super.replace(fb, offset, length, singleLine, attributes);
            }
        });
        input.addActionListener(event -> sendMessage());
        sendButton.addActionListener(event -> sendMessage());
        JPanel composer = new JPanel(new BorderLayout(6, 0));
        composer.add(input, BorderLayout.CENTER);
        composer.add(sendButton, BorderLayout.EAST);
        add(composer, BorderLayout.SOUTH);
        setChatEnabled(false);
    }

    private void sendMessage() {
        String text = input.getText().strip();
        if (!input.isEnabled() || text.isEmpty()) {
            return;
        }
        // Display our own message only when echoed by the server, just like the opponent's.
        if (sender.test(text)) {
            input.setText("");
            input.requestFocusInWindow();
        }
    }

    public void setChatEnabled(boolean enabled) {
        input.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    public void reset(String notice) {
        transcript.setText("");
        input.setText("");
        addMessage("System", notice);
    }

    public void addMessage(String author, String text) {
        transcript.append(author + ": " + text + "\n");
        try {
            while (transcript.getLineCount() > MAX_MESSAGES + 1) {
                transcript.getDocument().remove(0, transcript.getLineEndOffset(0));
            }
        } catch (BadLocationException exception) {
            throw new IllegalStateException("Unable to trim chat history", exception);
        }
    }
}
