package colorclicker;

import java.awt.Point;

/**
 * A tábla: boardSize × boardSize mező. KIINDULÓ változat.
 *
 * Ez az osztály csak tárol: a mezőket adja vissza, és meg tudja mondani, hogy
 * minden mező színes-e (isOver). A játék szabályai (melyik mezők színeződnek
 * ki egy kattintásra, milyen színnel, hányadik kattintás) nem itt vannak,
 * hanem a BoardGUI eseménykezelőjében. Ezért a szabályok csak az ablakon
 * keresztül, kattintgatva próbálhatók ki.
 *
 * A mezők helyét java.awt.Point írja le, vagyis a tábla az AWT-től függ. Az
 * optimalizált változatban ezt egy saját Position rekord váltja ki.
 *
 * @author pinter
 */
public class Board {

    private Field[][] board;
    private final int boardSize;

    public Board(int boardSize) {
        this.boardSize = boardSize;
        board = new Field[this.boardSize][this.boardSize];
        for (int i = 0; i < this.boardSize; ++i) {
            for (int j = 0; j < this.boardSize; ++j) {
                board[i][j] = new Field();
            }
        }
    }

    /**
     * @return igaz, ha már minden mező színes. Ehhez a teljes táblát végig
     * kell nézni, minden kattintás után.
     */
    public boolean isOver() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getColor() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public Field get(int x, int y) {
        return board[x][y];
    }

    public Field get(Point point) {
        int x = (int) point.getX();
        int y = (int) point.getY();
        return get(x, y);
    }

    public int getBoardSize() {
        return boardSize;
    }

}
