package amoba;

/**
 * A választható táblaméretek. A modell része.
 *
 * A feladat n × m táblát mond, ahol n az oszlopok, m a sorok száma: 8×5,
 * 10×6, 12×7. A konstruktor paraméterei ugyanebben a sorrendben (oszlop,
 * sor) vannak, hogy a feladatlappal egyezzen; a Board viszont mindenhol
 * (sor, oszlop) sorrendet használ, mint a tömbindexelés.
 */
public enum BoardSize {

    SMALL(8, 5),
    MEDIUM(10, 6),
    LARGE(12, 7);

    private final int columns;
    private final int rows;

    BoardSize(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
    }

    /**
     * @return az oszlopok száma (n)
     */
    public int getColumns() {
        return columns;
    }

    /**
     * @return a sorok száma (m)
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return a méret a feladatlap jelölésével, pl. "8 × 5"; ez a menüpont
     * felirata
     */
    @Override
    public String toString() {
        return columns + " × " + rows;
    }

}
