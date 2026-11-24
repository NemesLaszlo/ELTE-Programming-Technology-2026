package dodger.model;

/**
 * A négy irány, amerre az űrhajó mozoghat. A dx, dy az irány egységvektora:
 * a hajó egy lépésben SPEED × (dx, dy)-nal mozdul el.
 *
 * A nézet a W/A/S/D billentyűket képezi le ezekre az irányokra; a modell
 * csak irányokat ismer, billentyűket nem.
 */
public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

}
