package rpgame_abstract;

/**
 * Abstract base of the orc kinds. It does not implement
 * {@code applyDamageFrom}, so it stays abstract and each orc type
 * (Fighter, Berserker, Defender) gives its own damage rule.
 */
public abstract class Orc extends Character {

    public Orc(String name, int HP, int attack) {
        super(name, HP, attack);
    }

}
