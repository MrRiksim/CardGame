package game.client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ChatPanelSelfTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<String> sent = new ArrayList<>();
            ChatPanel panel = new ChatPanel(text -> { sent.add(text); return true; });
            JTextField input = find(panel, JTextField.class);
            JButton button = find(panel, JButton.class);
            JTextArea transcript = find(panel, JTextArea.class);
            input.setText("disabled");
            input.postActionEvent();
            check(sent.isEmpty() && !button.isEnabled(), "Chat must start disabled");

            panel.setChatEnabled(true);
            input.setText("  hello | opponent; 🙂  ");
            input.postActionEvent();
            check(sent.equals(List.of("hello | opponent; 🙂")) && input.getText().isEmpty(),
                    "Enter must send trimmed literal text and clear the composer");
            check(transcript.getText().isEmpty(), "Sent messages must wait for the server echo");
            input.setText("second message");
            button.doClick();
            check(sent.size() == 2 && sent.get(1).equals("second message"), "Send button must send");
            input.setText("   ");
            button.doClick();
            check(sent.size() == 2, "Blank messages must not send");
            input.setText("x".repeat(350));
            check(input.getText().length() == 300, "Composer must enforce the server's length limit");
            input.setText("one\ntwo");
            check(!input.getText().contains("\n"), "Pasted input must stay on one line");
            panel.addMessage("Player 2", "<html>Literal | punctuation, stays; visible</html>");
            check(transcript.getText().contains("<html>Literal"), "Messages must be plain text");
            for (int i = 0; i < 205; i++) panel.addMessage("You", "Message " + i);
            check(transcript.getLineCount() == 201 && transcript.getText().contains("Message 204"),
                    "Local history must be bounded and retain the latest messages");
            panel.reset("New match");
            check(transcript.getText().equals("System: New match\n") && input.getText().isEmpty(),
                    "New matches must reset transcript and composer");

            ChatPanel failed = new ChatPanel(text -> false);
            failed.setChatEnabled(true);
            JTextField failedInput = find(failed, JTextField.class);
            failedInput.setText("keep this draft");
            failedInput.postActionEvent();
            check(failedInput.getText().equals("keep this draft"), "Failed sends must retain the draft");

            if (args.length > 0) renderPreview(args[0]);
        });
        System.out.println("All chat panel checks passed.");
    }

    private static void renderPreview(String path) {
        JPanel preview = new JPanel(new BorderLayout(0, 8));
        preview.setSize(300, 940);
        preview.add(new CardInfoPanel(), BorderLayout.CENTER);
        ChatPanel chat = new ChatPanel(text -> true);
        chat.setChatEnabled(true);
        chat.reset("Message your opponent here. Press Enter or click Send.");
        chat.addMessage("Player 2", "Good luck!");
        chat.addMessage("You", "You too! That Chain Lightning was close.");
        chat.addMessage("Player 2", "I still have a shield ready 🙂");
        preview.add(chat, BorderLayout.SOUTH);
        layout(preview);
        BufferedImage image = new BufferedImage(300, 940, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        preview.printAll(graphics);
        graphics.dispose();
        try {
            ImageIO.write(image, "png", new File(path));
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested) layout(nested);
        }
    }

    private static <T extends Component> T find(Container container, Class<T> type) {
        for (Component child : container.getComponents()) {
            if (child.getClass() == type) return type.cast(child);
            if (child instanceof Container nested) {
                T found = find(nested, type);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
