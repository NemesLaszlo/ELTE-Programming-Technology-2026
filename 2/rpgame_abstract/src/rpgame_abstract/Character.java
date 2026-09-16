package rpgame_abstract;

/**
 * Common base of every participant of the game.
 *
 * Every character has a name, hit points (HP) and an attack value. The
 * general attack/attacked protocol is fixed here, but the way an incoming
 * hit is turned into damage is left abstract: every concrete kind of
 * character must define its own rule in {@link #applyDamageFrom(Character)}.
 */
public abstract class Character {

    protected String name;
    protected int HP;
    protected int attack;

    public Character(String name, int HP, int attack) {
        this.name = name;
        this.HP = HP;
        this.attack = attack;
    }

    public void attack(Character character) {
        if (isAlive()) {
            character.attacked(this);
        }
    }

    private void attacked(Character character) {
        if (isAlive()) {
            applyDamageFrom(character);
        }
    }

    public boolean isAlive() {
        return HP >= 0;
    }

    /**
     * Reduces this character's HP according to a hit received from
     * {@code character}. Abstract: there is no "default" damage rule,
     * every concrete subclass has to decide how much damage it takes.
     *
     * @param character the attacker
     */
    protected abstract void applyDamageFrom(Character character);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHP() {
        return HP;
    }

    public void setHP(int HP) {
        this.HP = HP;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

}
