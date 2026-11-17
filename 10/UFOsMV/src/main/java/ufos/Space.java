package ufos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A tér, amiben az UFO-k repkednek: a MODELL (az MV "M" betűje).
 *
 * A Space a gazdája az UFO-k listájának, és itt van minden szabály:
 * - hány UFO van, hogyan keletkezik egy új (véletlen hely, sebesség, szín),
 *   és hogyan vesszük el őket;
 * - egy lépésben (step) minden UFO továbbmegy, és kis eséllyel új színt kap;
 * - a háttér színe is a tér tulajdonsága.
 *
 * A modellben nincs Swing és nincs java.awt: a színek egész számok
 * (0xRRGGBB), a méret két int. A véletlent a konstruktor kapja: a tesztek
 * rögzített maggal adják (new Random(42)), így a "véletlen" is
 * kiszámítható (ld. a 8. heti Board).
 *
 * A nézet (DrawArea, UfoGUI) a Timer minden ütésére meghívja a step-et,
 * majd repaint-et kér; a gombok az addUfos / removeUfos / newBackground
 * metódusokat hívják; az egér a moveAllTo-t.
 */
public class Space {

    /** Egy lépésben ekkora eséllyel kap új színt egy UFO. */
    public static final double RECOLOR_PROBABILITY = 0.003;
    /** A sebesség komponensei a [-MAX_SPEED, MAX_SPEED) tartományból. */
    public static final double MAX_SPEED = 2;
    /** Ennyi szín van: 0x000000 .. 0xFFFFFF. */
    private static final int COLOR_COUNT = 0x1000000;

    private final Random random;
    private final List<Ufo> ufos = new ArrayList<>();
    private int width;
    private int height;
    private int backgroundRgb = 0xFFFFFF;

    /**
     * Új tér a megadott számú UFO-val.
     *
     * @param width a tér szélessége
     * @param height a tér magassága
     * @param ufoCount a kezdeti UFO-k száma
     */
    public Space(int width, int height, int ufoCount) {
        this(width, height, ufoCount, new Random());
    }

    /**
     * Új tér a megadott véletlenszám-generátorral (a teszteknek).
     *
     * @param width a tér szélessége
     * @param height a tér magassága
     * @param ufoCount a kezdeti UFO-k száma
     * @param random a véletlen forrása
     * @throws IllegalArgumentException ha a méret nem pozitív, vagy a darabszám negatív
     */
    public Space(int width, int height, int ufoCount, Random random) {
        if (ufoCount < 0) {
            throw new IllegalArgumentException("The number of UFOs cannot be negative: " + ufoCount);
        }
        this.random = random;
        resize(width, height);
        addUfos(ufoCount);
    }

    /**
     * A tér új mérete. A nézet hívja, ha az ablakot átméretezik; az UFO-k
     * a következő lépésben a határokhoz igazodnak.
     *
     * @throws IllegalArgumentException ha a méret nem pozitív
     */
    public final void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("The size must be positive: " + width + " x " + height);
        }
        this.width = width;
        this.height = height;
    }

    /**
     * Hozzáad ennyi új, véletlen UFO-t.
     *
     * @param count a darabszám (0 vagy negatív: nem történik semmi)
     */
    public final void addUfos(int count) {
        for (int i = 0; i < count; i++) {
            ufos.add(randomUfo());
        }
    }

    /**
     * Elvesz ennyi UFO-t (a lista elejéről, a legrégebbieket). Ha kevesebb
     * van, mind elfogy; ez nem hiba.
     *
     * @param count a darabszám
     * @return hány UFO-t vettünk el ténylegesen
     */
    public int removeUfos(int count) {
        int removed = 0;
        while (removed < count && !ufos.isEmpty()) {
            ufos.remove(0);
            removed++;
        }
        return removed;
    }

    /**
     * Egy lépés: minden UFO továbbmegy a sebessége irányába (a tér szélén
     * átfordul), és RECOLOR_PROBABILITY eséllyel új színt kap.
     */
    public void step() {
        for (Ufo ufo : ufos) {
            ufo.move(width, height);
            if (random.nextDouble() < RECOLOR_PROBABILITY) {
                ufo.setRgb(randomRgb());
            }
        }
    }

    /**
     * Minden UFO-t a megadott pontra tesz (az egérrel húzáskor).
     */
    public void moveAllTo(double x, double y) {
        for (Ufo ufo : ufos) {
            ufo.moveTo(x, y);
        }
    }

    /**
     * Új véletlen háttérszín.
     */
    public void newBackground() {
        backgroundRgb = randomRgb();
    }

    private Ufo randomUfo() {
        double x = random.nextDouble() * width;
        double y = random.nextDouble() * height;
        double velX = random.nextDouble() * 2 * MAX_SPEED - MAX_SPEED;
        double velY = random.nextDouble() * 2 * MAX_SPEED - MAX_SPEED;
        return new Ufo(x, y, velX, velY, randomRgb());
    }

    private int randomRgb() {
        return random.nextInt(COLOR_COUNT);
    }

    /**
     * @return az UFO-k, csak olvasásra: a listát csak a modell módosíthatja
     */
    public List<Ufo> getUfos() {
        return Collections.unmodifiableList(ufos);
    }

    public int getUfoCount() {
        return ufos.size();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * @return a háttér színe 0xRRGGBB alakban
     */
    public int getBackgroundRgb() {
        return backgroundRgb;
    }

}
