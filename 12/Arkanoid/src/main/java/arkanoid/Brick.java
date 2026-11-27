package arkanoid;

import java.awt.Image;

/**
 * Egy tégla: egy sprite, ami nem mozog. Ha a labda eltalálja, eltűnik
 * (ld. Level.collides).
 *
 * @author bli
 */
public class Brick extends Sprite {

    public Brick(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }

}
