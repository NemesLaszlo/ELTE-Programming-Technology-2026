package vehicles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A gépjárművek közös ősosztálya.
 *
 * Az osztály absztrakt, ezért közvetlenül nem példányosítható: csak a
 * leszármazottaiból ({@link Car}, {@link Bus}, {@link Truck}) hozható létre
 * objektum.
 *
 * @author bli
 */
public abstract class Vehicle {

    // Az adattagok privátak, kívülről csak a metódusokon keresztül érhetők el.
    private final String plate;
    private final String category;
    private final List<Integer> refuels = new ArrayList<>();

    /**
     * @param plate a gépjármű rendszáma, ez azonosítja a járművet
     * @param category a kategória betűjele (S, B vagy T)
     * @throws IllegalArgumentException ha a rendszám hiányzik vagy üres
     */
    protected Vehicle(String plate, String category) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("The plate must not be empty");
        }
        this.plate = plate;
        this.category = category;
    }

    /**
     * Új tankolás rögzítése.
     *
     * @param amount a tankolt mennyiség, pozitív szám
     * @throws IllegalArgumentException ha a mennyiség nem pozitív
     */
    public void addRefuel(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The refuel amount must be positive: " + amount);
        }
        refuels.add(amount);
    }

    /**
     * @return az összes tankolt mennyiség
     */
    public int sumRefuels() {
        int sum = 0;
        for (int refuel : refuels) {
            sum += refuel;
        }
        return sum;
    }

    /**
     * @return a tankolások száma
     */
    public int numRefuels() {
        return refuels.size();
    }

    /**
     * @return a tankolások átlagos mennyisége; ha még nem volt tankolás, akkor
     * 0 (így elkerüljük a nullával való osztást)
     */
    public double meanRefuels() {
        if (refuels.isEmpty()) {
            return 0;
        }
        return (double) sumRefuels() / numRefuels();
    }

    public String getPlate() {
        return plate;
    }

    public String getCategory() {
        return category;
    }

    /**
     * @return a tankolások csak olvasható listája, így a hívó nem tudja
     * megkerülni az {@link #addRefuel(int)} ellenőrzését
     */
    public List<Integer> getRefuels() {
        return Collections.unmodifiableList(refuels);
    }

    @Override
    public String toString() {
        return "Vehicle{" + "plate=" + plate + ", category=" + category + ", refuels=" + refuels + '}';
    }

}
