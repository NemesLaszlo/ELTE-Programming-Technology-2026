package amoba;

/**
 * A potyogós amőba MODELLJE (az MV "M" betűje): a játék menete.
 *
 * A Game tudja, ki következik, elfogadja a soron következő játékos lépését
 * (drop), és a lépés után eldönti, hogy vége van-e a játéknak: nyert-e a
 * lépő játékos, vagy betelt-e a tábla (döntetlen). A tábla szabályait (a
 * potyogást és a négyes keresését) a Board-ra bízza.
 *
 * A modell semmit nem tud a felületről, nem használ Swinget. A nézet
 * (AmobaGUI) eseménykezelői hívják a metódusait, majd a nézet a modell
 * állapota alapján frissíti magát. Mivel nincs benne Swing, felület nélkül,
 * JUnit-tal tesztelhető (ld. GameTest), és egy parancssoros felülettel is
 * játszható lenne.
 *
 * A játék menete:
 * - új játék: üres tábla, X kezd;
 * - a lépő játékos egy oszlopot választ, a jele az oszlop legalsó üres
 *   mezőjére esik; tele oszlopba nem lehet lépni (canDrop);
 * - ha a lépéssel négy egymás melletti jele lett vízszintesen, függőlegesen
 *   vagy átlósan,
 *   nyert, és vége a játéknak;
 * - ha a tábla betelt, döntetlen, és vége a játéknak;
 * - különben a másik játékos következik;
 * - a játék vége után nincs több lépés, csak új játék.
 */
public class Game {

    /** Az új játékot mindig ez a játékos kezdi. */
    public static final Player FIRST_PLAYER = Player.X;

    private int rows;
    private int columns;
    private Board board;
    /** A soron következő játékos; a játék vége után az utoljára lépő. */
    private Player currentPlayer;
    /** A győztes, vagy null (tart a játék, vagy döntetlen). */
    private Player winner;
    private boolean over;

    /**
     * Új játék a megadott méretű táblán.
     *
     * @param size a tábla mérete
     */
    public Game(BoardSize size) {
        // A privát változatot hívjuk, nem a publikus newGame(size)-t: a
        // konstruktorból felülírható metódust hívni nem biztonságos (a
        // fordító figyelmeztet is rá).
        newGame(size.getRows(), size.getColumns());
    }

    /**
     * Új játék tetszőleges méretű táblán. Csomagszintű: a teszteknek van, ahol
     * kis táblán könnyebb pl. döntetlent kirakni; a menü csak a BoardSize
     * méreteit kínálja.
     *
     * @param rows a sorok száma
     * @param columns az oszlopok száma
     */
    Game(int rows, int columns) {
        newGame(rows, columns);
    }

    /**
     * Új játék a megadott méretű táblán: üres tábla, X kezd.
     *
     * @param size a tábla mérete
     */
    public void newGame(BoardSize size) {
        newGame(size.getRows(), size.getColumns());
    }

    /**
     * Új játék ugyanakkora táblán, mint az előző. A játék vége után ezt
     * hívja a nézet (automatikus új játék).
     */
    public void newGame() {
        newGame(rows, columns);
    }

    private void newGame(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        board = new Board(rows, columns);
        currentPlayer = FIRST_PLAYER;
        winner = null;
        over = false;
    }

    /**
     * @param col az oszlop
     * @return igaz, ha a soron következő játékos léphet az oszlopba: tart még
     * a játék, és az oszlop nem telt meg
     * @throws IllegalArgumentException ha az oszlop a táblán kívül van
     */
    public boolean canDrop(int col) {
        return !over && !board.isColumnFull(col);
    }

    /**
     * A soron következő játékos lépése: a jele az oszlop legalsó üres
     * mezőjére esik. Utána vagy vége a játéknak (győzelem vagy döntetlen),
     * vagy a másik játékos következik.
     *
     * @param col az oszlop
     * @return a mező, ahova a jel esett. Csak ez az egy mező változott: a
     * nézetnek elég ezt az egy gombot frissítenie.
     * @throws IllegalStateException ha a lépés nem tehető meg: vége a
     * játéknak, vagy az oszlop tele van (ld. canDrop)
     * @throws IllegalArgumentException ha az oszlop a táblán kívül van
     */
    public Position drop(int col) {
        if (over) {
            throw new IllegalStateException("Vége a játéknak, nincs több lépés");
        }
        Position landed = board.drop(col, currentPlayer);
        if (board.hasLineThrough(landed)) {
            winner = currentPlayer;
            over = true;
        } else if (board.isFull()) {
            // Döntetlen: a tábla betelt, és az utolsó lépés sem nyert.
            over = true;
        } else {
            currentPlayer = currentPlayer.other();
        }
        return landed;
    }

    /**
     * @return igaz, ha a játék véget ért (győzelem vagy döntetlen)
     */
    public boolean isOver() {
        return over;
    }

    /**
     * @return igaz, ha a játék döntetlennel ért véget
     */
    public boolean isDraw() {
        return over && winner == null;
    }

    /**
     * @return a győztes, vagy null, ha tart a játék vagy döntetlen lett
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * @return a soron következő játékos (a játék vége után az utoljára lépő)
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * @return a tábla; a nézet ebből rajzolja a mezőket
     */
    public Board getBoard() {
        return board;
    }

}
