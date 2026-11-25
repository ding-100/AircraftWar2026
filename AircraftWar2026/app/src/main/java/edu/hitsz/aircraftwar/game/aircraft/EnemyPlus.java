package edu.hitsz.aircraftwar.game.aircraft;

import edu.hitsz.aircraftwar.game.Sprite;
import edu.hitsz.aircraftwar.game.shoot.ScatterShoot;

public final class EnemyPlus extends AbstractEnemy {
    public EnemyPlus(int x, int y, int speedX, int speedY, int hp) {
        super(x, y, speedX, speedY, Sprite.ELITE_PLUS, hp, 3, 10);
        shootStrategy = new ScatterShoot();
    }

    @Override
    public void onBomb() {
        decreaseHp(60);
    }

    @Override
    public void onFreeze() {
        freeze(3000);
    }
}
