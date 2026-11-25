package edu.hitsz.aircraftwar.game.shoot;

import edu.hitsz.aircraftwar.game.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar.game.bullet.BaseBullet;
import edu.hitsz.aircraftwar.game.bullet.EnemyBullet;
import edu.hitsz.aircraftwar.game.bullet.HeroBullet;

import java.util.List;

public interface ShootStrategy {
    List<BaseBullet> shoot(AbstractAircraft aircraft);

    static BaseBullet createBullet(AbstractAircraft aircraft, int x, int y, int speedX, int speedY) {
        if (aircraft.getDirection() < 0) {
            return new HeroBullet(x, y, speedX, speedY, aircraft.getPower());
        }
        return new EnemyBullet(x, y, speedX, speedY, aircraft.getPower());
    }
}
