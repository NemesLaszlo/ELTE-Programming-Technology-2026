package sort1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sort1 {

    /**
     * Kétparaméteres változat: növekvően rendez.
     *
     * Túlterhelés (overloading): azonos név, eltérő paraméterlista. A
     * Javában nincs alapértelmezett paraméterérték, azt így szokás pótolni:
     * a rövidebb változat továbbhív a teljesre, a logika egy helyen marad.
     */
    public static List<Integer> getSortedByNRemainder(List<Integer> list, final int n) {
        return getSortedByNRemainder(list, n, true);
    }

    /**
     * A lista rendezett MÁSOLATÁT adja vissza, az eredeti nem változik.
     *
     * {@code List<Integer>}: egész számok listája. A {@code < >} közé írt
     * típusparaméter nem lehet primitív, ezért Integer és nem int; a kettő
     * közti átalakítást a fordító elvégzi (autoboxing). Ez a metódus nem
     * generikus: az osztási maradéknak csak egész számokra van értelme.
     *
     * A paraméterek final jelzője csak annyit jelent, hogy a metóduson belül
     * nem kaphatnak új értéket; a hívó szempontjából nincs jelentősége.
     *
     * @throws IllegalArgumentException ha n nem pozitív
     */
    public static List<Integer> getSortedByNRemainder(List<Integer> list, final int n, final boolean ascending) {
        // A másoló konstruktor új listát épít ugyanazokkal az elemekkel.
        // Maguk az elemek nem másolódnak, de az Integer nem módosítható
        // (immutable), így ebből nem lehet baj.
        List<Integer> listCopy = new ArrayList<>(list);
        // A sort helyben rendez a megadott Comparator szerint. Stabil
        // rendezés: az azonos maradékú elemek egymáshoz képest megtartják az
        // eredeti sorrendjüket (csökkenő rendezésnél is).
        // (Ugyanezt csinálja a régebbi Collections.sort(lista, comparator).)
        listCopy.sort(new RemainderComparator(n, ascending));
        return listCopy;
    }

    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 10, 21, 12, 34, 5, -1, -5);
        System.out.println("Original:          " + list);
        System.out.println("By n=3 ascending:  " + getSortedByNRemainder(list, 3));
        System.out.println("By n=3 descending: " + getSortedByNRemainder(list, 3, false));
        System.out.println("Original is unchanged: " + list);
    }

}
