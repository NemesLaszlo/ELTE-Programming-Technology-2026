package arkanoid;

import java.awt.Image;

/**
 * A labda: egy sprite, aminek sebessége van, és a játéktér széléről
 * visszapattan.
 *
 * @author bli
 */
public class Ball extends Sprite {

    /** A sebesség x és y irányú összetevője (képpont / képkocka). */
    private int velX = 1;
    private int velY = 1;

    public Ball(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }

    /**
     * Egy lépés vízszintesen; a bal és a jobb szélről visszapattan.
     */
    public void moveX() {
        x += velX;
        if (x + width >= GameEngine.WIDTH || x <= 0) {
            invertVelX();
        }
    }

    /**
     * Egy lépés függőlegesen; a felső szélről visszapattan.
     *
     * @return hamis, ha a labda kiment alul (az ütő mellett): elveszett
     */
    public boolean moveY() {
        y += velY;
        if (y <= 0) {
            invertVelY();
        }
        return y < GameEngine.HEIGHT;
    }

    public void invertVelX() {
        velX = -velX;
    }

    public void invertVelY() {
        velY = -velY;
    }

}
