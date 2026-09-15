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
public class Dog extends Pet {

    @Override
    public void move() {
        System.out.println("Dog runs");
    }
    
    @Override
    public void makeSound() {        
        System.out.println("Dog barks");
    }
    
}
