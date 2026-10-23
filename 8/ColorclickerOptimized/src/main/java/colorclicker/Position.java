package colorclicker;

/**
 * A tábla egy mezőjének helye: sor és oszlop, 0-tól számozva. A modell része.
 *
 * A kiinduló változatban erre a java.awt.Point szolgált, ami az AWT osztálya:
 * a modell így a felület könyvtárától függött. Egy saját rekord két egész
 * számmal ugyanezt tudja, és a modell Swing/AWT nélkül marad.
 *
 * Rekord (record): csak adatot tároló osztály rövid írásmódja. A fordító
 * elkészíti a konstruktort, a row() és col() lekérdező metódusokat, valamint
 * az equals, hashCode és toString metódusokat. Az equals miatt két Position
 * akkor egyenlő, ha ugyanazt a mezőt jelöli, ezért használható listában
 * (contains, remove) és a tesztekben (assertEquals).
 *
 * @author pinter
 */
public record Position(int row, int col) {

}
