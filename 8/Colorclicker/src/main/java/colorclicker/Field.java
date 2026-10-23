package colorclicker;

import java.awt.Color;

/**
 * A tábla egy mezője: a színe, és hogy hányadik kattintás színezte ki.
 *
 * Amíg a mező üres, a színe null és a sorszáma -1. A mezőt a BoardGUI
 * eseménykezelője színezi ki (setColor, setNumber), a Board csak tárolja.
 *
 * Figyeljük meg: a szín típusa java.awt.Color, vagyis a "modell" ebben a
 * változatban az AWT-től függ. Az optimalizált változatban a szín egy egész
 * szám (0xRRGGBB), és a nézet csinál belőle Color-t.
 *
 * @author pinter
 */
public class Field {

    private Color color;
    private int number;

    public Field() {
        color = null;
        number = -1;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}
