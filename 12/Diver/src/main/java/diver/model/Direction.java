package diver.model;

/**
 * A négy irány a rácson. A dRow, dCol az elmozdulás egy lépésben
 * (a sor lefelé, az oszlop jobbra nő).
 *
 * A nézet a W/A/S/D billentyűket képezi le ezekre; a modell billentyűt nem
 * ismer. A cápák is egy irányba néznek és mennek.
 */
public enum Direction {
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    private final int dRow;
    private final int dCol;

    Direction(int dRow, int dCol) {
        this.dRow = dRow;
        this.dCol = dCol;
    }

    public int getDRow() {
        return dRow;
    }

    public int getDCol() {
        return dCol;
    }

    /**
     * @return az ellentétes irány (a cápa ebbe fordul, ha falba ütközik)
     */
    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

}
