package edu.hitsz.aircraftwar.game.shoot;

import edu.hitsz.aircraftwar.game.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar.game.bullet.BaseBullet;

import java.util.ArrayList;
import java.util.List;

public final class CircularShoot implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        int count = aircraft.getShootCount();
        List<BaseBullet> bullets = new ArrayList<>(count);
        int x = aircraft.getLocationX() + aircraft.getDirection() * 2;
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2;
        int speedY = aircraft.getSpeedY() + aircraft.getDirection() * 5;
        int speed = (int) Math.hypot(10, speedY);
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double horizontal = Math.cos(angle);
            double vertical = Math.sin(angle);
            bullets.add(ShootStrategy.createBullet(aircraft,
                    (int) (x + horizontal * 2), (int) (y + vertical * 2),
                    (int) (speed * horizontal), (int) (speed * vertical)));
        }
        return bullets;
    }
}
