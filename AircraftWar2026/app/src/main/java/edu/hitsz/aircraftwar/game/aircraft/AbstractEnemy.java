package edu.hitsz.aircraftwar.game.aircraft;

import edu.hitsz.aircraftwar.game.Sprite;

public abstract class AbstractEnemy extends AbstractAircraft {
    protected AbstractEnemy(int x, int y, int speedX, int speedY, Sprite sprite, int hp,
                            int shootCount, int power) {
        super(x, y, speedX, speedY, sprite, hp, shootCount, power, 1);
    }

    @Override
    public void forward() {
        super.forward();
        int halfWidth = getSprite().width / 2;
        if (locationX < halfWidth || locationX > FIELD_WIDTH - halfWidth) {
            locationX = Math.max(halfWidth, Math.min(locationX, FIELD_WIDTH - halfWidth));
            speedX = -speedX;
        }
        if (locationY >= FIELD_HEIGHT) {
            vanish();
        }
    }

    public int getPoints() {
        return 20;
    }

    public int getDropCount() {
        return 1;
    }

    public void onBomb() {
        vanish();
    }

    public abstract void onFreeze();
}
