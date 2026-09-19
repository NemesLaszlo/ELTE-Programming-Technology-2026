package bag;

import java.util.Objects;

/**
 * A zsák egy bejegyzése: egy elem és a darabszáma (multiplicitása).
 *
 * Nincs előtte public, vagyis csomagszintű láthatóságú: a Bag belső
 * ábrázolásának része, a bag csomagon kívül senkinek nem kell tudnia róla.
 */
class BagItem {

    // Az elem azonosítja a bejegyzést, ezért nem változhat (final);
    // a darabszám viszont igen.
    private final String item;
    private int num;

    public BagItem(String item, int num) {
        this.item = item;
        this.num = num;
    }

    public void addNum(int num) {
        this.num += num;
    }

    public String getItem() {
        return item;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    /**
     * Két bejegyzés akkor egyenlő, ha az elem és a darabszám is egyezik.
     *
     * A gyűjtemények kereső műveletei (pl. ArrayList.remove(Object),
     * contains, indexOf) az equals-t hívják. Ha nem definiáljuk felül, az
     * Object-től örökölt változat csak a referenciákat hasonlítja össze (==).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // Az instanceof null-ra is hamis, így külön null-vizsgálat nem kell.
        if (!(o instanceof BagItem)) {
            return false;
        }
        BagItem other = (BagItem) o;
        // Objects.equals: null-biztos összehasonlítás (az item lehet null).
        return Objects.equals(this.item, other.item) && this.num == other.num;
    }

    /**
     * Szabály: aki az equals-t felüldefiniálja, annak a hashCode-ot is kell,
     * mert egyenlő objektumoknak egyenlő hash-kódot kell adniuk - különben a
     * hash alapú gyűjtemények (HashSet, HashMap) hibásan működnek velük.
     *
     * Figyelem: a num változhat, és vele a hash-kód is. Ezért BagItem-et ne
     * tegyünk HashSet-be, és ne legyen HashMap kulcsa: ha betétel után
     * módosul, a gyűjtemény már nem találja meg. ArrayList-ben ez nem gond,
     * az nem használ hash-kódot.
     */
    @Override
    public int hashCode() {
        return Objects.hash(item, num);
    }

    @Override
    public String toString() {
        return item + "=" + num;
    }
}
