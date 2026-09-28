package race;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests of parsing and validating the race description.
 */
public class RaceFileReaderTest {

    private static final String SAMPLE = """
            4
            Vandor h 4
            Seta l 7
            Csuszo s 12
            Siklo s 10
            nffeeennf
            """;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static Race parse(String content) throws IOException, InvalidInputException {
        return RaceFileReader.read(new StringReader(content));
    }

    /**
     * Asserts that the content is rejected and that the error message
     * mentions the given text.
     */
    private static void assertInvalid(String content, String expectedInMessage) {
        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> parse(content));
        assertTrue("Unexpected message: " + ex.getMessage(),
                ex.getMessage().contains(expectedInMessage));
    }

    private Path createFile(String content) throws IOException {
        return Files.writeString(folder.newFile().toPath(), content);
    }

    // --- valid input ---

    @Test
    public void sampleOfTheAssignmentIsParsed() throws Exception {
        Race race = parse(SAMPLE);

        assertEquals(4, race.getCreatures().size());
        assertFalse(race.isFinished());

        Creature first = race.getCreatures().get(0);
        assertTrue(first instanceof SandWalker);
        assertEquals("Vandor", first.getName());
        assertEquals(4, first.getWater());

        assertTrue(race.getCreatures().get(1) instanceof Walker);
        assertTrue(race.getCreatures().get(2) instanceof Sponge);
        assertTrue(race.getCreatures().get(3) instanceof Sponge);

        assertEquals(List.of(
                Weather.SUNNY, Weather.CLOUDY, Weather.CLOUDY,
                Weather.RAINY, Weather.RAINY, Weather.RAINY,
                Weather.SUNNY, Weather.SUNNY, Weather.CLOUDY), race.getDays());
    }

    @Test
    public void missingLineEndAtTheEndIsAccepted() throws Exception {
        assertEquals(1, parse("1\nVandor h 4\nnfe").getCreatures().size());
    }

    @Test
    public void windowsLineEndsAreAccepted() throws Exception {
        Race race = parse("1\r\nVandor h 4\r\nnfe\r\n");
        assertEquals(3, race.getDays().size());
    }

    @Test
    public void blankLinesAndExtraSpacesAreIgnored() throws Exception {
        Race race = parse("\n  2  \n\n  Vandor   h\t4 \nSeta l 7\n\n nfe \n\n");
        assertEquals(2, race.getCreatures().size());
        assertEquals(3, race.getDays().size());
    }

    @Test
    public void fileIsRead() throws Exception {
        Race race = RaceFileReader.read(createFile(SAMPLE));
        assertEquals(4, race.getCreatures().size());
    }

    @Test
    public void accentedNamesAreReadFromFile() throws Exception {
        Race race = RaceFileReader.read(createFile("1\nVándor h 4\nn\n"));
        assertEquals("Vándor", race.getCreatures().get(0).getName());
    }

    // --- problems with the file ---

    @Test
    public void missingFileIsReported() {
        Path missing = folder.getRoot().toPath().resolve("missing.txt");
        assertThrows(NoSuchFileException.class, () -> RaceFileReader.read(missing));
    }

    // --- invalid content ---

    @Test
    public void emptyInputIsRejected() {
        assertInvalid("", "expected the number of creatures");
        assertInvalid("\n  \n", "expected the number of creatures");
    }

    @Test
    public void countMustBeANumber() {
        assertInvalid("four\nVandor h 4\nn\n", "Line 1: Expected a whole number, but found: four");
        assertInvalid("Vandor h 4\nn\n", "Line 1: Expected a whole number, but found: Vandor");
        assertInvalid("1 2\nVandor h 4\nn\n", "Line 1: More data than expected: 2");
    }

    @Test
    public void countMustBePositive() {
        assertInvalid("0\nn\n", "at least 1");
        assertInvalid("-1\nn\n", "at least 1");
    }

    @Test
    public void fewerCreaturesThanDeclaredAreRejected() {
        // The days are found where the third creature is expected.
        assertInvalid("3\nVandor h 4\nSeta l 7\nnfe\n", "Line 4");
        assertInvalid("3\nVandor h 4\nSeta l 7\n", "expected a creature");
    }

    @Test
    public void moreCreaturesThanDeclaredAreRejected() {
        // The second creature is found where the days are expected.
        assertInvalid("1\nVandor h 4\nSeta l 7\nnfe\n", "Line 3");
    }

    @Test
    public void missingDaysAreRejected() {
        assertInvalid("1\nVandor h 4\n", "expected the days of the race");
    }

    @Test
    public void creatureLineMustHaveThreeFields() {
        assertInvalid("1\nVandor h\nnfe\n", "Line 2: Data is missing");
        assertInvalid("1\nVandor h 4 5\nnfe\n", "Line 2: More data than expected: 5");
    }

    @Test
    public void unknownSpeciesIsRejected() {
        assertInvalid("1\nVandor x 4\nnfe\n", "Line 2: Unknown species code: x");
        assertInvalid("1\nVandor hs 4\nnfe\n", "Unknown species code: hs");
        assertInvalid("1\nVandor H 4\nnfe\n", "Unknown species code: H");
    }

    @Test
    public void waterMustBeANumber() {
        assertInvalid("1\nVandor h four\nnfe\n", "Line 2: Expected a whole number, but found: four");
        assertInvalid("1\nVandor h 4.5\nnfe\n", "Line 2: Expected a whole number, but found: 4.5");
    }

    @Test
    public void waterMustBePositive() {
        assertInvalid("1\nVandor h 0\nnfe\n", "must be positive");
        assertInvalid("1\nVandor h -2\nnfe\n", "must be positive");
    }

    @Test
    public void waterMustNotExceedTheCapacity() {
        assertInvalid("1\nVandor h 9\nnfe\n", "exceeds the capacity");
        assertInvalid("1\nSeta l 13\nnfe\n", "exceeds the capacity");
        assertInvalid("1\nCsuszo s 21\nnfe\n", "exceeds the capacity");
    }

    @Test
    public void duplicateNamesAreRejected() {
        assertInvalid("2\nVandor h 4\nVandor l 7\nnfe\n", "Duplicate creature name: Vandor");
    }

    @Test
    public void invalidDayCodeIsRejected() {
        assertInvalid("1\nVandor h 4\nnfxe\n", "Line 3: Unknown weather code: 'x'");
        assertInvalid("1\nVandor h 4\nnf e\n", "Line 3: More data than expected: e");
        assertInvalid("1\nVandor h 4\nNFE\n", "Unknown weather code: 'N'");
    }

    @Test
    public void contentAfterTheDaysIsRejected() {
        assertInvalid("1\nVandor h 4\nnfe\nSeta l 7\n", "Line 4");
    }

}
