package game.client;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws a single card the same way everywhere it appears (hand, dragged,
 * or sitting on the board), so they never drift out of sync visually.
 *
 * <p>Layout, per spec: no card border; artwork fills ~65% of the card's
 * height (image area on top, name+description area below); a white diamond
 * for energy cost is centered on the very top edge of every card; unit
 * cards additionally get a dark green HP square top-left and a dark grey
 * damage square top-right.
 */
public final class CardRenderer {

    private static final double IMAGE_HEIGHT_FRACTION = 0.65;

    private static final Color BODY_BACKGROUND = new Color(250, 250, 248);
    private static final Color TEXT_BACKGROUND = new Color(236, 234, 228);
    private static final Color HP_COLOR = new Color(30, 95, 45);
    private static final Color DAMAGE_COLOR = new Color(70, 70, 70);
    private static final Color PLACEHOLDER_UNIT = new Color(200, 225, 205);
    private static final Color PLACEHOLDER_SPELL = new Color(212, 205, 235);
    private static final Color PLACEHOLDER_TRAP = new Color(230, 205, 205);

    /*
     * Card art doesn't exist yet in a fresh checkout (the assets/ folder is
     * the user's to fill in), so a missing file falls back to a flat color
     * instead of crashing - the board stays usable before any art lands.
     */
    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();

    private CardRenderer() {
    }

    public static void draw(Graphics2D g, Rectangle rect, ClientCard card, boolean showDescription) {
        g.setColor(BODY_BACKGROUND);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        int imageHeight = (int) Math.round(rect.height * IMAGE_HEIGHT_FRACTION);
        Rectangle imageRect = new Rectangle(rect.x, rect.y, rect.width, imageHeight);
        Rectangle textRect = new Rectangle(
                rect.x, rect.y + imageHeight, rect.width, rect.height - imageHeight);

        drawArtwork(g, imageRect, card);
        drawTextArea(g, textRect, card, showDescription);
        drawCostDiamond(g, rect, card.energyCost());

        if (card instanceof ClientUnit unit) {
            drawStatSquare(g, rect, true, unit.health(), HP_COLOR);
            drawStatSquare(g, rect, false, unit.damage(), DAMAGE_COLOR);
        }
    }

    private static void drawArtwork(Graphics2D g, Rectangle imageRect, ClientCard card) {
        BufferedImage image = loadImage(CardArt.getImagePath(card.kind(), card.name()));

        if (image != null) {
            g.drawImage(image, imageRect.x, imageRect.y, imageRect.width, imageRect.height, null);
            return;
        }

        g.setColor(placeholderColor(card.kind()));
        g.fillRect(imageRect.x, imageRect.y, imageRect.width, imageRect.height);
    }

    private static Color placeholderColor(CardKind kind) {
        return switch (kind) {
            case UNIT -> PLACEHOLDER_UNIT;
            case SPELL -> PLACEHOLDER_SPELL;
            case TRAP -> PLACEHOLDER_TRAP;
        };
    }

    private static BufferedImage loadImage(String path) {
        IMAGE_CACHE.clear();

        if (IMAGE_CACHE.containsKey(path)) {
            return IMAGE_CACHE.get(path);
        }

        BufferedImage image = null;
        File file = new File(path);
        if (file.exists()) {
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                image = null;
            }
        }

        IMAGE_CACHE.put(path, image);
        return image;
    }

    private static void drawTextArea(
            Graphics2D g, Rectangle textRect, ClientCard card, boolean showDescription) {

        g.setColor(TEXT_BACKGROUND);
        g.fillRect(textRect.x, textRect.y, textRect.width, textRect.height);

        int padding = Math.max(2, textRect.width / 20);

        int nameFontSize = Math.max(9, textRect.height / 5);
        g.setFont(new Font("Arial", Font.BOLD, nameFontSize));
        g.setColor(Color.BLACK);

        int nameY = textRect.y + g.getFontMetrics().getAscent() + 2;
        drawClipped(g, card.name(), textRect.x + padding, nameY, textRect.width - 2 * padding);

        if (!showDescription || card.description() == null || card.description().isEmpty()) {
            return;
        }

        int descFontSize = Math.max(7, textRect.height / 9);
        g.setFont(new Font("Arial", Font.PLAIN, descFontSize));
        int lineHeight = g.getFontMetrics().getHeight();
        int y = nameY + lineHeight;

        for (String line : wrapText(g, card.description(), textRect.width - 2 * padding)) {
            if (y > textRect.y + textRect.height - 2) {
                break;
            }
            g.drawString(line, textRect.x + padding, y);
            y += lineHeight;
        }
    }

    private static void drawClipped(Graphics2D g, String text, int x, int y, int maxWidth) {
        FontMetrics metrics = g.getFontMetrics();
        String display = text;
        while (metrics.stringWidth(display) > maxWidth && display.length() > 1) {
            display = display.substring(0, display.length() - 1);
        }
        g.drawString(display, x, y);
    }

    private static List<String> wrapText(Graphics2D g, String text, int maxWidth) {
        FontMetrics metrics = g.getFontMetrics();
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : text.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (metrics.stringWidth(candidate) > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static void drawCostDiamond(Graphics2D g, Rectangle rect, int energyCost) {
        int size = (int) Math.round(rect.width * 0.30);
        int centerX = rect.x + rect.width / 2;
        int centerY = rect.y;

        Polygon diamond = new Polygon();
        diamond.addPoint(centerX, centerY - size / 2);
        diamond.addPoint(centerX + size / 2, centerY);
        diamond.addPoint(centerX, centerY + size / 2);
        diamond.addPoint(centerX - size / 2, centerY);

        g.setColor(Color.WHITE);
        g.fillPolygon(diamond);
        g.setColor(Color.DARK_GRAY);
        g.drawPolygon(diamond);

        drawCenteredNumber(g, energyCost, centerX, centerY, size);
    }

    private static void drawStatSquare(
            Graphics2D g, Rectangle rect, boolean isLeft, int value, Color color) {

        int size = (int) Math.round(rect.width * 0.28);
        int margin = Math.max(1, rect.width / 30);
        int x = isLeft ? rect.x + margin : rect.x + rect.width - margin - size;
        int y = rect.y + margin;

        g.setColor(color);
        g.fillRect(x, y, size, size);

        drawCenteredNumber(g, value, x + size / 2, y + size / 2, size, Color.WHITE);
    }

    private static void drawCenteredNumber(Graphics2D g, int value, int centerX, int centerY, int boxSize) {
        drawCenteredNumber(g, value, centerX, centerY, boxSize, Color.BLACK);
    }

    private static void drawCenteredNumber(
            Graphics2D g, int value, int centerX, int centerY, int boxSize, Color textColor) {

        g.setColor(textColor);
        g.setFont(new Font("Arial", Font.BOLD, Math.max(9, boxSize / 2)));
        String text = String.valueOf(value);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(
                text,
                centerX - metrics.stringWidth(text) / 2,
                centerY + metrics.getAscent() / 2 - 2
        );
    }
}
