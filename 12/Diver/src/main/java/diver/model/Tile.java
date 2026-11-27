package diver.model;

/**
 * Egy mező fajtája a pályán. A pályafájlban egy karakter jelöli; a búvár,
 * a cápák kezdőhelyét jelölő D, H, V karakterek nem mezőfajták (alattuk
 * víz van), azokat a Level külön tárolja.
 */
public enum Tile {
    /** Szikla: nem lehet rálépni, a cápa nem lát át rajta. */
    ROCK('#'),
    /** Víz: szabad mező. */
    WATER('.'),
    /** Gyöngy: szabad mező, a búvár felveszi, ha rálép; utána víz. */
    PEARL('P');

    private final char symbol;

    Tile(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    /**
     * @return igaz, ha a búvár és a cápák ráléphetnek
     */
    public boolean isPassable() {
        return this != ROCK;
    }

}
