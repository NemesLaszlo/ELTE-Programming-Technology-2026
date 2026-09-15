/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zoo;

import java.util.ArrayList;

/**
 *
 * @author bli
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<Animal> animals = new ArrayList<>();
        animals.add(new Dog());
        animals.add(new Lion());
        animals.add(new Hamster());
        animals.add(new Cat());
        Zoo zoo = new Zoo(animals);
        zoo.step();
    }

}
