package rpgame_abstract;

/**
 * Abstract dragon: can only be hurt if the attacker's attack exceeds a
 * colour-specific threshold. The damage rule is shared by both colours,
 * so it is implemented here; the subclasses only fix the threshold.
 */
public abstract class Dragon extends Character {

    protected final int ATTACK_THRESHOLD;

    public Dragon(String name, int HP, int attack, int ATTACK_THRESHOLD) {
        super(name, HP, attack);
        this.ATTACK_THRESHOLD = ATTACK_THRESHOLD;
    }

    @Override
    protected void applyDamageFrom(Character character) {
        if (character.attack > ATTACK_THRESHOLD) {
            HP -= character.attack;
        }
    }

    /**
     * The threshold is final (fixed by the dragon's colour), so it only
     * has a getter and no setter.
     */
    public int getAttackThreshold() {
        return ATTACK_THRESHOLD;
    }

}
