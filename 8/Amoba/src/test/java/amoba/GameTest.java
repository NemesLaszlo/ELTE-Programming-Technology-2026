package amoba;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * A játékmenet tesztjei: ki következik, mikor van vége, ki nyert. Nincs
 * bennük Swing: a játékot ablak nélkül, a drop hívásaival játsszuk le.
 *
 * A tábla 5 sor × 8 oszlop (BoardSize.SMALL), a legalsó sor a 4.
 */
public class GameTest {

    private static final int BOTTOM = 4;

    private Game game;

    @Before
    public void setUp() {
        game = new Game(BoardSize.SMALL);
    }

    /** Lejátszik egy lépéssorozatot: a soron következő játékos lép az oszlopba. */
    private void play(int... columns) {
        for (int col : columns) {
            game.drop(col);
        }
    }

    // ----- Az új játék -----

    @Test
    public void newGameStartsWithXOnAnEmptyBoard() {
        assertEquals(Player.X, game.getCurrentPlayer());
        assertFalse(game.isOver());
        assertFalse(game.isDraw());
        assertNull(game.getWinner());
        assertEquals(5, game.getBoard().getRows());
        assertEquals(8, game.getBoard().getColumns());
        assertEquals(0, game.getBoard().getPieceCount());
        for (int col = 0; col < 8; col++) {
            assertTrue(game.canDrop(col));
        }
    }

    // ----- Lépések -----

    @Test
    public void dropReturnsWhereThePieceLanded() {
        assertEquals(new Position(BOTTOM, 3), game.drop(3));
        assertEquals(new Position(BOTTOM - 1, 3), game.drop(3));
    }

    @Test
    public void playersTakeTurns() {
        game.drop(0);
        assertEquals(Player.O, game.getCurrentPlayer());
        assertEquals(Player.X, game.getBoard().get(BOTTOM, 0));

        game.drop(0);
        assertEquals(Player.X, game.getCurrentPlayer());
        assertEquals(Player.O, game.getBoard().get(BOTTOM - 1, 0));
    }

