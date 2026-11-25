package edu.hitsz.aircraftwar.game.aircraft;

import edu.hitsz.aircraftwar.game.Sprite;

public final class MobEnemy extends AbstractEnemy {
    public MobEnemy(int x, int y, int speedY, int hp) {
        super(x, y, 0, speedY, Sprite.MOB, hp, 0, 0);
    }

    @Override
    public int getPoints() {
        return 10;
    }

    @Override
    public int getDropCount() {
        return 0;
    }

    @Override
    public void onFreeze() {
        speedY = 0;
    }
}
