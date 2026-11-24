package dodger.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A Dodger játék MODELLJE: a játéktér, a hajó, a hulló tárgyak, az életek,
 * a pontszám és az idő, és minden szabály.
 *
 * A játék ütemekben (tick) halad: a nézet Timer-e TICK_MS
 * ezredmásodpercenként meghívja a tick metódust, ami egy ütemnyit léptet
 * mindent. Az idő is ütemekben van mérve (ticks × TICK_MS), ezért a modell
 * ideje nem függ a rendszerórától: a tesztben "eltelik" az idő, ha
 * meghívjuk a tick-et.
 *
 * A billentyűzetet a modell nem ismeri: a nézet irányokat jelez be
 * (press / release), és a tick minden lenyomva tartott irányba lépteti a
 * hajót. Így a hajó addig megy, amíg a billentyűt nyomják, és átlósan is
 * tud menni (W + D).
 *
 * A szabályok:
 * - a hajó a játéktér alján indul, és nem hagyhatja el a játékteret;
 * - felülről véletlen helyen és sebességgel aszteroidák és csillagok esnek;
 *   az aszteroidák idővel egyre sűrűbben jönnek;
 * - csillag elkapása: +1 pont, a csillag eltűnik;
 * - aszteroida eltalálja a hajót: -1 élet, az aszteroida eltűnik;
 * - 0 élettel vége a játéknak; utána a tick már nem csinál semmit;
 * - a játéktér alján kiment tárgyak eltűnnek.
 *
 * A véletlent a konstruktor kapja: a tesztek rögzített maggal adják, vagy
 * egyáltalán nem bízzák a véletlenre, hanem a spawn metódussal konkrét
 * tárgyat tesznek a játéktérre.
 */
public class Game {

    /** A játéktér mérete képpontban. */
    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;
    /** Egy ütem hossza ezredmásodpercben (50 ütem / mp). */
    public static final int TICK_MS = 20;
    /** Ennyi élettel indul a játék. */
    public static final int LIVES = 3;

    /** Egy ütemben ekkora eséllyel jön új csillag. */
    static final double STAR_PROBABILITY = 0.015;
    /** Egy ütemben ekkora eséllyel jön új aszteroida az elején... */
    static final double ASTEROID_PROBABILITY = 0.03;
    /** ...és ütemenként ennyivel nő az esély (1 perc után kb. a duplája). */
    static final double ASTEROID_PROBABILITY_GROWTH = 0.00001;
    static final int MIN_SPEED = 2;
    static final int MAX_SPEED = 5;

    private final Random random;
    private final Ship ship;
    private final List<FallingObject> objects = new ArrayList<>();
    private final Set<Direction> pressed = EnumSet.noneOf(Direction.class);
    private int lives = LIVES;
    private int score;
    private long ticks;
    private boolean over;

    /**
     * Új játék.
     */
    public Game() {
        this(new Random());
    }

    /**
     * Új játék a megadott véletlenszám-generátorral (a teszteknek).
     *
     * @param random a véletlen forrása
     */
    public Game(Random random) {
        this.random = random;
        ship = new Ship((WIDTH - Ship.SIZE) / 2, HEIGHT - Ship.SIZE - 10);
    }

    /**
     * Egy irány billentyűjét lenyomták: a hajó ebbe az irányba megy, amíg
     * fel nem engedik.
     */
    public void press(Direction direction) {
        pressed.add(direction);
    }

    /**
     * Egy irány billentyűjét felengedték.
     */
    public void release(Direction direction) {
        pressed.remove(direction);
    }

    /**
     * Egy ütem: a hajó lép, a tárgyak esnek, ütközések, új tárgyak.
     * A játék vége után nem csinál semmit.
     */
    public void tick() {
        if (over) {
            return;
        }
        ticks++;
        for (Direction direction : pressed) {
            ship.move(direction, WIDTH, HEIGHT);
        }
        for (FallingObject object : objects) {
            object.fall();
        }
        handleCollisions();
        objects.removeIf(object -> object.getY() >= HEIGHT);
        spawnRandomObjects();
    }

    private void handleCollisions() {
        Iterator<FallingObject> iterator = objects.iterator();
        while (iterator.hasNext() && !over) {
            FallingObject object = iterator.next();
            if (!ship.intersects(object)) {
                continue;
            }
            iterator.remove();
            if (object.getKind() == FallingObject.Kind.STAR) {
                score++;
            } else {
                lives--;
                if (lives == 0) {
                    over = true;
                }
            }
        }
    }

    private void spawnRandomObjects() {
        if (random.nextDouble() < STAR_PROBABILITY) {
            spawn(randomObject(FallingObject.Kind.STAR));
        }
        double asteroidProbability = ASTEROID_PROBABILITY + ticks * ASTEROID_PROBABILITY_GROWTH;
        if (random.nextDouble() < asteroidProbability) {
            spawn(randomObject(FallingObject.Kind.ASTEROID));
        }
    }

    private FallingObject randomObject(FallingObject.Kind kind) {
        int size = FallingObject.sizeOf(kind);
        int x = random.nextInt(WIDTH - size + 1);
        int speed = MIN_SPEED + random.nextInt(MAX_SPEED - MIN_SPEED + 1);
        // A játéktér fölött indul, hogy "beússzon" a képbe.
        return new FallingObject(kind, x, -size, speed);
    }

    /**
     * Egy tárgy a játéktérre. Csomagszintű: a tesztek így tesznek konkrét
     * aszteroidát vagy csillagot a hajó fölé, véletlen nélkül.
     */
    void spawn(FallingObject object) {
        objects.add(object);
    }

    /**
     * @return a játék kezdete óta eltelt idő ezredmásodpercben (ütemekből
     * számolva, a vége után nem nő)
     */
    public long getElapsedMillis() {
        return ticks * TICK_MS;
    }

    public Ship getShip() {
        return ship;
    }

    /**
     * @return a hulló tárgyak, csak olvasásra
     */
    public List<FallingObject> getObjects() {
        return Collections.unmodifiableList(objects);
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public boolean isOver() {
        return over;
    }

}
