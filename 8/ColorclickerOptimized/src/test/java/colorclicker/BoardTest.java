package colorclicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

/**
 * A modell tesztjei. Nincs bennük Swing: a játék szabályait ablak és
 * kattintgatás nélkül ellenőrizzük. A Colorclicker (kiinduló) projektben
 * ezek a tesztek nem lennének megírhatók, mert ott a szabályok a BoardGUI
 * eseménykezelőjében vannak.
 *
 * A táblák rögzített magú Random-ot kapnak (new Random(42)), így a véletlen
 * színek és mezők minden futtatáskor ugyanazok.
 */
public class BoardTest {

    private static final int SEED = 42;

    private Board board(int size) {
        return new Board(size, new Random(SEED));
    }

    // ----- Az új tábla -----

    @Test
    public void newBoardIsEmpty() {
        Board board = board(3);

        assertEquals(3, board.getSize());
        assertEquals(0, board.getClickCount());
        assertEquals(9, board.getEmptyCount());
        assertEquals(9, board.emptyPositions().size());
        assertFalse(board.isOver());
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                assertTrue(board.get(row, col).isEmpty());
                assertEquals(Field.EMPTY, board.get(row, col).getNumber());
                assertTrue(board.canClick(row, col));
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void sizeMustBePositive() {
        board(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void positionOutsideTheBoardIsRejected() {
        board(3).get(3, 0);
    }

    @Test(expected = IllegalStateException.class)
    public void emptyFieldHasNoColor() {
        board(3).get(0, 0).getRgb();
    }

    // ----- Egy kattintás -----

    @Test
    public void clickColorsTheClickedFieldAndFourMore() {
        Board board = board(10);

        List<Position> changed = board.click(2, 3);

        assertEquals(1 + Board.EXTRA_FIELDS, changed.size());
        assertEquals(new Position(2, 3), changed.get(0));
        assertEquals(1, board.getClickCount());
        assertEquals(100 - 5, board.getEmptyCount());
        assertFalse(board.get(2, 3).isEmpty());
        assertEquals(1, board.get(2, 3).getNumber());
    }

    @Test
    public void theColoredFieldsAreDistinctAndShareTheColorAndTheNumber() {
        Board board = board(10);

        List<Position> changed = board.click(0, 0);

        Set<Position> distinct = new HashSet<>(changed);
        assertEquals(changed.size(), distinct.size());
        int rgb = board.get(0, 0).getRgb();
        for (Position position : changed) {
            assertEquals(1, board.get(position).getNumber());
            assertEquals(rgb, board.get(position).getRgb());
        }
    }

    @Test
    public void exactlyTheReturnedFieldsChange() {
        Board board = board(10);
        board.click(5, 5);
        List<Position> emptyBefore = board.emptyPositions();

        List<Position> changed = board.click(1, 1);

        // A változott mezők: előtte üresek voltak, utána nem.
        for (Position position : changed) {
            assertTrue(emptyBefore.contains(position));
            assertFalse(board.get(position).isEmpty());
        }
        // Minden más mező változatlan: ami üres volt, az üres maradt.
        List<Position> emptyAfter = board.emptyPositions();
        assertEquals(emptyBefore.size() - changed.size(), emptyAfter.size());
        for (Position position : emptyBefore) {
            assertEquals(!changed.contains(position), emptyAfter.contains(position));
        }
    }

    @Test
    public void clicksAreNumberedInOrder() {
        Board board = board(10);

        board.click(0, 0);
        board.click(9, 9);
        board.click(4, 5);

        assertEquals(3, board.getClickCount());
        assertEquals(1, board.get(0, 0).getNumber());
        assertEquals(2, board.get(9, 9).getNumber());
        assertEquals(3, board.get(4, 5).getNumber());
        assertEquals(100 - 15, board.getEmptyCount());
    }

    @Test
    public void eachClickGetsItsOwnColor() {
        Board board = board(10);

        board.click(0, 0);
        board.click(9, 9);

        // Két véletlen szín egyezése elvileg lehetséges, de a rögzített
        // maggal nem fordul elő.
        assertNotEquals(board.get(0, 0).getRgb(), board.get(9, 9).getRgb());
    }

    @Test
    public void colorsFitIn24Bits() {
        Board board = board(10);
        board.click(0, 0);

        int rgb = board.get(0, 0).getRgb();

        assertTrue(rgb >= 0 && rgb <= 0xFFFFFF);
    }

    // ----- Szabálytalan kattintás -----

    @Test
    public void aColoredFieldCannotBeClicked() {
        Board board = board(10);
        board.click(2, 3);

        assertFalse(board.canClick(2, 3));
    }

    @Test(expected = IllegalStateException.class)
    public void clickingAColoredFieldThrows() {
        Board board = board(10);
        board.click(2, 3);

        board.click(2, 3);
    }

    @Test
    public void aFailedClickChangesNothing() {
        Board board = board(10);
        board.click(2, 3);

        try {
            board.click(2, 3);
        } catch (IllegalStateException expected) {
            // ezt vártuk
        }

        assertEquals(1, board.getClickCount());
        assertEquals(95, board.getEmptyCount());
    }

    // ----- A játék vége -----

    @Test
    public void fewerThanFourEmptyFieldsAreAllColored() {
        // 2 × 2-es tábla: egy kattintás a mezőt és a maradék 3-at színezi,
        // nem 4-et. (A kiinduló változat ilyenkor kivételt dobott volna.)
        Board board = board(2);

        List<Position> changed = board.click(0, 0);

        assertEquals(4, changed.size());
        assertEquals(0, board.getEmptyCount());
        assertTrue(board.isOver());
    }

    @Test
    public void theGameEndsWhenEveryFieldIsColored() {
        // 5 × 5 = 25 mező, kattintásonként 5: pontosan 5 kattintás.
        Board board = board(5);
        int clicks = 0;
        while (!board.isOver()) {
            Position next = board.emptyPositions().get(0);
            board.click(next.row(), next.col());
            clicks++;
        }

        assertEquals(5, clicks);
        assertEquals(5, board.getClickCount());
        assertEquals(0, board.getEmptyCount());
        assertTrue(board.emptyPositions().isEmpty());
    }

    @Test
    public void noFieldCanBeClickedAfterTheEnd() {
        Board board = board(2);
        board.click(1, 1);

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                assertFalse(board.canClick(row, col));
            }
        }
    }

    @Test
    public void oneClickOnASingleFieldBoardEndsTheGame() {
        Board board = board(1);

        List<Position> changed = board.click(0, 0);

        assertEquals(List.of(new Position(0, 0)), changed);
        assertTrue(board.isOver());
    }

    // ----- Az idő -----

    @Test
    public void elapsedTimeIsNotNegative() {
        Board board = board(3);

        assertTrue(board.getElapsedMillis() >= 0);
    }

    @Test
    public void elapsedTimeStopsAtTheEnd() throws InterruptedException {
        Board board = board(1);
        board.click(0, 0);

        long first = board.getElapsedMillis();
        Thread.sleep(20);
        long second = board.getElapsedMillis();

        assertEquals(first, second);
    }

}
