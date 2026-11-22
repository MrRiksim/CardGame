package game.client;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Design pattern – Flyweight (Flyweight interface).
 *
 * <p>Defines the interface through which {@link CardRenderer} (the Client)
 * passes extrinsic state ({@code Graphics2D} and the target {@code Rectangle}
 * coordinates/size on screen) to render card artwork.
 */
public interface CardArtFlyweight {

    /**
     * Operation(ExtrinsicState) – draws the card artwork using the supplied
     * extrinsic rendering context and bounds.
     *
     * @param g         extrinsic graphics context
     * @param imageRect extrinsic position and dimensions on the board/hand
     */
    void draw(Graphics2D g, Rectangle imageRect);
}
