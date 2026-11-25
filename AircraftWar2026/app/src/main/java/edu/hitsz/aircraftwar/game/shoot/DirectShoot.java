package edu.hitsz.aircraftwar.game.shoot;

import edu.hitsz.aircraftwar.game.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar.game.bullet.BaseBullet;

import java.util.ArrayList;
import java.util.List;

public final class DirectShoot implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        int count = aircraft.getShootCount();
        List<BaseBullet> bullets = new ArrayList<>(count);
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2;
        int speedY = aircraft.getSpeedY() + aircraft.getDirection() * 5;
        for (int i = 0; i < count; i++) {
            int x = aircraft.getLocationX() + (i * 2 - count + 1) * 10;
            bullets.add(ShootStrategy.createBullet(aircraft, x, y, 0, speedY));
        }
        return bullets;
    }
}
