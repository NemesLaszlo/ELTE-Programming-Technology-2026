package sort2;

import java.util.Comparator;

/**
 * Bármilyen két objektumot összehasonlít a szöveges alakjuk (toString)
 * hossza alapján.
 *
 * {@code Comparator<Object>}, vagyis nem kötődik egyetlen elemtípushoz sem:
 * toString-je mindennek van. Ezzel a példával látszik, mire jó a Sort2.sort
 * paraméterében a {@code Comparator<? super T>}: ugyanezt az egy
 * összehasonlítót {@code List<Integer>}-hez és {@code List<String>}-hez is
 * átadhatjuk, mert az Object az Integer-nek is, a String-nek is őse.
 */
public class TextLengthComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.toString().length(), o2.toString().length());
    }

}
