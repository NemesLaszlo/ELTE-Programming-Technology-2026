package race;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests of the mapping between the day codes and {@link Weather}.
 */
public class WeatherTest {

    @Test
    public void codesAreMappedToWeather() {
        assertEquals(Weather.SUNNY, Weather.fromCode('n'));
        assertEquals(Weather.CLOUDY, Weather.fromCode('f'));
        assertEquals(Weather.RAINY, Weather.fromCode('e'));
    }

    @Test
    public void everyWeatherIsFoundByItsOwnCode() {
        for (Weather weather : Weather.values()) {
            assertEquals(weather, Weather.fromCode(weather.getCode()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownCodeIsRejected() {
        Weather.fromCode('x');
    }

    @Test(expected = IllegalArgumentException.class)
    public void codesAreCaseSensitive() {
        Weather.fromCode('N');
    }

}
