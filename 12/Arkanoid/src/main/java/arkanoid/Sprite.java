package arkanoid;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * Egy sprite: egy játékbeli tárgy, aminek helye, mérete és KÉPE van, és ki
 * tudja rajzolni magát. A 2D játékok alapegysége.
 *
 * Figyeljük meg: ez a sprite a képet is tartja, és maga rajzol
 * (draw(Graphics)): a "modell" és a "nézet" egy osztályban van, mint a 10.
 * heti UFOs kiinduló változatában. Ez egy kis játékhoz elég, de a sprite
 * így nem tesztelhető Swing nélkül; a Diver mintában a sprite csak a
 * téglalap, a kép a nézeté.
 *
 * @author bli
 */
public class Sprite {

    /** A bal felső sarok. */
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Image image;

    public Sprite(int x, int y, int width, int height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    /**
     * Kirajzolja a képet a sprite helyére, a sprite méretében.
     *
     * @param g a rajzfelület (a GameEngine.paintComponent adja)
     */
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    /**
     * @param other a másik sprite
     * @return igaz, ha a két befoglaló téglalap metszi egymást
     */
    public boolean collides(Sprite other) {
        Rectangle rect = new Rectangle(x, y, width, height);
        Rectangle otherRect = new Rectangle(other.x, other.y, other.width, other.height);
        return rect.intersects(otherRect);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
