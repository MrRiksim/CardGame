package game.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Design pattern – Flyweight (UnsharedConcreteFlyweight).
 *
 * <p>Used when a card's artwork file is not shared from the image pool (for
 * example, missing artwork that falls back to a card-specific placeholder
 * color and bounds). Implements {@link CardArtFlyweight} so {@link CardRenderer}
 * can invoke {@link #draw(Graphics2D, Rectangle)} uniformly.
 */
public final class PlaceholderCardArtFlyweight implements CardArtFlyweight {

    private final Color allStatesColor;

    public PlaceholderCardArtFlyweight(Color allStatesColor) {
        this.allStatesColor = allStatesColor;
    }

    @Override
    public void draw(Graphics2D g, Rectangle imageRect) {
        g.setColor(allStatesColor);
        g.fillRect(imageRect.x, imageRect.y, imageRect.width, imageRect.height);
    }
}
