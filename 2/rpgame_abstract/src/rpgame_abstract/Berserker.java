package rpgame_abstract;

/**
 * Berserker orc: takes double damage.
 */
public class Berserker extends Orc {

    public Berserker(String name, int HP, int attack) {
        super(name, HP, attack);
    }

    @Override
    protected void applyDamageFrom(Character character) {
        HP -= character.attack * 2;
    }

}
