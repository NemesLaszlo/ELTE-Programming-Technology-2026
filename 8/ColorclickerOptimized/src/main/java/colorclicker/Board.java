package colorclicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A Color clicker MODELLJE (az MV "M" betűje): a tábla, a kattintások száma,
 * az eltelt idő, és a játék szabályai.
 *
 * A modell semmit nem tud a felületről, nem használ Swinget vagy AWT-t. A GUI
 * eseménykezelője hívja a click metódust, majd a visszakapott lista alapján
 * CSAK A VÁLTOZOTT gombokat frissíti (ld. BoardGUI.click). Mivel nincs benne
 * Swing, felület nélkül, JUnit-tal tesztelhető (ld. test/.../BoardTest).
 *
 * A játék szabályai:
 * - a tábla size × size mező, induláskor mind üres;
 * - egy üres mezőre kattintva a mező, és még legfeljebb EXTRA_FIELDS véletlen
 *   üres mező ugyanazt a véletlen színt és a kattintás sorszámát kapja;
 * - már színes mezőre kattintani nem lehet (canClick hamis);
 * - a játék akkor ér véget, amikor minden mező színes; ekkor az óra megáll.
 *
 * A kiinduló változathoz képest a szabályok a BoardGUI eseménykezelőjéből
 * kerültek ide, és a click metódus visszaadja, mely mezők változtak: ez
 * teszi lehetővé az optimalizált frissítést.
 *
 * @author pinter
 */
public class Board {

    /** Egy kattintásra ennyi további üres mező színeződik ki (ha van annyi). */
    public static final int EXTRA_FIELDS = 4;
    /** A színek száma: a 0xRRGGBB értékek 0-tól eddig (kizárva) terjednek. */
    private static final int COLOR_COUNT = 0x1000000;

    private final int size;
    /** A mezők: fields[sor][oszlop]. */
    private final Field[][] fields;
    /** A véletlen színek és mezők forrása; a tesztek rögzített maggal adják. */
    private final Random random;
    /** Az eddigi kattintások száma; az utolsó kattintás sorszáma. */
    private int clickCount;
    /** Az üres mezők száma; ha 0, vége a játéknak. */
    private int emptyCount;
    /** A játék kezdete (System.currentTimeMillis). */
    private final long startTime;
    /** A játék vége, vagy -1, amíg tart a játék. */
    private long endTime = -1;

    /**
     * Új, üres tábla, véletlen színekkel és mezőkkel.
     *
     * @param size a tábla mérete (sorok = oszlopok száma), legalább 1
     */
    public Board(int size) {
        this(size, new Random());
    }

    /**
     * Új, üres tábla a megadott véletlenszám-generátorral. Rögzített maggal
     * (new Random(42)) a játék menete kiszámítható, ezt használják a tesztek.
     *
     * @param size a tábla mérete, legalább 1
     * @param random a véletlen színek és mezők forrása
     */
    public Board(int size, Random random) {
        if (size < 1) {
            throw new IllegalArgumentException("A tábla mérete legalább 1: " + size);
        }
        this.size = size;
        this.random = random;
        fields = new Field[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                fields[row][col] = new Field();
            }
        }
        emptyCount = size * size;
        startTime = System.currentTimeMillis();
    }

    /**
     * @param row sor
     * @param col oszlop
     * @return igaz, ha a mezőre lehet kattintani: tart még a játék, és a mező
     * üres
     * @throws IllegalArgumentException ha a hely a táblán kívül van
     */
    public boolean canClick(int row, int col) {
        return !isOver() && get(row, col).isEmpty();
    }

    /**
     * Kattintás egy üres mezőre. A mező, és még legfeljebb EXTRA_FIELDS
     * véletlen üres mező ugyanazt a véletlen színt és a kattintás sorszámát
     * kapja. Ha az utolsó üres mező is színes lett, a játék véget ér.
     *
     * @param row a mező sora
     * @param col a mező oszlopa
     * @return a kiszínezett mezők helye: elsőként a kattintott mező, utána a
     * véletlen mezők. Ezek, és csak ezek a mezők változtak, a nézetnek elég
     * ezeket frissítenie.
     * @throws IllegalStateException ha a mezőre nem lehet kattintani (már
     * színes, vagy vége a játéknak), ld. canClick
     * @throws IllegalArgumentException ha a hely a táblán kívül van
     */
    public List<Position> click(int row, int col) {
        if (!canClick(row, col)) {
            throw new IllegalStateException("Erre a mezőre nem lehet kattintani: (" + row + ", " + col + ")");
        }
        clickCount++;
        int rgb = random.nextInt(COLOR_COUNT);

        // A kattintott mező, majd a többi üres mező véletlen sorrendben: az
        // első EXTRA_FIELDS darab (vagy amennyi van) kap színt.
        List<Position> changed = new ArrayList<>();
        changed.add(new Position(row, col));
        List<Position> empty = emptyPositions();
        empty.remove(new Position(row, col));
        Collections.shuffle(empty, random);
        changed.addAll(empty.subList(0, Math.min(EXTRA_FIELDS, empty.size())));

        for (Position position : changed) {
            get(position).color(clickCount, rgb);
            emptyCount--;
        }
        if (emptyCount == 0) {
            endTime = System.currentTimeMillis();
        }
        return changed;
    }

    /**
     * @return igaz, ha minden mező színes: vége a játéknak
     */
    public boolean isOver() {
        return emptyCount == 0;
    }

    /**
     * @return a játék kezdete óta eltelt idő ezredmásodpercben; a játék vége
     * után már nem változik
     */
    public long getElapsedMillis() {
        long end = isOver() ? endTime : System.currentTimeMillis();
        return end - startTime;
    }

    /**
     * @param row sor
     * @param col oszlop
     * @return a mező
     * @throws IllegalArgumentException ha a hely a táblán kívül van
     */
    public Field get(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IllegalArgumentException("A táblán kívüli hely: (" + row + ", " + col + ")");
        }
        return fields[row][col];
    }

    /**
     * @param position a mező helye
     * @return a mező
     * @throws IllegalArgumentException ha a hely a táblán kívül van
     */
    public Field get(Position position) {
        return get(position.row(), position.col());
    }

    /**
     * @return a tábla mérete (sorok = oszlopok száma)
     */
    public int getSize() {
        return size;
    }

    /**
     * @return az eddigi kattintások száma
     */
    public int getClickCount() {
        return clickCount;
    }

    /**
     * @return az üres mezők száma
     */
    public int getEmptyCount() {
        return emptyCount;
    }

    /**
     * @return az üres mezők helye, sorfolytonosan
     */
    public List<Position> emptyPositions() {
        List<Position> empty = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (fields[row][col].isEmpty()) {
                    empty.add(new Position(row, col));
                }
            }
        }
        return empty;
    }

}
