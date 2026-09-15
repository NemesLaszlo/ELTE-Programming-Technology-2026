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
public class Lion extends WildAnimal {

    @Override
    public void move() {
        System.out.println("Lion stalks");
    }    
    
    @Override
    public void makeSound() {
        
        System.out.println("Lion roars");
    }
    
}
