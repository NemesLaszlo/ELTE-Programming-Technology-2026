package bag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Zsák (multihalmaz) HashMap-pel megvalósítva: elem -> darabszám.
 *
 * Az ArrayList-es változathoz képest nincs szükség külön BagItem osztályra
 * és lineáris keresésre: a HashMap a kulcs hash-kódja alapján átlagosan
 * konstans idő alatt találja meg az elemet. Ehhez a kulcs típusának
 * helyes equals és hashCode metódus kell - a String-nek van.
 *
 * Az ábrázolás invariánsa: minden tárolt darabszám pozitív (ami elfogy, azt
 * töröljük a map-ből).
 *
 * A HashMap nem őrzi meg a betétel sorrendjét, ezért a toString kimenetében
 * az elemek sorrendje tetszőleges. Ha ez számít: LinkedHashMap (betétel
 * sorrendje) vagy TreeMap (kulcs szerint rendezett).
 */
public class Bag {

    // Map<String, Integer>: a Map-nek két típusparamétere van, sorrendben a
    // kulcs és az érték típusa - itt String kulcsokhoz (elem) Integer
    // értékeket (darabszám) rendelünk. A fordító ellenőrzi, hogy csak ilyen
    // párok kerülhetnek bele, és a get eredményét sem kell cast-olni.
    //
    // Az értékek típusa Integer és nem int, mert típusparaméter nem lehet
    // primitív. Az int <-> Integer átalakítást a fordító elvégzi
    // (autoboxing).
    //
    // A változó típusa az interfész (Map), csak a példányosításnál döntünk a
    // konkrét megvalósításról - így az később egyetlen helyen lecserélhető.
    private final Map<String, Integer> data;

    public Bag() {
        // Az üres < > ("gyémánt") rövidítés: a fordító a data típusából
        // kitalálja, hogy new HashMap<String, Integer>()-ről van szó.
        data = new HashMap<>();
    }

    /**
     * Betesz num darabot az elemből.
     *
     * @throws IllegalArgumentException ha num nem pozitív
     */
    public void add(String item, int num) {
        Objects.requireNonNull(item, "item must not be null");
        if (num <= 0) {
            throw new IllegalArgumentException("num must be positive: " + num);
        }
        // A put felülírja a kulcshoz tartozó korábbi értéket, ezért az új
        // darabszámot nekünk kell kiszámolni. (Ugyanezt egy lépésben tudja a
        // data.merge(item, num, Integer::sum).)
        data.put(item, howMany(item) + num);
    }

    public boolean contains(String item) {
        return data.containsKey(item);
    }

    /**
     * Az elem összes példányát kiveszi.
     *
     * @return hány darab volt belőle, vagy null, ha nem volt a zsákban -
     * pontosan ezt adja a Map.remove is
     */
    public Integer remove(String item) {
        return data.remove(item);
    }

    /**
     * Kivesz num darabot az elemből. Ha nincs belőle ennyi, mindet kiveszi.
     *
     * @return igaz, ha az elem benne volt a zsákban
     * @throws IllegalArgumentException ha num nem pozitív
     */
    public boolean remove(String item, int num) {
        // E nélkül az ellenőrzés nélkül negatív num-mal növelni lehetne a
        // darabszámot.
        if (num <= 0) {
            throw new IllegalArgumentException("num must be positive: " + num);
        }
        if (!data.containsKey(item)) {
            return false;
        }
        int left = data.get(item) - num;
        if (left > 0) {
            data.put(item, left);
        } else {
            // 0 darabos bejegyzést nem hagyunk a map-ben (invariáns).
            data.remove(item);
        }
        return true;
    }

    /**
     * @return hány darab van az elemből; 0, ha nincs a zsákban
     */
    public int howMany(String item) {
        // A sima get hiányzó kulcsra null-t ad, amit int-ként használva
        // NullPointerException-t kapnánk. A getOrDefault ezt kezeli le.
        return data.getOrDefault(item, 0);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public void clear() {
        data.clear();
    }

    /**
     * A zsákban lévő különböző elemek. A keySet() csak "nézet" a map-re: ami
     * abból törlődik, az a map-ből is. Ezért másolatot adunk vissza, így a
     * hívó nem tud belenyúlni a zsák belső állapotába.
     */
    public List<String> items() {
        return new ArrayList<>(data.keySet());
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
        // Map bejárása: az entrySet() a kulcs-érték párokat adja. Ez jobb,
        // mint a keySet()-en végigmenni és minden kulcsra get-et hívni.
        // Map.Entry<String, Integer>: egy ilyen pár típusa. Az Entry a Map
        // interfészen belül deklarált interfész, ezért a pont a nevében; a
        // típusparaméterei ugyanazok, mint a map-é (kulcs, érték).
        for (Map.Entry<String, Integer> entry : other.data.entrySet()) {
            result.add(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            result.add(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Metszet: minden elemből annyi, amennyi mindkét zsákban megvan, vagyis
     * a két darabszám minimuma.
     *
     * Csak a saját elemeinken megyünk végig, a másik zsákból kulcs alapján
     * kérdezünk (howMany). Két egymásba ágyazott ciklussal is menne, de
     * azzal pont a HashMap előnyét, a gyors keresést dobnánk el.
     */
    public Bag intersection(Bag other) {
        Bag result = new Bag();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            // Ha az elem nincs a másik zsákban, a howMany 0-t ad, így a
            // minimum is 0 - ilyenkor nem teszünk semmit az eredménybe
            // (az add nem is engedné).
            int common = Math.min(entry.getValue(), other.howMany(entry.getKey()));
            if (common > 0) {
                result.add(entry.getKey(), common);
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
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int left = entry.getValue() - other.howMany(entry.getKey());
            if (left > 0) {
                result.add(entry.getKey(), left);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Bag{" + "data=" + data + '}';
    }
}
