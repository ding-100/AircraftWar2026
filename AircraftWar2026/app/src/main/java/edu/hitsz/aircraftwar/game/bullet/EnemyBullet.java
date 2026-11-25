package edu.hitsz.aircraftwar.game.bullet;

import edu.hitsz.aircraftwar.game.Sprite;

public final class EnemyBullet extends BaseBullet {
    public EnemyBullet(int x, int y, int speedX, int speedY, int power) {
        super(x, y, speedX, speedY, power, Sprite.ENEMY_BULLET);
    }

    public void onFreeze() {
        freeze(5000);
    }
}
