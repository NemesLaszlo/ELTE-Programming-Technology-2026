package rpgame_abstract;

/**
 * Shield-bearer orc: takes half damage.
 */
public class Defender extends Orc {

    public Defender(String name, int HP, int attack) {
        super(name, HP, attack);
    }

    @Override
    protected void applyDamageFrom(Character character) {
        HP -= character.attack / 2;
    }

}
