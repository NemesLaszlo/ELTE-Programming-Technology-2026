package vehicles;

/**
 * Személygépkocsi.
 *
 * @author bli
 */
public class Car extends Vehicle {

    /** A személygépkocsi betűjele a bemeneti fájlban. */
    public static final String CATEGORY = "S";

    /**
     * @param plate a személygépkocsi rendszáma
     */
    public Car(String plate) {
        super(plate, CATEGORY);
    }

}
