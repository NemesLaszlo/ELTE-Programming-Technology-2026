package ufos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 * A rajzterület: a NÉZET egyik fele. Egy JPanel, amelynek a paintComponent
 * metódusa a modell (Space) állapotát rajzolja ki. A nézetben nincs
 * állapot és nincs szabály: minden, amit kirajzol, a modellből jön.
 *
 * Elemi grafika Swingben:
 * - a paintComponent(Graphics)-ot a Swing hívja, amikor a komponenst ki
 *   kell rajzolni (először, az ablak átméretezésekor, vagy mert kértük);
 * - ha a modell megváltozott, a repaint() hívással kérjük az újrarajzolást
 *   (ld. UfoGUI: a Timer minden ütésére step() + repaint());
 * - a rajzolás közben NEM változtatjuk a komponens állapotát (pl. nincs
 *   setBackground a paintComponent-ben): a hátteret egy kitöltött
 *   téglalappal rajzoljuk.
 *
 * Az egér húzása az egyetlen esemény, amit ez az osztály kezel: a modellt
 * hívja (moveAllTo), majd repaint-et kér. Az ablak átméretezésekor a
 * modellnek megmondja az új méretet (resize).
 */
@SuppressWarnings("serial") // a JPanel Serializable, de a panelt nem mentjük fájlba
class DrawArea extends JPanel {

    /** A modell: a tér az UFO-kkal. */
    private final Space space;

    public DrawArea(Space space) {
        this.space = space;
        setPreferredSize(new Dimension(space.getWidth(), space.getHeight()));
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Az egér hegye az UFO közepére kerül.
                space.moveAllTo(e.getX() - Ufo.WIDTH / 2.0, e.getY() - Ufo.HEIGHT / 2.0);
                repaint();
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (getWidth() > 0 && getHeight() > 0) {
                    space.resize(getWidth(), getHeight());
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Élsimítás: a körök széle nem lesz "fogazott".
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // A háttér: a modell színe, kitöltött téglalapként.
        g2.setColor(new Color(space.getBackgroundRgb()));
        g2.fillRect(0, 0, getWidth(), getHeight());
        for (Ufo ufo : space.getUfos()) {
            drawUfo(g2, ufo);
        }
    }

    /**
     * Egy UFO kirajzolása: kupola (kör) és alatta a test (lapos ellipszis),
     * a test a kupola sötétebb árnyalata. Ez az egyetlen hely, ahol a nézet
     * egy UFO állapotát olvassa.
     */
    private void drawUfo(Graphics2D g2, Ufo ufo) {
        int x = (int) ufo.getX();
        int y = (int) ufo.getY();
        // A modell egész számként tárolja a színt, a Color itt készül.
        Color color = new Color(ufo.getRgb());
        g2.setColor(color);
        g2.fillOval(x + 75, y, 50, 50);
        g2.setColor(color.darker());
        g2.fillOval(x, y + 25, Ufo.WIDTH, 30);
    }

}
