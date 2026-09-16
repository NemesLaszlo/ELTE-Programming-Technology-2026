package rpgame_abstract;

/**
 * The hero. Has a real-valued defense; incoming attack is divided by it.
 */
public class MainCharacter extends Character {

    protected double defense;

    public MainCharacter(String name, int HP, int attack, double defense) {
        super(name, HP, attack);
        this.defense = defense;
    }

    @Override
    protected void applyDamageFrom(Character character) {
        HP -= character.attack / defense;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

}
