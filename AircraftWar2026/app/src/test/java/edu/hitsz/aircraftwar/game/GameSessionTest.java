package edu.hitsz.aircraftwar.game;

import edu.hitsz.aircraftwar.game.aircraft.MobEnemy;
import edu.hitsz.aircraftwar.game.bullet.HeroBullet;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class GameSessionTest {
    @Test
    public void heroStaysWithinThePlayfield() {
        GameSession session = centeredMobs();
        session.moveHero(-100, 1000);

        assertEquals(Sprite.HERO.width / 2, session.getHero().getLocationX());
        assertEquals(AbstractFlyingObject.FIELD_HEIGHT - Sprite.HERO.height / 2,
                session.getHero().getLocationY());
    }

    @Test
    public void oneBulletDestroysOneMobAndAddsTenPoints() {
        GameSession session = centeredMobs();
        for (int tick = 0; tick < 120 && session.getScore() == 0; tick++) {
            session.update();
        }

        assertEquals(10, session.getScore());
        assertEquals(100, session.getHero().getHp());
    }

    @Test
    public void bodyCollisionEndsTheRoundWithoutAProjectile() {
        GameSession session = centeredMobs();
        for (int tick = 0; tick < 50; tick++) {
            session.update();
        }
        session.moveHero(256, 60);
        for (int tick = 50; tick < 59; tick++) {
            session.update();
        }

        assertTrue(session.isGameOver());
        assertEquals(0, session.getScore());
    }

    @Test
    public void enemyAndBulletOutsideOfThePlayfieldAreRemoved() {
        MobEnemy enemy = new MobEnemy(256, 767, 6, 30);
        HeroBullet bullet = new HeroBullet(256, 1, 0, -5, 50);

        enemy.forward();
        bullet.forward();

        assertTrue(enemy.notValid());
        assertTrue(bullet.notValid());
    }

    @Test
    public void collisionUsesTheAircraftInnerHeight() {
        MobEnemy enemy = new MobEnemy(100, 100, 6, 30);

        assertTrue(enemy.crash(new HeroBullet(100, 124, 0, -5, 50)));
        assertFalse(enemy.crash(new HeroBullet(100, 125, 0, -5, 50)));
    }

    @Test
    public void newRoundHasIndependentHeroAndHardModeStartsWithShield() {
        GameSession first = centeredMobs();
        first.getHero().decreaseHp(100);

        GameSession second = new GameSession(Difficulty.HARD);
        assertEquals(100, second.getHero().getHp());
        assertTrue(second.isShieldActive());

        for (int tick = 0; tick < 125; tick++) {
            second.update();
        }
        assertFalse(second.isShieldActive());
    }

    private GameSession centeredMobs() {
        return new GameSession(Difficulty.EASY, new Random() {
            @Override
            public int nextInt(int bound) {
                return bound == 4 || bound == 39 ? 0 : bound / 2;
            }
        });
    }
}
