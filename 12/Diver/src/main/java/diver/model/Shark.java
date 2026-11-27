package diver.model;

/**
 * Egy cápa: a helye és az iránya. Egy vonal mentén járőrözik (vízszintesen
 * vagy függőlegesen): megy, amíg sziklába vagy a pálya szélébe nem ütközik,
 * akkor megfordul. A Game mozgatja (patrol) és kérdezi, lát-e valamit
 * (sees): a cápa maga nem ismeri a pályát.
 */
public final class Shark {

    /** Ennyi mezőre lát előre; a saját mezőjét is "látja". */
    public static final int VISION = 1;

    private Position position;
    private Direction direction;

    public Shark(Position position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    /**
     * Egy lépés az irányba, ha a következő mező szabad; különben megfordul,
     * és ebben az ütemben nem lép.
     *
     * @param level a pálya (hol van szikla, hol a széle)
     */
    void patrol(Level level) {
        Position next = position.step(direction);
        if (level.contains(next) && level.getTile(next).isPassable()) {
            position = next;
        } else {
            direction = direction.opposite();
        }
    }

    /**
     * Látja-e a cápa a megadott mezőt: a saját mezője, vagy az előtte lévő
     * legfeljebb VISION mező az irányában; szikla mögé nem lát.
     *
     * @param target a vizsgált mező (a búvár helye)
     * @param level a pálya
     * @return igaz, ha a mező a cápa látóterében van
     */
    boolean sees(Position target, Level level) {
        if (position.equals(target)) {
            return true;
        }
        Position ahead = position;
        for (int distance = 1; distance <= VISION; distance++) {
            ahead = ahead.step(direction);
            if (!level.contains(ahead) || !level.getTile(ahead).isPassable()) {
                return false;
            }
            if (ahead.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public Position getPosition() {
        return position;
    }

    public Direction getDirection() {
        return direction;
    }

}
