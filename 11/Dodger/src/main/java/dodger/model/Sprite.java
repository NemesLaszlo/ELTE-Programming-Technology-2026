package dodger.model;

/**
 * Egy mozgó alakzat a játéktéren: a befoglaló téglalapja (bal felső sarok,
 * szélesség, magasság). A sprite a MODELL része: nem tudja, hogyan néz ki,
 * csak azt, hol van és mekkora. A kirajzolás a nézet (GamePanel) dolga.
 *
 * Az ütközést a befoglaló téglalapok metszésével vizsgáljuk (intersects):
 * ez a legegyszerűbb, és egy ilyen játékhoz bőven elég.
 */
public class Sprite {

    private int x;
    private int y;
    private final int width;
    private final int height;

    /**
     * @param x a bal felső sarok x koordinátája
     * @param y a bal felső sarok y koordinátája
     * @param width a szélesség
     * @param height a magasság
     */
    public Sprite(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * @param other a másik sprite
     * @return igaz, ha a két befoglaló téglalapnak van közös pontja
     */
    public boolean intersects(Sprite other) {
        return x < other.x + other.width && other.x < x + width
                && y < other.y + other.height && other.y < y + height;
    }

    /**
     * Elmozdítja a sprite-ot. Csomagszintű: csak a modell mozgathatja.
     */
    void moveBy(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Áthelyezi a sprite-ot. Csomagszintű: csak a modell mozgathatja.
     */
    void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
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

    /**
     * @return a téglalap alsó széle (y + magasság)
     */
    public int getBottom() {
        return y + height;
    }

    /**
     * @return a téglalap jobb széle (x + szélesség)
     */
    public int getRight() {
        return x + width;
    }

}
