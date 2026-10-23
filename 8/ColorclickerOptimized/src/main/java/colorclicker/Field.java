package colorclicker;

/**
 * A tábla egy mezője. A modell része.
 *
 * A mező vagy üres, vagy egy kattintás kiszínezte: ekkor tároljuk a kattintás
 * sorszámát (1-től) és a színét. A szín egy 0xRRGGBB alakú egész szám, mint
 * a CSS-ben a #RRGGBB (pl. 0xFF8800 = narancs), és nem java.awt.Color: a
 * modell nem használ AWT-t vagy Swinget, a nézet csinál belőle Color-t
 * (new Color(rgb)). Így a modell ablak nélkül, JUnit-tal tesztelhető.
 *
 * A mezőt csak a Board színezheti ki (a color metódus csomagszintű), és csak
 * egyszer: a színezés a játék szabálya, nem a mezőé.
 *
 * @author pinter
 */
public class Field {

    /** Az üres mező sorszáma. A kattintásokat 1-től számozzuk. */
    public static final int EMPTY = 0;

    private int number = EMPTY;
    private int rgb;

    /**
     * @return igaz, ha a mezőt még nem színezte ki kattintás
     */
    public boolean isEmpty() {
        return number == EMPTY;
    }

    /**
     * @return hányadik kattintás színezte ki a mezőt (1-től), vagy EMPTY (0),
     * ha a mező üres
     */
    public int getNumber() {
        return number;
    }

    /**
     * @return a mező színe 0xRRGGBB alakban
     * @throws IllegalStateException ha a mező üres
     */
    public int getRgb() {
        if (isEmpty()) {
            throw new IllegalStateException("Az üres mezőnek nincs színe");
        }
        return rgb;
    }

    /**
     * Kiszínezi a mezőt. Csomagszintű: csak a Board hívja, a click részeként.
     *
     * @param number a kattintás sorszáma (1-től)
     * @param rgb a szín 0xRRGGBB alakban
     * @throws IllegalStateException ha a mező már színes
     * @throws IllegalArgumentException ha a sorszám nem pozitív, vagy a szín
     * nem fér el 24 biten
     */
    void color(int number, int rgb) {
        if (!isEmpty()) {
            throw new IllegalStateException("A mező már színes: " + this.number + ". kattintás");
        }
        if (number <= EMPTY) {
            throw new IllegalArgumentException("A kattintás sorszáma 1-től indul: " + number);
        }
        if (rgb < 0 || rgb > 0xFFFFFF) {
            throw new IllegalArgumentException("A szín 0x000000 és 0xFFFFFF közé esik: " + Integer.toHexString(rgb));
        }
        this.number = number;
        this.rgb = rgb;
    }

}
