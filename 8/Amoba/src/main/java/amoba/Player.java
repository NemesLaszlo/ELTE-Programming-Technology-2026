package amoba;

/**
 * A két játékos: X és O. A modell része.
 *
 * Enum, mert pontosan két játékos van, és sehol nem kell belőlük újat
 * létrehozni. A tábla egy mezője vagy üres (null), vagy az egyik játékosé.
 * A name() adja a jelet ("X", "O"), ezt írja a nézet a gombokra.
 */
public enum Player {

    X, O;

    /**
     * @return a másik játékos; a lépés után ő következik
     */
    public Player other() {
        return this == X ? O : X;
    }

}
