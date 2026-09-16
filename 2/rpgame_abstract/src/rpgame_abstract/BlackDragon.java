package rpgame_abstract;

/**
 * Black dragon: only hurt by attacks greater than 20.
 */
public class BlackDragon extends Dragon {

    public BlackDragon(String name, int HP, int attack) {
        super(name, HP, attack, 20);
    }

}
