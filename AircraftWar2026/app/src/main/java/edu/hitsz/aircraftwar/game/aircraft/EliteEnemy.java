package edu.hitsz.aircraftwar.game.aircraft;

import edu.hitsz.aircraftwar.game.Sprite;

public final class EliteEnemy extends AbstractEnemy {
    public EliteEnemy(int x, int y, int speedY, int hp) {
        super(x, y, 0, speedY, Sprite.ELITE, hp, 2, 10);
    }

    @Override
    public void onFreeze() {
        freeze(4000);
    }
}
