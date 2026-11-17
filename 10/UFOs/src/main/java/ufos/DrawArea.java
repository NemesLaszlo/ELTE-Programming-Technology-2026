package ufos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * A rajzterület: egy JPanel, amelynek a paintComponent metódusát
 * felülírjuk, és abban rajzoljuk ki az UFO-kat. KIINDULÓ változat.
 *
 * Elemi grafika Swingben:
 * - a komponens tartalmát a paintComponent(Graphics) rajzolja; soha nem mi
 *   hívjuk, hanem a Swing, amikor a komponenst (újra) ki kell rajzolni;
 * - ha a kirajzolandó állapot megváltozott, a repaint() hívással KÉRJÜK az
 *   újrarajzolást; a Swing hamarosan meghívja a paintComponent-et;
 * - a Graphics2D a "ceruza": szín, alakzatok (fillOval, fillRect, drawLine,
 *   drawString, ...), transzformációk.
 *
 * Mi a gond ezzel a változattal?
 * - A rajzterület tartja az UFO-k listáját, és az egérkezelője maga
 *   mozgatja őket: a nézetben van a logika.
 * - A háttér színét is itt sorsoljuk (updateBackground), és a
 *   paintComponent-ben állítjuk be a setBackground-dal, ami újabb
 *   újrarajzolást kér: a rajzolás közben nem szabad a komponens állapotát
 *   változtatni.
 *
 * @author bli
 */
@SuppressWarnings("serial") // a JPanel Serializable, de a panelt nem mentjük fájlba
class DrawArea extends JPanel {

    private final ArrayList<UFO> ufos;
    private Color background = Color.WHITE;

    public DrawArea(ArrayList<UFO> ufos) {
        this.ufos = ufos;
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent me) {
                // Húzáskor minden UFO az egér helyére ugrik.
                for (UFO ufo : ufos) {
                    ufo.movePosition(me.getX() - 100, me.getY());
                }
            }

            @Override
            public void mouseMoved(MouseEvent me) {
            }
        });
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        // A super letörli a területet a háttérszínnel; e nélkül az előző
        // képkocka is ott maradna.
        super.paintComponent(grphcs);
        Graphics2D g2 = (Graphics2D) grphcs;
        setBackground(background);
        for (UFO ufo : ufos) {
            ufo.draw(g2);
        }
    }

    /**
     * Új véletlen háttérszín. A következő paintComponent használja.
     */
    public void updateBackground() {
        background = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
    }

}