    @Test
    public void cannotDropIntoAFullColumn() {
        play(5, 5, 5, 5, 5);

        assertFalse(game.canDrop(5));
        assertTrue(game.canDrop(4));
        try {
            game.drop(5);
            throw new AssertionError("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            // ezt vártuk
        }
        // A sikertelen lépés nem adja át a sort.
        assertEquals(Player.O, game.getCurrentPlayer());
        assertFalse(game.isOver());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotDropOutsideTheBoard() {
        game.drop(8);
    }

    // ----- Győzelem -----

    @Test
    public void fourInARowWinsTheGame() {
        // X: 0, 1, 2, 3 az alsó sorban; O: fölé.
        play(0, 0, 1, 1, 2, 2);
        assertFalse(game.isOver());

        Position landed = game.drop(3);

        assertEquals(new Position(BOTTOM, 3), landed);
        assertTrue(game.isOver());
        assertFalse(game.isDraw());
        assertEquals(Player.X, game.getWinner());
        // A győztes marad az "aktuális" játékos: a sor nem kerül át.
        assertEquals(Player.X, game.getCurrentPlayer());
    }

    @Test
    public void oCanWinToo() {
        // X: 0, 1, 2, 7; O: 3, 4, 5, 6 az alsó sorban.
        play(0, 3, 1, 4, 2, 5, 7, 6);

        assertTrue(game.isOver());
        assertEquals(Player.O, game.getWinner());
    }

    @Test
    public void aDiagonalWinsTheGame() {
        // X a (4,0), (3,1), (2,2), (1,3) átlón; a közbülső O-k és az X
        // "töltelék" lépései (5, 6, 7) nem adnak négyest.
        play(0, 1, 1, 2, 5, 2, 2, 3, 6, 3, 7, 3);
        assertFalse(game.isOver());

        Position landed = game.drop(3);

        assertEquals(new Position(1, 3), landed);
        assertTrue(game.isOver());
        assertEquals(Player.X, game.getWinner());
    }

    @Test
    public void fourInAColumnWins() {
        // Négy X egymás fölött a 0. oszlopban; O az 1. oszlopba rak.
        play(0, 1, 0, 1, 0, 1);
        assertFalse(game.isOver());

        Position landed = game.drop(0);

        assertEquals(new Position(BOTTOM - 3, 0), landed);
        assertTrue(game.isOver());
        assertEquals(Player.X, game.getWinner());
    }

    @Test
    public void noMoveIsAcceptedAfterTheEnd() {
        play(0, 0, 1, 1, 2, 2, 3);
        assertTrue(game.isOver());

        for (int col = 0; col < 8; col++) {
            assertFalse(game.canDrop(col));
        }
        try {
            game.drop(4);
            throw new AssertionError("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            // ezt vártuk
        }
        assertEquals(7, game.getBoard().getPieceCount());
    }

    // ----- Döntetlen -----

    @Test
    public void aFullBoardWithoutALineIsADraw() {
        // 4 × 4-es tábla, 16 lépés, egyik után sincs négyes; a végállás:
        //                        O O X X
        //                        X O O O
        //                        O X X X
        //                        O X O X
        Game small = new Game(4, 4);
        int[] moves = {3, 2, 1, 0, 2, 0, 0, 0, 3, 3, 1, 2, 3, 1, 2, 1};
        for (int col : moves) {
            assertFalse(small.isOver());
            small.drop(col);
        }

        assertTrue(small.isOver());
        assertTrue(small.isDraw());
        assertNull(small.getWinner());
        assertTrue(small.getBoard().isFull());
    }

    @Test
    public void aFullRowWithAlternatingPiecesIsADraw() {
        // 1 × 4-es tábla: X O X O, a tábla tele, nincs négyes.
        Game tiny = new Game(1, 4);
        tiny.drop(0);
        tiny.drop(1);
        tiny.drop(2);
        tiny.drop(3);

        assertTrue(tiny.isOver());
        assertTrue(tiny.isDraw());
    }

    @Test
    public void aWinningLastMoveIsAWinNotADraw() {
        // 4 × 4-es tábla, 16 lépés: az utolsó (O a 0. oszlopba, a (0,0)
        // mezőre) betölti a táblát ÉS átlós négyest ad: (0,0) (1,1) (2,2) (3,3).
        // A végállás:            O X O X
        //                        X O X X
        //                        X O O X
        //                        O X O O
        Game full = new Game(4, 4);
        int[] moves = {1, 3, 3, 0, 3, 1, 3, 2, 0, 1, 1, 2, 2, 2, 0};
        for (int col : moves) {
            full.drop(col);
            assertFalse(full.isOver());
        }

        Position landed = full.drop(0);

        assertEquals(new Position(0, 0), landed);
        assertTrue(full.getBoard().isFull());
        assertTrue(full.isOver());
        assertFalse(full.isDraw());
        assertEquals(Player.O, full.getWinner());
    }

    // ----- Új játék -----

    @Test
    public void newGameClearsTheBoardAndXStarts() {
        play(0, 0, 1, 1, 2, 2, 3);
        assertTrue(game.isOver());

        game.newGame();

        assertFalse(game.isOver());
        assertNull(game.getWinner());
        assertEquals(Player.X, game.getCurrentPlayer());
        assertEquals(0, game.getBoard().getPieceCount());
        assertEquals(5, game.getBoard().getRows());
        assertEquals(8, game.getBoard().getColumns());
    }

    @Test
    public void newGameCanChangeTheSize() {
        play(0, 1);

        game.newGame(BoardSize.LARGE);

        assertEquals(7, game.getBoard().getRows());
        assertEquals(12, game.getBoard().getColumns());
        assertEquals(0, game.getBoard().getPieceCount());
        assertEquals(Player.X, game.getCurrentPlayer());
    }

    @Test
    public void newGameKeepsTheChosenSize() {
        game.newGame(BoardSize.MEDIUM);
        play(0, 0, 1, 1, 2, 2, 3);

        game.newGame();

        assertEquals(6, game.getBoard().getRows());
        assertEquals(10, game.getBoard().getColumns());
    }

}
