package game.client;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * <b>Design pattern - Mediator (Concrete Colleague 1):</b> side panel that
 * displays detailed card information in a table using {@link CardInfoVisitor}.
 */
public class CardInfoPanel extends UiColleague {

    private final JLabel titleLabel;
    private final DefaultTableModel tableModel;
    private final JTextArea descriptionArea;
    private final JPanel previewPanel;
    private final JButton clearButton;

    private ClientCard currentCard;

    public CardInfoPanel() {
        super(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(260, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        setBackground(new Color(245, 245, 242));

        titleLabel = new JLabel("Click any card to inspect", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));

        previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentCard == null) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cardW = 120;
                int cardH = 175;
                int x = (getWidth() - cardW) / 2;
                int y = (getHeight() - cardH) / 2;
                CardRenderer.draw(g2, new Rectangle(x, y, cardW, cardH), currentCard, true);
                g2.dispose();
            }
        };
        previewPanel.setPreferredSize(new Dimension(220, 190));
        previewPanel.setOpaque(false);

        JPanel topSection = new JPanel(new BorderLayout(4, 6));
        topSection.setOpaque(false);
        topSection.add(titleLabel, BorderLayout.NORTH);
        topSection.add(previewPanel, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new String[]{"Attribute", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);

        descriptionArea = new JTextArea(4, 18);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Arial", Font.ITALIC, 12));
        descriptionArea.setBackground(new Color(236, 234, 228));
        descriptionArea.setBorder(BorderFactory.createTitledBorder("Description"));

        clearButton = new JButton("Clear Selection");
        clearButton.setEnabled(false);
        clearButton.addActionListener(e -> {
            if (mediator != null) {
                mediator.notify(this, UiEvent.SELECTION_CLEARED, null);
            } else {
                clear();
            }
        });

        JPanel southSection = new JPanel(new BorderLayout(4, 6));
        southSection.setOpaque(false);
        southSection.add(descriptionArea, BorderLayout.CENTER);
        southSection.add(clearButton, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(southSection, BorderLayout.SOUTH);
    }

    public void show(ClientCard card) {
        if (card == null) {
            clear();
            return;
        }
        this.currentCard = card;

        CardInfoVisitor visitor = new CardInfoVisitor();
        card.accept(visitor);

        titleLabel.setText(visitor.getTitle());
        tableModel.setRowCount(0);
        for (String[] row : visitor.getRows()) {
            tableModel.addRow(row);
        }
        descriptionArea.setText(visitor.getDescription().isEmpty()
                ? "No additional description."
                : visitor.getDescription());
        clearButton.setEnabled(true);
        previewPanel.repaint();
    }

    public void clear() {
        this.currentCard = null;
        titleLabel.setText("Click any card to inspect");
        tableModel.setRowCount(0);
        descriptionArea.setText("");
        clearButton.setEnabled(false);
        previewPanel.repaint();
    }
}
