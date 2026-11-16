package game.client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Design pattern – Flyweight.
 *
 * <p>Card artwork images are the <b>intrinsic (shared, immutable) state</b>:
 * each distinct asset path maps to exactly one {@link BufferedImage} that is
 * loaded from disk once and then shared by every caller that asks for the
 * same path.  The image data never changes after loading, so sharing it is
 * always safe.
 *
 * <p>The <b>extrinsic state</b> – where on screen to draw the image, at what
 * size, inside which card rectangle – is never stored here.  Callers
 * ({@link CardRenderer}) supply it at the point of use, keeping this class
 * purely concerned with sharing image data.
 *
 * <p>No caller outside this class can reach the cache map, satisfying the
 * Flyweight requirement that the factory is the single gatekeeper for shared
 * instances.
 */
public final class CardImageFlyweightFactory {

    /*
     * Intrinsic-state pool: path → shared BufferedImage (or null when the
     * file is missing/unreadable, cached so we don't retry the disk on every
     * repaint).
     */
    private static final Map<String, BufferedImage> cache = new HashMap<>();

    private CardImageFlyweightFactory() {}

    /**
     * Returns the shared {@link BufferedImage} for {@code path}, loading it
     * from disk on the first request and reusing the same object on every
     * subsequent request for that path.
     *
     * @param path relative path to the asset file (e.g. "assets/units/knight.png")
     * @return the shared image, or {@code null} if the file is missing or unreadable
     */
    public static BufferedImage get(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        BufferedImage image = null;
        File file = new File(path);
        if (file.exists()) {
            try {
                image = ImageIO.read(file);
            } catch (IOException ignored) {
                // Missing or corrupt art falls back to a placeholder color in CardRenderer.
            }
        }

        cache.put(path, image);
        return image;
    }
}
