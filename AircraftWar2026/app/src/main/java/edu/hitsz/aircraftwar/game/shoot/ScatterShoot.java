package edu.hitsz.aircraftwar.game.shoot;

import edu.hitsz.aircraftwar.game.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar.game.bullet.BaseBullet;

import java.util.ArrayList;
import java.util.List;

public final class ScatterShoot implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        int count = aircraft.getShootCount();
        List<BaseBullet> bullets = new ArrayList<>(count);
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2;
        int speedY = aircraft.getSpeedY() + aircraft.getDirection() * 5;
        for (int i = 0; i < count; i++) {
            int offset = i * 2 - count + 1;
            int x = aircraft.getLocationX() + offset * 10;
            bullets.add(ShootStrategy.createBullet(aircraft, x, y, offset * 5, speedY));
        }
        return bullets;
    }
}
