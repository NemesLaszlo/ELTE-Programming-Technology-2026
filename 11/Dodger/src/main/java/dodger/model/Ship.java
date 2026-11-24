package dodger.model;

/**
 * A játékos űrhajója. Egy sprite, ami a megadott irányba lép, de nem hagyja
 * el a játékteret.
 */
public class Ship extends Sprite {

    /** A hajó mérete képpontban (négyzet). */
    public static final int SIZE = 30;
    /** Ennyi képpontot lép egy ütemben. */
    public static final int SPEED = 5;

    public Ship(int x, int y) {
        super(x, y, SIZE, SIZE);
    }

    /**
     * Egy lépés az irányba, a játéktér határain belül maradva: a szélén a
     * hajó megáll, nem fordul át a túloldalra.
     *
     * @param direction az irány
     * @param fieldWidth a játéktér szélessége
     * @param fieldHeight a játéktér magassága
     */
    void move(Direction direction, int fieldWidth, int fieldHeight) {
        int newX = clamp(getX() + direction.getDx() * SPEED, 0, fieldWidth - SIZE);
        int newY = clamp(getY() + direction.getDy() * SPEED, 0, fieldHeight - SIZE);
        moveTo(newX, newY);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

}
