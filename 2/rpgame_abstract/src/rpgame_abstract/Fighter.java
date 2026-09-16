package rpgame_abstract;

/**
 * Plain orc warrior: takes exactly the attacker's attack value as damage.
 * Since applyDamageFrom is abstract in Character, this "plain" rule has to
 * be spelled out here explicitly.
 */
public class Fighter extends Orc {

    public Fighter(String name, int HP, int attack) {
        super(name, HP, attack);
    }

    @Override
    protected void applyDamageFrom(Character character) {
        HP -= character.attack;
    }

}
