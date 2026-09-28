package vehicles;

/**
 * Busz.
 *
 * @author bli
 */
public class Bus extends Vehicle {

    /** A busz betűjele a bemeneti fájlban. */
    public static final String CATEGORY = "B";

    /**
     * @param plate a busz rendszáma
     */
    public Bus(String plate) {
        super(plate, CATEGORY);
    }

}
