package bag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Zsák (multihalmaz) ArrayList-tel megvalósítva.
 *
 * A zsák olyan, mint a halmaz, de egy elem többször is szerepelhet benne.
 * Magát az elemet nem tároljuk többször: elemenként egyetlen BagItem tartja
 * nyilván, hogy hány darab van belőle.
 *
 * Az ábrázolás invariánsa (minden művelet után igaz marad):
 * - egy elemhez legfeljebb egy BagItem tartozik a listában,
 * - minden tárolt darabszám pozitív (ami elfogy, azt töröljük a listából).
 *
 * Hátránya: egy elem megkeresése (getItem) lineáris idejű, és szinte minden
 * művelet ezzel kezd. A HashMap-es változat ugyanezt átlagosan konstans idő
 * alatt tudja.
 */
public class Bag {

    // List<BagItem>: "BagItem-eket tartalmazó lista". A List generikus, vagyis
    // bármilyen elemtípussal használható; a < > közé írt típusparaméterrel
    // mondjuk meg, hogy mi mivel használjuk. Innentől a fordító ellenőrzi,
    // hogy csak BagItem kerülhet bele, és kivételkor sem kell
    // típuskényszerítés (cast): amit a listából kapunk, az eleve BagItem.
    //
    // A változó típusa az interfész (List), csak a példányosításnál döntünk a
    // konkrét megvalósításról - így az később egyetlen helyen lecserélhető.
    // A final csak a referenciát rögzíti: a lista tartalma változhat, de a
    // data mindig ugyanarra a listára mutat.
    private final List<BagItem> data;

    public Bag() {
        // Az üres < > ("gyémánt") rövidítés: a fordító a data típusából
        // kitalálja, hogy new ArrayList<BagItem>()-ről van szó.
        this.data = new ArrayList<>();
    }

    /**
     * Betesz num darabot az elemből.
     *
     * @throws IllegalArgumentException ha num nem pozitív
     */
    public void add(String item, int num) {
        // Hibás paraméterre azonnal kivételt dobunk, nem hagyjuk, hogy a zsák
        // érvénytelen állapotba kerüljön (pl. negatív darabszám).
        Objects.requireNonNull(item, "item must not be null");
        if (num <= 0) {
            throw new IllegalArgumentException("num must be positive: " + num);
        }
        BagItem bi = getItem(item);
        if (bi == null) {
            data.add(new BagItem(item, num));
        } else {
            bi.addNum(num);
        }
    }

    public boolean contains(String item) {
        return getItem(item) != null;
    }

    /**
     * Az elem összes példányát kiveszi.
     *
     * @return hány darab volt belőle, vagy null, ha nem volt a zsákban. Ezért
     * Integer a visszatérési típus és nem int: az int nem lehet null.
     * (Ugyanígy viselkedik a Map.remove is.)
     */
    public Integer remove(String item) {
        BagItem bi = getItem(item);
        if (bi == null) {
            return null;
        }
        // A remove(Object) változat hívódik meg, amely equals-szal keres.
        // Vigyázat: List<Integer> esetén a remove(int) index szerint töröl!
        data.remove(bi);
        return bi.getNum();
    }

    /**
     * Kivesz num darabot az elemből. Ha nincs belőle ennyi, mindet kiveszi.
     *
     * @return igaz, ha az elem benne volt a zsákban
     * @throws IllegalArgumentException ha num nem pozitív
     */
    public boolean remove(String item, int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("num must be positive: " + num);
        }
        BagItem bi = getItem(item);
        if (bi == null) {
            return false;
        }
        int left = bi.getNum() - num;
        if (left > 0) {
            bi.setNum(left);
        } else {
            // 0 darabos bejegyzést nem hagyunk a listában (invariáns).
            data.remove(bi);
        }
        return true;
    }

    /**
     * @return hány darab van az elemből; 0, ha nincs a zsákban
     */
    public int howMany(String item) {
        BagItem bi = getItem(item);
        return bi != null ? bi.getNum() : 0;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public void clear() {
        data.clear();
    }

    /**
     * A zsákban lévő különböző elemek. Mindig új listát adunk vissza, így a
     * hívó bárhogy módosíthatja, a zsák belső állapotához nem fér hozzá.
     */
    public List<String> items() {
        List<String> items = new ArrayList<>();
        for (BagItem bi : data) {
            items.add(bi.getItem());
        }
        return items;
    }

    /**
     * Unió: a két zsák "összeöntése", a darabszámok összeadódnak.
     * Új zsákot ad vissza, sem ez a zsák, sem a paraméter nem változik.
     *
     * Az other.data elérhető, pedig private: a private láthatóság osztályra
     * vonatkozik, nem példányra, így egy Bag látja egy másik Bag adattagjait.
     */
    public Bag union(Bag other) {
        Bag result = new Bag();
        // Az add új BagItem-eket hoz létre, így az eredmény nem osztozik
        // módosítható objektumokon a két eredeti zsákkal.
        for (BagItem bi : other.data) {
            result.add(bi.getItem(), bi.getNum());
        }
        for (BagItem bi : data) {
            result.add(bi.getItem(), bi.getNum());
        }
        return result;
    }

    /**
     * Metszet: minden elemből annyi, amennyi mindkét zsákban megvan, vagyis
     * a két darabszám minimuma.
     */
    public Bag intersection(Bag other) {
        Bag result = new Bag();
        for (BagItem bi : data) {
            // Ha az elem nincs a másik zsákban, a howMany 0-t ad, így a
            // minimum is 0 - ilyenkor nem teszünk semmit az eredménybe
            // (az add nem is engedné).
            int common = Math.min(bi.getNum(), other.howMany(bi.getItem()));
            if (common > 0) {
                result.add(bi.getItem(), common);
            }
        }
        return result;
    }

    /**
     * Különbség: ebből a zsákból elemenként kivesszük azt, ami a másikban
     * van. Ami így elfogyna (vagy negatívba menne), az kimarad.
     * Nem szimmetrikus: a.difference(b) általában más, mint b.difference(a).
     */
    public Bag difference(Bag other) {
        Bag result = new Bag();
        for (BagItem bi : data) {
            int left = bi.getNum() - other.howMany(bi.getItem());
            if (left > 0) {
                result.add(bi.getItem(), left);
            }
        }
        return result;
    }

    /**
     * Lineáris keresés: a bejegyzést adja vissza, vagy null-t, ha nincs ilyen
     * elem. Private, mert a BagItem belső részlet, nem adjuk ki a kezünkből.
     */
    private BagItem getItem(String item) {
        for (BagItem bi : data) {
            // Stringet (és általában objektumot) equals-szal hasonlítunk,
            // nem ==-vel: az == csak azt nézné, ugyanaz-e a két referencia.
            if (bi.getItem().equals(item)) {
                return bi;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Bag{" + "data=" + data + '}';
    }
}
