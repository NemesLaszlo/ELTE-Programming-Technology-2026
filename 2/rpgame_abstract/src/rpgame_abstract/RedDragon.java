package rpgame_abstract;

/**
 * Red dragon: only hurt by attacks greater than 60.
 */
public class RedDragon extends Dragon {

    public RedDragon(String name, int HP, int attack) {
        super(name, HP, attack, 60);
    }

}
