package edu.hitsz.aircraftwar.game.aircraft;

import edu.hitsz.aircraftwar.game.Sprite;
import edu.hitsz.aircraftwar.game.shoot.CircularShoot;

public final class BossEnemy extends AbstractEnemy {
    public BossEnemy(int x, int y, int speedX, int hp) {
        super(x, y, speedX, 0, Sprite.BOSS, hp, 20, 10);
        shootStrategy = new CircularShoot();
    }

    @Override
    public int getDropCount() {
        return 3;
    }

    @Override
    public void onBomb() {
        // Boss is immune to bomb and freeze effects.
    }

    @Override
    public void onFreeze() {
    }
}
