package dodger.model;

/**
 * Egy felülről lefelé hulló tárgy: aszteroida (ha eltalál, életet veszítünk)
 * vagy csillag (ha elkapjuk, pontot ér). A fajtája és a sebessége nem
 * változik, miután létrejött.
 */
public class FallingObject extends Sprite {

    /** A tárgy fajtája. */
    public enum Kind {
        /** Ütközéskor egy élet elvész. */
        ASTEROID,
        /** Elkapáskor egy pont jár. */
        STAR
    }

    /** Az aszteroida mérete képpontban. */
    public static final int ASTEROID_SIZE = 36;
    /** A csillag mérete képpontban. */
    public static final int STAR_SIZE = 24;

    private final Kind kind;
    private final int speed;

    /**
     * @param kind a fajtája
     * @param x a bal felső sarok x koordinátája
     * @param y a bal felső sarok y koordinátája
     * @param speed ennyi képpontot esik egy ütemben
     */
    public FallingObject(Kind kind, int x, int y, int speed) {
        super(x, y, sizeOf(kind), sizeOf(kind));
        this.kind = kind;
        this.speed = speed;
    }

    /**
     * @param kind a fajta
     * @return a fajta mérete képpontban
     */
    public static int sizeOf(Kind kind) {
        return kind == Kind.ASTEROID ? ASTEROID_SIZE : STAR_SIZE;
    }

    /**
     * Egy ütemnyi esés lefelé.
     */
    void fall() {
        moveBy(0, speed);
    }

    public Kind getKind() {
        return kind;
    }

    public int getSpeed() {
        return speed;
    }

}
