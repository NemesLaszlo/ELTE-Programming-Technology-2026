package diver.model;

/**
 * Egy mező helye a rácson: sor és oszlop (0-tól). Rekord, mint a 8. heti
 * Position: értékosztály, két azonos helyű példány egyenlő.
 *
 * @param row a sor (felülről, 0-tól)
 * @param col az oszlop (balról, 0-tól)
 */
public record Position(int row, int col) {

    /**
     * @param direction az irány
     * @return a szomszédos mező az irányban (lehet a pályán kívül is: a
     * hívó ellenőrzi)
     */
    public Position step(Direction direction) {
        return new Position(row + direction.getDRow(), col + direction.getDCol());
    }

}
