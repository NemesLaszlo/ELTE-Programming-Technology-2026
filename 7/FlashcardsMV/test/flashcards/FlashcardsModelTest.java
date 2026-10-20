package flashcards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * A modell tesztjei. Nincs bennük Swing: a gyakorlás szabályait ablak és
 * kattintgatás nélkül ellenőrizzük. A Flashcards (nem MV) projektben ezek a
 * tesztek nem lennének megírhatók, mert ott a szabályok a GUI-ban vannak.
 */
public class FlashcardsModelTest {

    private static final List<Card> CARDS = List.of(
            new Card("Q1", "A1"),
            new Card("Q2", "A2"),
            new Card("Q3", "A3"));

    /** Ideiglenes mappa a fájlos tesztekhez; a teszt után törlődik. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private FlashcardsModel model;

    @Before
    public void setUp() {
        model = new FlashcardsModel();
    }

    private Path fileWith(String content) throws IOException {
        Path file = folder.newFile("cards.txt").toPath();
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    // ----- Állapot kártyák nélkül -----

    @Test
    public void newModelHasNoCards() {
        assertFalse(model.hasCards());
        assertFalse(model.hasCurrentCard());
        assertFalse(model.isFinished());
        assertEquals(0, model.getCardCount());
        assertEquals(0, model.getAnsweredCount());
        assertEquals(0, model.getScore());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotToggleWithoutCards() {
        model.toggle();
    }

    @Test(expected = IllegalStateException.class)
    public void cannotAnswerWithoutCards() {
        model.answer(true);
    }

    // ----- A gyakorlás menete -----

    @Test
    public void loadStartsWithTheQuestionOfTheFirstCard() {
        model.load(CARDS);

        assertTrue(model.hasCards());
        assertTrue(model.hasCurrentCard());
        assertFalse(model.isFinished());
        assertEquals(3, model.getCardCount());
        assertEquals(new Card("Q1", "A1"), model.getCurrentCard());
        assertTrue(model.isShowingQuestion());
        assertEquals("Q1", model.getCurrentText());
    }

    @Test
    public void toggleSwitchesBetweenQuestionAndAnswer() {
        model.load(CARDS);

        model.toggle();
        assertFalse(model.isShowingQuestion());
        assertEquals("A1", model.getCurrentText());

        model.toggle();
        assertTrue(model.isShowingQuestion());
        assertEquals("Q1", model.getCurrentText());
    }

    @Test
    public void goodAnswerScoresAndMovesToTheNextCard() {
        model.load(CARDS);

        model.answer(true);

        assertEquals(1, model.getScore());
        assertEquals(1, model.getAnsweredCount());
        assertEquals("Q2", model.getCurrentText());
    }

    @Test
    public void wrongAnswerMovesOnWithoutAPoint() {
        model.load(CARDS);

        model.answer(false);

        assertEquals(0, model.getScore());
        assertEquals(1, model.getAnsweredCount());
        assertEquals("Q2", model.getCurrentText());
    }

    @Test
    public void theNextCardAlwaysStartsWithItsQuestion() {
        model.load(CARDS);
        model.toggle();                     // az 1. kártya válaszát nézzük

        model.answer(true);

        assertTrue(model.isShowingQuestion());
        assertEquals("Q2", model.getCurrentText());
    }

    @Test
    public void answeringAllCardsFinishesThePractice() {
        model.load(CARDS);

        model.answer(true);
        model.answer(false);
        model.answer(true);

        assertTrue(model.isFinished());
        assertFalse(model.hasCurrentCard());
        assertTrue(model.hasCards());
        assertEquals(3, model.getAnsweredCount());
        assertEquals(2, model.getScore());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotAnswerWhenFinished() {
        model.load(List.of(new Card("Q", "A")));
        model.answer(true);

        model.answer(true);
    }

    @Test
    public void resetStartsOverButKeepsTheCards() {
        model.load(CARDS);
        model.answer(true);
        model.toggle();

        model.reset();

        assertEquals(3, model.getCardCount());
        assertEquals(0, model.getAnsweredCount());
        assertEquals(0, model.getScore());
        assertTrue(model.isShowingQuestion());
        assertEquals("Q1", model.getCurrentText());
    }

    @Test
    public void loadReplacesThePreviousCards() {
        model.load(CARDS);
        model.answer(true);

        model.load(List.of(new Card("X", "Y")));

        assertEquals(1, model.getCardCount());
        assertEquals(0, model.getScore());
        assertEquals("X", model.getCurrentText());
    }

    @Test
    public void theModelKeepsItsOwnCopyOfTheCards() {
        List<Card> cards = new java.util.ArrayList<>(CARDS);
        model.load(cards);

        cards.clear();

        assertEquals(3, model.getCardCount());
    }

    // ----- Fájl beolvasása -----

    @Test
    public void readCardsParsesEveryLine() throws IOException {
        Path file = fileWith("Q1~A1\nQ2~A2\n");

        List<Card> cards = FlashcardsModel.readCards(file);

        assertEquals(List.of(new Card("Q1", "A1"), new Card("Q2", "A2")), cards);
    }

    @Test
    public void readCardsSkipsBlankLinesAndTrimsSpaces() throws IOException {
        Path file = fileWith("\n  Q1 ~ A1  \n\n   \nQ2~A2");

        List<Card> cards = FlashcardsModel.readCards(file);

        assertEquals(List.of(new Card("Q1", "A1"), new Card("Q2", "A2")), cards);
    }

    @Test
    public void readCardsReportsTheMalformedLine() throws IOException {
        Path file = fileWith("Q1~A1\nno separator here\nQ3~A3\n");

        try {
            FlashcardsModel.readCards(file);
            fail("Expected an IOException");
        } catch (IOException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Line 2 "));
        }
    }

    @Test(expected = IOException.class)
    public void readCardsRejectsAMissingAnswer() throws IOException {
        FlashcardsModel.readCards(fileWith("Q1~\n"));
    }

    @Test(expected = IOException.class)
    public void readCardsRejectsAnEmptyFile() throws IOException {
        FlashcardsModel.readCards(fileWith("\n\n"));
    }

    @Test(expected = IOException.class)
    public void readCardsRejectsAMissingFile() throws IOException {
        FlashcardsModel.readCards(folder.getRoot().toPath().resolve("missing.txt"));
    }

    @Test
    public void loadFromFileStartsThePractice() throws IOException {
        model.load(fileWith("Q1~A1\nQ2~A2\n"));

        assertEquals(2, model.getCardCount());
        assertEquals("Q1", model.getCurrentText());
    }

    @Test
    public void aFailedLoadKeepsThePreviousCards() throws IOException {
        model.load(CARDS);
        model.answer(true);

        try {
            model.load(fileWith("broken line"));
            fail("Expected an IOException");
        } catch (IOException ex) {
            // ezt vártuk
        }

        assertEquals(3, model.getCardCount());
        assertEquals(1, model.getAnsweredCount());
        assertEquals("Q2", model.getCurrentText());
    }

    @Test
    public void theSampleFileOfTheProjectCanBeRead() throws IOException {
        // A tesztek a projekt mappájából futnak, ott van a cards.txt.
        List<Card> cards = FlashcardsModel.readCards(Path.of("cards.txt"));

        assertTrue(cards.size() >= 5);
    }

}
