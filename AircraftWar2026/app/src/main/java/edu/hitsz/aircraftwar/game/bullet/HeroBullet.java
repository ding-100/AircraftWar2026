package edu.hitsz.aircraftwar.game.bullet;

import edu.hitsz.aircraftwar.game.Sprite;

public final class HeroBullet extends BaseBullet {
    public HeroBullet(int x, int y, int speedX, int speedY, int power) {
        super(x, y, speedX, speedY, power, Sprite.HERO_BULLET);
    }
}
