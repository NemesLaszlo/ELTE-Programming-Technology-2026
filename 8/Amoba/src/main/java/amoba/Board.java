package amoba;

/**
 * A tábla: rows × columns mező, mindegyik üres vagy egy játékosé. A modell
 * része, nem használ Swinget.
 *
 * A Board két dolgot tud, és ennyit: a jel potyogtatását egy oszlopba (drop),
 * és annak eldöntését, hogy egy mezőn át van-e négy egyforma jel egy sorban
 * (hasLineThrough). Azt, hogy ki következik, mikor van vége a játéknak és ki
 * nyert, a Game osztály tudja: a Board nem ismeri a játék menetét, csak a
 * tábla szabályait.
 *
 * Koordináták: fields[sor][oszlop]; a 0. sor a tábla teteje, a jelek a
 * nagyobb sorszámú sorok felé esnek. Ez megegyezik a GridLayout
 * sorrendjével, így a nézet (sor, oszlop) párja közvetlenül a modellé.
 */
public class Board {

    /** Ennyi egymás melletti azonos jel nyer. */
    public static final int WINNING_LENGTH = 4;

    /**
     * Az irányok, amelyek mentén a négyes számít: {sorlépés, oszloplépés}.
     * Vízszintes {0, 1}, függőleges {1, 0}, és a két átló {1, 1}, {1, -1}.
     * Minden irányt mindkét értelemben nézünk (ld. hasLineThrough), ezért az
     * ellentettjét nem kell felsorolni. Ha a szabály változna (pl. a
     * függőleges négyes nem számítana), csak ezt a tömböt kell módosítani.
     */
    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

    private final int rows;
    private final int columns;
    /** A mezők: fields[sor][oszlop]; null az üres mező. */
    private final Player[][] fields;
    /** A táblán lévő jelek száma; ha rows × columns, a tábla betelt. */
    private int pieceCount;

    /**
     * Új, üres tábla.
     *
     * @param rows a sorok száma, legalább 1
     * @param columns az oszlopok száma, legalább 1
     */
    public Board(int rows, int columns) {
        if (rows < 1 || columns < 1) {
            throw new IllegalArgumentException("A tábla mérete legalább 1 × 1: " + rows + " × " + columns);
        }
        this.rows = rows;
        this.columns = columns;
        fields = new Player[rows][columns];
    }

    /**
     * @param size a tábla mérete
     */
    public Board(BoardSize size) {
        this(size.getRows(), size.getColumns());
    }

    /**
     * @return a sorok száma
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return az oszlopok száma
     */
    public int getColumns() {
        return columns;
    }

    /**
     * @return a táblán lévő jelek száma
     */
    public int getPieceCount() {
        return pieceCount;
    }

    /**
     * @param row sor
     * @param col oszlop
     * @return a mező tulajdonosa, vagy null, ha a mező üres
     * @throws IllegalArgumentException ha a hely a táblán kívül van
     */
    public Player get(int row, int col) {
        checkPosition(row, col);
        return fields[row][col];
    }

    /**
     * @param position a mező helye
     * @return a mező tulajdonosa, vagy null, ha a mező üres
     * @throws IllegalArgumentException ha a hely a táblán kívül van
     */
    public Player get(Position position) {
        return get(position.row(), position.col());
    }

    /**
     * @param col oszlop
     * @return igaz, ha az oszlop tele van: a legfelső mezője sem üres
     * @throws IllegalArgumentException ha az oszlop a táblán kívül van
     */
    public boolean isColumnFull(int col) {
        checkColumn(col);
        return fields[0][col] != null;
    }

    /**
     * @return igaz, ha minden mező foglalt
     */
    public boolean isFull() {
        return pieceCount == rows * columns;
    }

    /**
     * Egy jel potyogtatása: a jel az oszlop legalsó üres mezőjére esik. A
     * feladat szerint mindegy, melyik sorban kattintottunk, ezért a metódus
     * csak az oszlopot kapja.
     *
     * @param col az oszlop
     * @param player a lépő játékos
     * @return a mező, ahova a jel esett
     * @throws IllegalStateException ha az oszlop tele van (ld. isColumnFull)
     * @throws IllegalArgumentException ha az oszlop a táblán kívül van, vagy
     * a játékos null
     */
    public Position drop(int col, Player player) {
        checkColumn(col);
        if (player == null) {
            throw new IllegalArgumentException("A játékos nem lehet null");
        }
        // Alulról felfelé az első üres mező.
        for (int row = rows - 1; row >= 0; row--) {
            if (fields[row][col] == null) {
                fields[row][col] = player;
                pieceCount++;
                return new Position(row, col);
            }
        }
        throw new IllegalStateException("Az oszlop tele van: " + col);
    }

    /**
     * Van-e a megadott mezőn átmenő, legalább WINNING_LENGTH hosszú sor a
     * mező tulajdonosának jeleiből, a DIRECTIONS valamelyik iránya mentén.
     * Elég a legutóbb lerakott jel mezőjét vizsgálni: ha a lépés nyert, a
     * négyesben benne van az új jel.
     *
     * @param position a vizsgált mező (a legutóbbi lépés helye)
     * @return igaz, ha a mezőn át van négyes
     * @throws IllegalArgumentException ha a hely a táblán kívül van
     */
    public boolean hasLineThrough(Position position) {
        Player owner = get(position);
        if (owner == null) {
            return false;
        }
        for (int[] direction : DIRECTIONS) {
            int dRow = direction[0];
            int dCol = direction[1];
            // A mező maga, plusz az azonos jelek az irányban és az
            // ellentettjében: így a mező a négyes bármelyik helyén lehet.
            int length = 1 + count(position, dRow, dCol) + count(position, -dRow, -dCol);
            if (length >= WINNING_LENGTH) {
                return true;
            }
        }
        return false;
    }

    /**
     * Hány, a kiinduló mezővel azonos tulajdonosú mező követi egymást a
     * megadott irányban (a kiinduló mezőt nem számolva).
     */
    private int count(Position from, int dRow, int dCol) {
        Player owner = fields[from.row()][from.col()];
        int count = 0;
        int row = from.row() + dRow;
        int col = from.col() + dCol;
        while (isInside(row, col) && fields[row][col] == owner) {
            count++;
            row += dRow;
            col += dCol;
        }
        return count;
    }

    private boolean isInside(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < columns;
    }

    private void checkPosition(int row, int col) {
        if (!isInside(row, col)) {
            throw new IllegalArgumentException("A táblán kívüli hely: (" + row + ", " + col + ")");
        }
    }

    private void checkColumn(int col) {
        if (col < 0 || col >= columns) {
            throw new IllegalArgumentException("A táblán kívüli oszlop: " + col);
        }
    }

}
