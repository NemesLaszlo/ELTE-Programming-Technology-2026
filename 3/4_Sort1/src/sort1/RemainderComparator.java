package sort1;

import java.util.Comparator;

/**
 * Egész számokat hasonlít össze az n-nel vett osztási maradékuk alapján.
 *
 * A Comparator egy "kívülről megadott" rendezési szempont. Akkor kell, ha a
 * típus természetes rendezése (Comparable, Integer-nél a nagyság szerinti)
 * nem az, ami szerint rendezni szeretnénk.
 *
 * A compare(a, b) eredménye: negatív, ha a előrébb való, mint b; nulla, ha a
 * rendezés szempontjából egyformák; pozitív, ha a hátrébb való.
 *
 * {@code implements Comparator<Integer>}: a Comparator generikus interfész,
 * a {@code < >} között adjuk meg, hogy milyen típusú elemeket hasonlítunk
 * össze. Mivel itt Integer-t rögzítettünk, a megvalósítandó metódus
 * compare(Integer, Integer) lesz. (Maga a RemainderComparator ettől még nem
 * generikus osztály: nincs saját típusváltozója, csak Integer-ekre jó.)
 */
public class RemainderComparator implements Comparator<Integer> {

    private final int n;
    private final boolean ascending;

    /**
     * @throws IllegalArgumentException ha n nem pozitív (0-val nem lehet
     * osztani, negatív osztóval pedig a maradék fogalma nem egyértelmű)
     */
    public RemainderComparator(int n, boolean ascending) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive: " + n);
        }
        this.n = n;
        this.ascending = ascending;
    }

    @Override
    public int compare(Integer i1, Integer i2) {
        // Math.floorMod és nem %: a % negatív számra negatív eredményt ad
        // (-1 % 3 == -1), a floorMod a matematikai maradékot (floorMod(-1, 3)
        // == 2), ami mindig 0 és n-1 közé esik.
        int r1 = Math.floorMod(i1, n);
        int r2 = Math.floorMod(i2, n);
        // Integer.compare és nem r1 - r2: a kivonásos trükk nagy abszolút
        // értékű számoknál túlcsordulhat, és rossz előjelet adhat. Itt a
        // maradékok kicsik, de jobb eleve a biztos megoldást megszokni.
        // Csökkenő sorrendhez egyszerűen felcseréljük a két oldalt.
        return ascending ? Integer.compare(r1, r2) : Integer.compare(r2, r1);
    }

}
