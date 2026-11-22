package game.client;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Design pattern – Flyweight (FlyweightFactory).
 *
 * <p>Manages the pool of shared {@link CardArtFlyweight} instances keyed by
 * asset path ({@code -flyweights}). When {@link #getFlyweight(String, Color)}
 * is called:
 * <ul>
 *   <li>If a shared flyweight for {@code key} already exists in the pool, the
 *       existing {@link SharedCardArtFlyweight} is returned.</li>
 *   <li>Otherwise, the image is loaded from disk, wrapped in a new
 *       {@link SharedCardArtFlyweight} (ConcreteFlyweight), added to the pool,
 *       and returned.</li>
 *   <li>If the asset file does not exist, an unshared
 *       {@link PlaceholderCardArtFlyweight} (UnsharedConcreteFlyweight) is
 *       returned.</li>
 * </ul>
 */
public final class CardImageFlyweightFactory {

    /*
     * Pool of shared flyweights: key (asset path) -> CardArtFlyweight.
     */
    private static final Map<String, CardArtFlyweight> flyweights = new HashMap<>();

    private CardImageFlyweightFactory() {}

    /**
     * Returns an existing shared {@link CardArtFlyweight} from the pool if one
     * exists for {@code key}; otherwise creates a new {@link SharedCardArtFlyweight},
     * stores it in the pool, and returns it (or returns an unshared
     * {@link PlaceholderCardArtFlyweight} if the image file is missing).
     */
    public static CardArtFlyweight getFlyweight(String key, Color fallbackColor) {
        if (flyweights.containsKey(key)) {
            return flyweights.get(key);
        }

        File file = new File(key);
        if (file.exists()) {
            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    CardArtFlyweight shared = new SharedCardArtFlyweight(image);
                    flyweights.put(key, shared);
                    return shared;
                }
            } catch (IOException ignored) {
                // Missing or unreadable art falls back to an unshared placeholder flyweight.
            }
        }

        return new PlaceholderCardArtFlyweight(fallbackColor);
    }
}
