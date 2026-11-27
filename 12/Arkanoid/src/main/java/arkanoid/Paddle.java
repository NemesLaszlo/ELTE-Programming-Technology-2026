package arkanoid;

import java.awt.Image;

/**
 * Az ütő: a játékos sprite-ja. Vízszintesen mozog a beállított sebességgel
 * (a billentyűk állítják), és nem hagyja el a játékteret.
 *
 * @author bli
 */
public class Paddle extends Sprite {

    /** A vízszintes sebesség: negatív balra, pozitív jobbra, 0 áll. */
    private int velX;

    public Paddle(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }

    /**
     * Egy lépés a sebesség irányába, ha azzal még a játéktéren belül marad.
     */
    public void move() {
        if ((velX < 0 && x > 0) || (velX > 0 && x + width < GameEngine.WIDTH)) {
            x += velX;
        }
    }

    public int getVelX() {
        return velX;
    }

    public void setVelX(int velX) {
        this.velX = velX;
    }

}
