package sort2;

import java.util.Comparator;

/**
 * Egy tetszőleges Comparator megfordítása: amit az eredeti előrébb tesz, azt
 * ez hátrébb.
 *
 * A gyakorlás kedvéért írjuk meg; a könyvtárban ez készen van:
 * comparator.reversed() vagy Collections.reverseOrder(comparator).
 *
 * Az osztály fejlécének generikus részei:
 * <ul>
 * <li>{@code ReverseComparator<T>}: az osztály neve utáni {@code <T>}
 * típusváltozót vezet be, ettől lesz az osztály generikus. Hogy T konkrétan
 * mi, az példányosításkor dől el: {@code new ReverseComparator<String>(...)},
 * vagy gyémánttal {@code new ReverseComparator<>(...)}, ha a fordító ki
 * tudja találni.</li>
 * <li>{@code implements Comparator<T>}: ugyanezt a T-t adjuk tovább a
 * Comparator-nak, ezért lesz a metódusunk compare(T, T).</li>
 * </ul>
 *
 * @param <T> az összehasonlított elemek típusa
 */
public class ReverseComparator<T> implements Comparator<T> {

    // Comparator<? super T> darabonként:
    //   Comparator<...>  összehasonlító, a < > között az, hogy miket tud
    //                    összehasonlítani;
    //   ?                valamilyen típus, amelyet nem nevezünk meg;
    //   super T          megkötés a ?-re: csak T vagy T valamelyik őse lehet.
    // Vagyis bármi jó, ami T-ket össze tud hasonlítani - akkor is, ha ennél
    // többet tud. Ha T = String, akkor egy Comparator<Object> is megfelel:
    // aki bármilyen két objektumot össze tud hasonlítani, az két String-et
    // is. Sima Comparator<T>-vel ezt a fordító nem engedné, mert a
    // Comparator<Object> és a Comparator<String> két független típus.
    private final Comparator<? super T> original;

    public ReverseComparator(Comparator<? super T> original) {
        this.original = original;
    }

    @Override
    public int compare(T t1, T t2) {
        // A megfordítás a két paraméter felcserélése. Az eredmény negálása
        // (-original.compare(t1, t2)) csábító, de hibás: ha az eredeti
        // Integer.MIN_VALUE-t ad, annak a negáltja is Integer.MIN_VALUE.
        return original.compare(t2, t1);
    }

}
