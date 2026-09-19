package sort2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Sort2 {

    /**
     * Helyben rendezi a listát három szakaszban: az [i, j] részt (mindkét
     * határ beleértve) a comparator szerint, az előtte és az utána lévő
     * részt pedig külön-külön, ellentétes irányban.
     *
     * Visszatérési érték nincs (void): ez jelzi, hogy a metódus a kapott
     * listát módosítja, nem újat készít.
     *
     * A szignatúra generikus részei (bővebben a README-ben):
     * <ul>
     * <li>{@code <T>} a visszatérési típus előtt: bevezeti a T típusváltozót,
     * "legyen T egy tetszőleges típus". Híváskor nem kell kiírni, a fordító a
     * lista típusából kitalálja: {@code List<Integer>} esetén T = Integer.</li>
     * <li>{@code List<T>}: T típusú elemek listája.</li>
     * <li>{@code Comparator<? super T>}: a {@code ?} egy meg nem nevezett
     * típus, a {@code super T} pedig megköti, hogy ez csak T vagy T valamelyik
     * őse lehet. Vagyis: olyan összehasonlító, amely T-ket, vagy annál
     * általánosabb dolgokat tud összehasonlítani. Integer-ek listájához így
     * nem csak {@code Comparator<Integer>} adható át, hanem
     * {@code Comparator<Number>} vagy {@code Comparator<Object>} is - hiszen
     * aki bármilyen két objektumot össze tud hasonlítani, az két Integer-t
     * is. Sima {@code Comparator<T>} paraméterrel a fordító ezeket
     * elutasítaná.</li>
     * </ul>
     *
     * T-re nincs megkötés (nem kell Comparable-nek lennie), mert a rendezés
     * szempontját teljes egészében a kapott comparator adja.
     *
     * @throws IndexOutOfBoundsException ha nem teljesül:
     * {@code 0 <= i <= j < list.size()}
     */
    public static <T> void sort(List<T> list, int i, int j, Comparator<? super T> comparator) {
        // Előre ellenőrzünk: a subList is dobna kivételt rossz indexre, de
        // addigra a lista egy részét már átrendeztük volna. Így hiba esetén
        // a lista érintetlen marad.
        if (i < 0 || i > j || j >= list.size()) {
            throw new IndexOutOfBoundsException(
                    "required: 0 <= i <= j < size, got i=" + i + ", j=" + j + ", size=" + list.size());
        }
        // Az üres < > ("gyémánt") helyére a fordító a bal oldalból kitalálja a
        // típust: new ReverseComparator<T>(comparator).
        Comparator<T> reversed = new ReverseComparator<>(comparator);
        // A subList(tol, ig) nem másolat, hanem "nézet" az eredeti lista egy
        // szakaszára (tol beleértve, ig már nem): amit a nézeten rendezünk,
        // az az eredeti listában rendeződik. Ezért nem kell semmit
        // visszamásolni. Üres szakasz (pl. i == 0) sem gond.
        //
        // A három sorban ugyanaz a rendező algoritmus fut, amely nem ismer
        // "növekvőt" és "csökkenőt": csak azt kérdezgeti a kapott
        // Comparator-tól, hogy két elem közül melyik való előrébb. A
        // reversed mindenre az ellenkezőjét feleli, mint a comparator, ezért
        // lesz a két szélső szakasz sorrendje fordított. Ha a hívó eleve
        // csökkenő comparatort ad, akkor a közepe csökkenő, a szélei növekvők.
        list.subList(0, i).sort(reversed);
        list.subList(i, j + 1).sort(comparator);
        list.subList(j + 1, list.size()).sort(reversed);
    }

    public static void main(String[] args) {
        // A README példája. Itt módosítható lista kell, ezért csomagoljuk az
        // Arrays.asList eredményét ArrayList-be.
        List<Integer> numbers = new ArrayList<>(Arrays.asList(5, 2, 8, 1, 9, 3, 7));
        System.out.println("Before: " + numbers);
        // Comparator.naturalOrder(): a típus saját (Comparable) rendezése
        // Comparator formájában - Integer-nél a nagyság szerinti növekvő.
        // Ez is generikus metódus; hogy itt Comparator<Integer> kell, azt a
        // fordító a környezetből találja ki. Kiírva így nézne ki:
        // Comparator.<Integer>naturalOrder()
        sort(numbers, 2, 4, Comparator.naturalOrder());
        System.out.println("After:  " + numbers);

        // Más szempont, más típus: String-ek a kis- és nagybetűk
        // megkülönböztetése nélkül. (A String természetes rendezésében
        // minden nagybetű megelőzi a kisbetűket.)
        List<String> words = new ArrayList<>(Arrays.asList("pear", "Apple", "fig", "Banana", "cherry", "Date", "kiwi"));
        System.out.println("Before: " + words);
        sort(words, 2, 4, String.CASE_INSENSITIVE_ORDER);
        System.out.println("After:  " + words);

        // A "? super T" a gyakorlatban: a TextLengthComparator egy
        // Comparator<Object>, mégis átadható List<Integer>-hez (T = Integer)
        // és List<String>-hez (T = String) is, mert az Object mindkettő őse.
        List<Integer> lengths = new ArrayList<>(Arrays.asList(100, 7, 25, 3000, 1, 42, 999));
        System.out.println("Before: " + lengths);
        sort(lengths, 2, 4, new TextLengthComparator());
        System.out.println("After:  " + lengths);
        sort(words, 2, 4, new TextLengthComparator());
        System.out.println("Words by length: " + words);
    }

}
