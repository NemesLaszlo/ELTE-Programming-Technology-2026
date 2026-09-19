package bag;

/**
 * A zsák műveleteinek kipróbálása.
 *
 * Csak a Bag publikus műveleteit használja, ezért ugyanez a főprogram
 * változtatás nélkül működik az ArrayList-es és a HashMap-es zsákkal is.
 */
public class TestBag {

    public static void main(String[] args) {
        Bag bag = new Bag();
        Bag bag2 = new Bag();
        bag.add("a", 20);
        bag.add("a", 1); // meglévő elem: csak a darabszáma nő (a=21)
        bag.add("b", 33);
        bag.add("c", 32);
        bag2.add("a", 42);
        bag2.add("c", 12);

        // String + objektum összefűzésekor a toString() automatikusan
        // meghívódik.
        System.out.println("The union of " + bag + " and " + bag2 + " is " + bag.union(bag2) + ".");
        System.out.println("The intersection of " + bag + " and " + bag2 + " is " + bag.intersection(bag2) + ".");
        System.out.println("The difference of " + bag + " and " + bag2 + " is " + bag.difference(bag2) + ".");

        // A halmazműveletek új zsákot adtak vissza, az eredetiek nem
        // változtak. A remove viszont helyben módosít:
        System.out.println("b: " + bag.howMany("b") + " pcs");
        bag.remove("b", 30);
        System.out.println("b after removing 30: " + bag.howMany("b") + " pcs");
        bag.remove("b", 30); // több, mint amennyi van: az elem teljesen kikerül
        System.out.println("contains b after removing 30 more: " + bag.contains("b"));
        System.out.println("x (never added): " + bag.howMany("x") + " pcs");
    }
}
