package amoba;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * A tábla tesztjei: a potyogás és a négyes keresése. Nincs bennük Swing.
 *
 * A tábla 5 sor × 8 oszlop (BoardSize.SMALL); a sorokat felülről számozzuk,
 * a legalsó sor a 4.
 */
public class BoardTest {

    private static final int BOTTOM = 4;

    private Board board;

    @Before
    public void setUp() {
        board = new Board(BoardSize.SMALL);
    }

    /** Lerak néhány jelet ugyanabba az oszlopba, alulról felfelé. */
    private Position dropAll(int col, Player... players) {
        Position last = null;
        for (Player player : players) {
            last = board.drop(col, player);
        }
        return last;
    }

    // ----- Az új tábla -----

    @Test
    public void newBoardIsEmpty() {
        assertEquals(5, board.getRows());
        assertEquals(8, board.getColumns());
        assertEquals(0, board.getPieceCount());
        assertFalse(board.isFull());
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                assertNull(board.get(row, col));
            }
        }
        for (int col = 0; col < 8; col++) {
            assertFalse(board.isColumnFull(col));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void boardNeedsAtLeastOneRow() {
        new Board(0, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void boardNeedsAtLeastOneColumn() {
        new Board(5, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void positionOutsideTheBoardIsRejected() {
        board.get(5, 0);
    }

    // ----- Potyogás -----

    @Test
    public void thePieceFallsToTheBottomOfTheColumn() {
        Position landed = board.drop(2, Player.X);

        assertEquals(new Position(BOTTOM, 2), landed);
        assertEquals(Player.X, board.get(BOTTOM, 2));
        assertEquals(1, board.getPieceCount());
    }

    @Test
    public void piecesStackUpInAColumn() {
        assertEquals(new Position(BOTTOM, 3), board.drop(3, Player.X));
        assertEquals(new Position(BOTTOM - 1, 3), board.drop(3, Player.O));
        assertEquals(new Position(BOTTOM - 2, 3), board.drop(3, Player.X));

        assertEquals(Player.X, board.get(BOTTOM, 3));
        assertEquals(Player.O, board.get(BOTTOM - 1, 3));
        assertEquals(Player.X, board.get(BOTTOM - 2, 3));
        assertNull(board.get(BOTTOM - 3, 3));
    }

    @Test
    public void columnsAreIndependent() {
        board.drop(0, Player.X);
        board.drop(0, Player.O);

        assertEquals(new Position(BOTTOM, 1), board.drop(1, Player.X));
    }

    @Test
    public void aColumnBecomesFull() {
        dropAll(6, Player.X, Player.O, Player.X, Player.O);
        assertFalse(board.isColumnFull(6));

        board.drop(6, Player.X);

        assertTrue(board.isColumnFull(6));
    }

    @Test(expected = IllegalStateException.class)
    public void cannotDropIntoAFullColumn() {
        dropAll(6, Player.X, Player.O, Player.X, Player.O, Player.X);

        board.drop(6, Player.O);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotDropOutsideTheBoard() {
        board.drop(8, Player.X);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotDropWithoutAPlayer() {
        board.drop(0, null);
    }

    @Test
    public void theBoardBecomesFull() {
        Board small = new Board(1, 3);
        small.drop(0, Player.X);
        small.drop(1, Player.O);
        assertFalse(small.isFull());

        small.drop(2, Player.X);

        assertTrue(small.isFull());
    }

    // ----- Négyes keresése -----

    @Test
    public void emptyPositionHasNoLine() {
        assertFalse(board.hasLineThrough(new Position(BOTTOM, 0)));
    }

    @Test
    public void aSinglePieceIsNotALine() {
        Position landed = board.drop(0, Player.X);

        assertFalse(board.hasLineThrough(landed));
    }

    @Test
    public void threeInARowIsNotEnough() {
        board.drop(0, Player.X);
        board.drop(1, Player.X);
        Position landed = board.drop(2, Player.X);

        assertFalse(board.hasLineThrough(landed));
    }

    @Test
    public void fourInARowHorizontallyIsALine() {
        board.drop(0, Player.X);
        board.drop(1, Player.X);
        board.drop(2, Player.X);
        Position landed = board.drop(3, Player.X);

        assertTrue(board.hasLineThrough(landed));
        // A négyes bármelyik mezőjén át megtalálható.
        assertTrue(board.hasLineThrough(new Position(BOTTOM, 0)));
        assertTrue(board.hasLineThrough(new Position(BOTTOM, 2)));
    }

    @Test
    public void theNewPieceCanBeInTheMiddleOfTheLine() {
        board.drop(0, Player.X);
        board.drop(1, Player.X);
        board.drop(3, Player.X);
        Position landed = board.drop(2, Player.X);

        assertTrue(board.hasLineThrough(landed));
    }

    @Test
    public void fiveInARowIsAlsoALine() {
        for (int col = 0; col < 4; col++) {
            board.drop(col, Player.X);
        }
        Position landed = board.drop(4, Player.X);

        assertTrue(board.hasLineThrough(landed));
    }

    @Test
    public void onlyTheOwnPiecesCount() {
        board.drop(0, Player.X);
        board.drop(1, Player.X);
        board.drop(2, Player.O);
        board.drop(3, Player.X);
        Position landed = board.drop(4, Player.X);

        assertFalse(board.hasLineThrough(landed));
    }

    @Test
    public void aLineInTheUpperRowsIsFound() {
        // Az alsó sor O, fölötte X X X X.
        for (int col = 0; col < 4; col++) {
            board.drop(col, Player.O);
        }
        board.drop(0, Player.X);
        board.drop(1, Player.X);
        board.drop(2, Player.X);
        Position landed = board.drop(3, Player.X);

        assertEquals(new Position(BOTTOM - 1, 3), landed);
        assertTrue(board.hasLineThrough(landed));
    }

    @Test
    public void fourOnTheDownRightDiagonalIsALine() {
        // X a (4,0), (3,1), (2,2), (1,3) mezőkön; alattuk O-k tartják.
        board.drop(0, Player.X);
        dropAll(1, Player.O, Player.X);
        dropAll(2, Player.O, Player.O, Player.X);
        Position landed = dropAll(3, Player.O, Player.O, Player.O, Player.X);

        assertEquals(new Position(1, 3), landed);
        assertTrue(board.hasLineThrough(landed));
        assertTrue(board.hasLineThrough(new Position(BOTTOM, 0)));
    }

    @Test
    public void fourOnTheDownLeftDiagonalIsALine() {
        // X a (4,7), (3,6), (2,5), (1,4) mezőkön.
        board.drop(7, Player.X);
        dropAll(6, Player.O, Player.X);
        dropAll(5, Player.O, Player.O, Player.X);
        Position landed = dropAll(4, Player.O, Player.O, Player.O, Player.X);

        assertEquals(new Position(1, 4), landed);
        assertTrue(board.hasLineThrough(landed));
    }

    @Test
    public void threeOnADiagonalIsNotEnough() {
        board.drop(0, Player.X);
        dropAll(1, Player.O, Player.X);
        Position landed = dropAll(2, Player.O, Player.O, Player.X);

        assertFalse(board.hasLineThrough(landed));
    }

    @Test
    public void fourInAColumnIsALine() {
        Position landed = dropAll(0, Player.X, Player.X, Player.X, Player.X);

        assertEquals(new Position(BOTTOM - 3, 0), landed);
        assertTrue(board.hasLineThrough(landed));
        assertTrue(board.hasLineThrough(new Position(BOTTOM, 0)));
    }

    @Test
    public void threeInAColumnIsNotEnough() {
        Position landed = dropAll(0, Player.X, Player.X, Player.X);

        assertFalse(board.hasLineThrough(landed));
    }

    @Test
    public void aColumnWithMixedPiecesIsNotALine() {
        Position landed = dropAll(0, Player.X, Player.X, Player.O, Player.X, Player.X);

        assertFalse(board.hasLineThrough(landed));
    }

    @Test
    public void aLineIsFoundOnTheLargestBoard() {
        Board large = new Board(BoardSize.LARGE);
        for (int col = 8; col < 11; col++) {
            large.drop(col, Player.O);
        }
        Position landed = large.drop(11, Player.O);

        assertEquals(new Position(6, 11), landed);
        assertTrue(large.hasLineThrough(landed));
    }

}
