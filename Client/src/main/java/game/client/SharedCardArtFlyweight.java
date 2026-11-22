package game.client;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Design pattern – Flyweight (ConcreteFlyweight).
 *
 * <p>Stores the <b>intrinsic state</b> ({@code image} – the decoded
 * {@link BufferedImage} loaded once from disk and shared across all card
 * instances with the same artwork).
 *
 * <p>The <b>extrinsic state</b> (where on screen to draw it and at what size,
 * {@code imageRect}) is passed into {@link #draw(Graphics2D, Rectangle)} by
 * the client ({@link CardRenderer}) at render time.
 */
public final class SharedCardArtFlyweight implements CardArtFlyweight {

    private final BufferedImage intrinsicImage;

    public SharedCardArtFlyweight(BufferedImage intrinsicImage) {
        this.intrinsicImage = intrinsicImage;
    }

    public BufferedImage getIntrinsicImage() {
        return intrinsicImage;
    }

    @Override
    public void draw(Graphics2D g, Rectangle imageRect) {
        g.drawImage(intrinsicImage, imageRect.x, imageRect.y, imageRect.width, imageRect.height, null);
    }
}
