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
public abstract class Pet extends Animal {

    @Override
    public void move() {
        System.out.println("Pet moves lazily");
    }

}
