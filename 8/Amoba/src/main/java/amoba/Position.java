package amoba;

/**
 * A tábla egy mezőjének helye: sor és oszlop, 0-tól számozva. A 0. sor a
 * tábla teteje, a jelek lefelé, a nagyobb sorszámok felé potyognak. A modell
 * része.
 *
 * Rekord (record): csak adatot tároló osztály rövid írásmódja; a fordító
 * készíti a konstruktort, a row() és col() lekérdezőket, az equals,
 * hashCode és toString metódusokat. Az equals miatt a tesztekben
 * assertEquals-szal hasonlítható.
 */
public record Position(int row, int col) {

}
