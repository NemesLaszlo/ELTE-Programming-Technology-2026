package race;

/**
 * The kinds of weather a race day can have.
 *
 * <p>Each constant knows the one-letter code that represents it in the input
 * file, so the mapping between the file format and the model lives in a single
 * place.</p>
 */
public enum Weather {

    /** A sunny day, written as {@code n} ("napos") in the input file. */
    SUNNY('n'),
    /** A cloudy day, written as {@code f} ("felhős") in the input file. */
    CLOUDY('f'),
    /** A rainy day, written as {@code e} ("esős") in the input file. */
    RAINY('e');

    private final char code;

    Weather(char code) {
        this.code = code;
    }

    /**
     * Returns the one-letter code of this weather used in the input file.
     *
     * @return the code character
     */
    public char getCode() {
        return code;
    }

    /**
     * Looks up the weather that belongs to a code character.
     *
     * @param code the code character read from the input file
     * @return the matching weather
     * @throws IllegalArgumentException if no weather has the given code
     */
    public static Weather fromCode(char code) {
        for (Weather weather : values()) {
            if (weather.code == code) {
                return weather;
            }
        }
        throw new IllegalArgumentException("Unknown weather code: '" + code + "'");
    }

}
