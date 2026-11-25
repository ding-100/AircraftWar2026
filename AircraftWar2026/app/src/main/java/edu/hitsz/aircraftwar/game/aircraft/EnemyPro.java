package edu.hitsz.aircraftwar.game.aircraft;

import edu.hitsz.aircraftwar.game.Sprite;

public final class EnemyPro extends AbstractEnemy {
    public EnemyPro(int x, int y, int speedX, int speedY, int hp) {
        super(x, y, speedX, speedY, Sprite.ELITE_PRO, hp, 2, 10);
    }

    @Override
    public void onFreeze() {
        slow(2, 5000);
    }
}
