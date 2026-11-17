package ufos;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Egy UFO: a helye, a sebessége és a színe. KIINDULÓ változat.
 *
 * Az osztály egyszerre "modell" (hol van az UFO, merre megy) és "nézet" (a
 * draw metódus rajzolja ki magát egy Graphics2D-re), és a színt
 * java.awt.Color-ként tárolja. Ezért felület nélkül nem tesztelhető: a
 * move szabályát (a képernyő szélén átfordul a túloldalra) csak a futó
 * programban, szemmel ellenőrizhetjük. Az UFOsMV projektben ezt
 * választjuk szét.
 *
 * @author bli
 */
public class UFO {

    private double x;
    private double y;
    private final double velx;
    private final double vely;

    private Color color;

    /**
     * Új UFO véletlen helyen, véletlen (lassú) sebességgel és véletlen
     * színnel.
     */
    public UFO() {
        x = Math.random() * 2000;
        y = Math.random() * 2000;
        velx = Math.random() * 4 - 2;
        vely = Math.random() * 4 - 2;
        updateColor();
    }

    /**
     * Kirajzolja az UFO-t: egy kupola (kör) és alatta egy lapos test
     * (ellipszis), a test a kupola sötétebb árnyalata.
     *
     * @param g2 a rajzfelület, a DrawArea.paintComponent adja
     */
    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fillOval((int) x + 75, (int) y - 25, 50, 50);
        g2.setColor(color.darker());
        g2.fillOval((int) x, (int) y, 200, 30);
    }

    /**
     * Egy lépés a sebesség irányába. Ha az UFO kimegy a rajzterületről,
     * a túloldalon jön vissza.
     *
     * @param width a rajzterület szélessége
     * @param height a rajzterület magassága
     */
    public void move(int width, int height) {
        x += velx;
        if (x > width) {
            x = 0;
        }
        if (x < 0) {
            x = width;
        }
        y += vely;
        if (y > height) {
            y = 0;
        }
        if (y < 0) {
            y = height;
        }
    }

    /**
     * Áthelyezi az UFO-t a megadott pontra (az egérrel húzáskor).
     */
    public void movePosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Új véletlen szín.
     */
    public final void updateColor() {
        color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
    }

}
