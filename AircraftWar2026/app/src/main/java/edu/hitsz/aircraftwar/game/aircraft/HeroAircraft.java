package edu.hitsz.aircraftwar.game.aircraft;

import edu.hitsz.aircraftwar.game.Sprite;
import edu.hitsz.aircraftwar.game.shoot.CircularShoot;
import edu.hitsz.aircraftwar.game.shoot.DirectShoot;
import edu.hitsz.aircraftwar.game.shoot.ScatterShoot;

public final class HeroAircraft extends AbstractAircraft {
    private int firePowerTicks;

    public HeroAircraft() {
        super(FIELD_WIDTH / 2, FIELD_HEIGHT - Sprite.HERO.height, 0, 0, Sprite.HERO, 100, 1, 50, -1);
    }

    public void moveTo(int x, int y) {
        int halfWidth = getSprite().width / 2;
        int halfHeight = getSprite().height / 2;
        locationX = Math.max(halfWidth, Math.min(x, FIELD_WIDTH - halfWidth));
        locationY = Math.max(halfHeight, Math.min(y, FIELD_HEIGHT - halfHeight));
    }

    public void boostFire(boolean isSuper) {
        shootCount = isSuper ? 20 : 3;
        shootStrategy = isSuper ? new CircularShoot() : new ScatterShoot();
        firePowerTicks = 5000 / STEP_MILLIS;
    }

    @Override
    public void forward() {
        if (firePowerTicks > 0 && --firePowerTicks == 0) {
            shootCount = 1;
            shootStrategy = new DirectShoot();
        }
    }
}
