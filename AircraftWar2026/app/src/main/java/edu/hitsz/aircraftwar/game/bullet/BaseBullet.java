package edu.hitsz.aircraftwar.game.bullet;

import edu.hitsz.aircraftwar.game.AbstractFlyingObject;
import edu.hitsz.aircraftwar.game.Sprite;

public abstract class BaseBullet extends AbstractFlyingObject {
    private final int power;

    protected BaseBullet(int x, int y, int speedX, int speedY, int power, Sprite sprite) {
        super(x, y, speedX, speedY, sprite);
        this.power = power;
    }

    @Override
    public void forward() {
        super.forward();
        if (locationX <= 0 || locationX >= FIELD_WIDTH || locationY <= 0 || locationY >= FIELD_HEIGHT) {
            vanish();
        }
    }

    public int getPower() {
        return power;
    }
}
