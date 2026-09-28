package animalfarm;

/**
 * Kecske.
 *
 * @author pinter
 */
public class Goat extends Animal {

    /** A kecske betűjele a bemeneti fájlban. */
    public static final String KIND = "K";

    // Statikus konstans, mert a küszöb a fajtára jellemző, nem az egyes állatokra.
    private static final int MALNOURISHED_THRESHOLD = 12;

    /**
     * @param name a kecske neve
     * @param weight a kecske súlya kilogrammban
     */
    public Goat(String name, int weight) {
        super(name, weight, KIND);
    }

    @Override
    public int getMalnourishedThreshold() {
        return MALNOURISHED_THRESHOLD;
    }

}
