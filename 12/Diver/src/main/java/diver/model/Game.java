package diver.model;

import java.util.Collections;
import java.util.List;

/**
 * A Gyöngyhalász játék MODELLJE: a pályák, az aktuális pálya állapota, a
 * búvár, a cápák, az életek, a pontszám, az idő, és minden szabály.
 *
 * A modell nem tud a felületről (nincs Swing, AWT), sem az adatbázisról
 * (nincs SQL): a nézet hívja a metódusait, és az állapota alapján rajzol.
 * Felület nélkül, JUnit-tal tesztelhető (GameTest), tetszőleges pályákkal.
 *
 * A játék menete:
 * - a búvár a pálya kezdőhelyén (a hajónál) indul, 3 élettel; a cápák a
 *   kezdőhelyükön, az irányukba nézve;
 * - a búvár a WASD-dal egy mezőt lép (move): sziklára és a pályán kívülre
 *   nem; ha gyöngyre lép, felveszi: +1 pont;
 * - az idő ütemekben telik (tick): a nézet Timer-e TICK_MS-enként hívja;
 *   minden SHARK_PERIOD-ik ütemben a cápák egy mezőt lépnek, a falnál
 *   megfordulnak;
 * - ha a búvár egy cápa látóterébe kerül (a cápa mezője, vagy az előtte
 *   lévő VISION mező, sziklán át nem), elveszít egy életet, és visszakerül
 *   a kezdőhelyre; ezt minden búvárlépés és minden cápalépés után
 *   ellenőrizzük;
 * - ha a pálya minden gyöngyét felvette, jön a következő pálya (az utolsó
 *   után újra az első), a pontszám és az életek megmaradnak;
 * - 0 élettel vége a játéknak: a move és a tick nem csinál többé semmit.
 */
public class Game {

    /** Egy ütem hossza ezredmásodpercben (20 ütem / mp). */
    public static final int TICK_MS = 50;
    /** A cápák ennyi ütemenként lépnek egyet (fél másodperc). */
    public static final int SHARK_PERIOD = 10;
    /** Ennyi élettel indul a játék. */
    public static final int LIVES = 3;

    private final List<Level> levels;
    private int levelIndex;
    private Level level;
    private Tile[][] tiles;
    private Position diver;
    private List<Shark> sharks;
    private int pearlsLeft;
    private int lives = LIVES;
    private int score;
    private long ticks;
    private boolean over;

    /**
     * Új játék a 10 beépített pályával.
     */
    public Game() {
        this(Level.loadAll());
    }

    /**
     * Új játék a megadott pályákkal, sorban (a tesztek így adnak kis
     * pályákat).
     *
     * @param levels a pályák; legalább egy
     */
    public Game(List<Level> levels) {
        if (levels.isEmpty()) {
            throw new IllegalArgumentException("At least one level is required");
        }
        this.levels = List.copyOf(levels);
        startLevel(0);
    }

    /**
     * Az adott sorszámú pálya kiinduló állapota: a mezők másolata, a búvár a
     * kezdőhelyen, a cápák a kezdőhelyükön.
     */
    private void startLevel(int index) {
        levelIndex = index;
        level = levels.get(index);
        tiles = level.copyTiles();
        diver = level.getStart();
        sharks = level.newSharks();
        pearlsLeft = level.getPearlCount();
    }

    /**
     * A búvár egy lépése. Ha a célmező szikla vagy a pályán kívül van, nem
     * történik semmi. A játék vége után sem.
     *
     * @param direction az irány
     * @return igaz, ha a búvár lépett
     */
    public boolean move(Direction direction) {
        if (over) {
            return false;
        }
        Position next = diver.step(direction);
        if (!level.contains(next) || !getTile(next).isPassable()) {
            return false;
        }
        diver = next;
        if (getTile(diver) == Tile.PEARL) {
            tiles[diver.row()][diver.col()] = Tile.WATER;
            pearlsLeft--;
            score++;
        }
        if (pearlsLeft == 0) {
            startLevel((levelIndex + 1) % levels.size());
            return true;
        }
        checkSharks();
        return true;
    }

    /**
     * Egy ütem: az idő telik, és minden SHARK_PERIOD-ik ütemben a cápák
     * lépnek. A játék vége után nem csinál semmit.
     */
    public void tick() {
        if (over) {
            return;
        }
        ticks++;
        if (ticks % SHARK_PERIOD == 0) {
            for (Shark shark : sharks) {
                shark.patrol(level);
            }
            checkSharks();
        }
    }

    /**
     * Ha valamelyik cápa látja a búvárt: egy élet elvész, a búvár a
     * kezdőhelyre kerül; 0 élettel vége.
     */
    private void checkSharks() {
        for (Shark shark : sharks) {
            if (shark.sees(diver, level)) {
                lives--;
                diver = level.getStart();
                if (lives == 0) {
                    over = true;
                }
                return;
            }
        }
    }

    /**
     * @param position egy mező a pályán
     * @return a mező fajtája (a felvett gyöngy helyén már víz)
     */
    public Tile getTile(Position position) {
        return tiles[position.row()][position.col()];
    }

    public int getRows() {
        return level.getRows();
    }

    public int getCols() {
        return level.getCols();
    }

    public Position getDiver() {
        return diver;
    }

    /**
     * @return a cápák, csak olvasásra
     */
    public List<Shark> getSharks() {
        return Collections.unmodifiableList(sharks);
    }

    /**
     * @return az aktuális pálya sorszáma, 1-től
     */
    public int getLevelNumber() {
        return levelIndex + 1;
    }

    public int getLevelCount() {
        return levels.size();
    }

    public int getPearlsLeft() {
        return pearlsLeft;
    }

    public int getLives() {
        return lives;
    }

    /**
     * @return az eddig felvett gyöngyök száma, minden pályán együtt
     */
    public int getScore() {
        return score;
    }

    /**
     * @return a játék kezdete óta eltelt idő ezredmásodpercben (ütemekből
     * számolva, a vége után nem nő)
     */
    public long getElapsedMillis() {
        return ticks * TICK_MS;
    }

    public boolean isOver() {
        return over;
    }

}
