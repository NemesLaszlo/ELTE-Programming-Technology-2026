/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zoo;

/**
 *
 * @author bli
 */
public class Hamster extends Pet {

    @Override
    public void move() {
        System.out.println("Hamster scurries");
    }

    @Override
    public void makeSound() {
        System.out.println("Hamster squeaks");
    }
    
}
