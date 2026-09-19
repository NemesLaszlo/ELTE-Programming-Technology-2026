package numboth;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NumBoth {

    /**
     * Hány közös eleme van a két gyűjteménynek? A multiplicitás nem számít:
     * ami mindkettőben többször szerepel, az is csak egyszer.
     *
     * Generikus metódus, a szignatúra részei (bővebben a README-ben):
     * <ul>
     * <li>{@code <T>} a visszatérési típus (int) előtt: bevezeti a T
     * típusváltozót, "legyen T egy tetszőleges típus". E nélkül a fordító
     * egy T nevű osztályt keresne. Így egyetlen metódus működik számokra,
     * szövegekre és bármi másra.</li>
     * <li>{@code Collection<T>} mindkét paraméternél: a két gyűjtemény
     * ugyanolyan típusú elemeket tartalmaz. A Collection a List, a Set stb.
     * közös ősinterfésze, így a metódus bármelyikkel hívható.</li>
     * </ul>
     * Híváskor T-t nem kell kiírni: numBoth(c1, c2) esetén a fordító látja,
     * hogy c1 egy {@code List<Integer>}, tehát T = Integer.
     *
     * Ciklus helyett a gyűjtemények "tömeges" műveleteit használjuk. (Ciklus
     * persze így is fut, csak a könyvtári metódusok belsejében.)
     */
    public static <T> int numBoth(Collection<T> c1, Collection<T> c2) {
        // A HashSet a konstruktorában kapott gyűjtemény elemeit veszi át, és
        // kiszűri az ismétlődéseket. Másolattal dolgozunk, mert a retainAll
        // helyben módosít - a hívó gyűjteményeit nem ronthatjuk el.
        // A T a metódus törzsében is használható típusként: a Set<T>
        // "ugyanolyan elemek halmaza, mint amilyeneket kaptunk".
        Set<T> common = new HashSet<>(c1);
        // c2-ből a helyesség miatt nem kellene halmazt csinálni, a hatékonyság
        // miatt viszont igen: a retainAll minden elemre contains-t hív a
        // paraméterén, ami listán lineáris, HashSet-en átlagosan konstans.
        Set<T> s2 = new HashSet<>(c2);
        // retainAll: csak azok maradnak meg, amelyek s2-ben is benne vannak,
        // vagyis ez a halmazmetszet. (addAll = unió, removeAll = különbség.)
        common.retainAll(s2);
        return common.size();
    }

    public static void main(String[] args) {
        // Az Arrays.asList rögzített méretű listát ad: olvasni lehet, de az
        // add/remove UnsupportedOperationException-t dob. Itt ez elég, hiszen
        // a numBoth nem módosítja a paramétereit.
        List<Integer> c1 = Arrays.asList(1, 2, 2, 3, 3, 4);
        List<Integer> c2 = Arrays.asList(2, 3, 3, 7, 8);
        System.out.println("The collections " + c1 + " and " + c2 + " have "
                + numBoth(c1, c2) + " elements in common not counting duplicates.");
        System.out.println("The originals are unchanged: " + c1 + ", " + c2);
    }

}
