package ufos;

/**
 * Egy UFO a MODELLBEN: a helye, a sebessége és a színe. Nincs benne
 * java.awt: a szín egész szám (0xRRGGBB, mint a 8. heti Field-ben), a
 * kirajzolás a nézet (DrawArea) dolga.
 *
 * A hely az UFO befoglaló téglalapjának bal felső sarka; a téglalap mérete
 * WIDTH × HEIGHT. A nézet ebbe a téglalapba rajzolja a kupolát és a testet.
 */
public class Ufo {

    /** A befoglaló téglalap szélessége. */
    public static final int WIDTH = 200;
    /** A befoglaló téglalap magassága (a kupola is beleértve). */
    public static final int HEIGHT = 55;

    private double x;
    private double y;
    private final double velX;
    private final double velY;
    private int rgb;

    /**
     * @param x a bal felső sarok x koordinátája
     * @param y a bal felső sarok y koordinátája
     * @param velX az x irányú sebesség (képpont / lépés)
     * @param velY az y irányú sebesség (képpont / lépés)
     * @param rgb a szín 0xRRGGBB alakban
     */
    public Ufo(double x, double y, double velX, double velY, int rgb) {
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.rgb = rgb;
    }

    /**
     * Egy lépés a sebesség irányába. Ha az UFO kimegy a térből, a
     * túloldalon jön vissza (a tér "gömbölyű", mint a régi Asteroids
     * játékban).
     *
     * @param width a tér szélessége
     * @param height a tér magassága
     */
    public void move(int width, int height) {
        x += velX;
        if (x > width) {
            x = 0;
        } else if (x < 0) {
            x = width;
        }
        y += velY;
        if (y > height) {
            y = 0;
        } else if (y < 0) {
            y = height;
        }
    }

    /**
     * Áthelyezi az UFO-t a megadott pontra (az egérrel húzáskor).
     */
    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    /**
     * @return a szín 0xRRGGBB alakban; a nézet new Color(rgb)-vel használja
     */
    public int getRgb() {
        return rgb;
    }

    /**
     * Csomagszintű: csak a Space (a modell) színezheti át az UFO-t.
     */
    void setRgb(int rgb) {
        this.rgb = rgb;
    }

}
