package vehicles;

/**
 * Teherautó.
 *
 * @author bli
 */
public class Truck extends Vehicle {

    /** A teherautó betűjele a bemeneti fájlban. */
    public static final String CATEGORY = "T";

    /**
     * @param plate a teherautó rendszáma
     */
    public Truck(String plate) {
        super(plate, CATEGORY);
    }

}
