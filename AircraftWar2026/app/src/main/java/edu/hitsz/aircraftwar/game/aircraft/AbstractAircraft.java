package edu.hitsz.aircraftwar.game.aircraft;

import edu.hitsz.aircraftwar.game.AbstractFlyingObject;
import edu.hitsz.aircraftwar.game.Sprite;
import edu.hitsz.aircraftwar.game.bullet.BaseBullet;
import edu.hitsz.aircraftwar.game.shoot.DirectShoot;
import edu.hitsz.aircraftwar.game.shoot.ShootStrategy;

import java.util.List;

public abstract class AbstractAircraft extends AbstractFlyingObject {
    private final int maxHp;
    private int hp;
    protected int shootCount;
    private final int power;
    private final int direction;
    protected ShootStrategy shootStrategy = new DirectShoot();

    protected AbstractAircraft(int x, int y, int speedX, int speedY, Sprite sprite,
                              int hp, int shootCount, int power, int direction) {
        super(x, y, speedX, speedY, sprite);
        this.hp = hp;
        maxHp = hp;
        this.shootCount = shootCount;
        this.power = power;
        this.direction = direction;
    }

    public void decreaseHp(int damage) {
        hp = Math.max(0, hp - damage);
        if (hp == 0) {
            vanish();
        }
    }

    public void restoreHp(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public int getHp() {
        return hp;
    }

    public int getShootCount() {
        return shootCount;
    }

    public int getPower() {
        return power;
    }

    public int getDirection() {
        return direction;
    }

    public List<BaseBullet> shoot() {
        return shootStrategy.shoot(this);
    }
}
