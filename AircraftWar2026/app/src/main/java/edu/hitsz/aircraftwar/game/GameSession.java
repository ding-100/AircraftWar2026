package edu.hitsz.aircraftwar.game;

import edu.hitsz.aircraftwar.game.aircraft.AbstractEnemy;
import edu.hitsz.aircraftwar.game.aircraft.BossEnemy;
import edu.hitsz.aircraftwar.game.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar.game.bullet.BaseBullet;
import edu.hitsz.aircraftwar.game.bullet.EnemyBullet;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/** Owns one round. The game loop is the only thread that changes it. */
public final class GameSession {
    private static final int BOSS_SCORE_INTERVAL = 300;
    private static final int DIFFICULTY_TICKS = 600;

    private final Difficulty difficulty;
    private final Random random;
    private final RandomEnemySpawner enemySpawner;
    private final ShieldSkill shield;
    private final HeroAircraft hero = new HeroAircraft();
    private final List<AbstractEnemy> enemies = new ArrayList<>();
    private final List<BaseBullet> heroBullets = new ArrayList<>();
    private final List<BaseBullet> enemyBullets = new ArrayList<>();
    private final List<Supply> supplies = new ArrayList<>();

    private int ticks;
    private int spawnCounter;
    private int heroShotCounter;
    private int enemyShotCounter;
    private int bossShotCounter;
    private int backgroundOffset;
    private int score;
    private int nextBossScore = BOSS_SCORE_INTERVAL;
    private BossEnemy boss;

    public GameSession(Difficulty difficulty) {
        this(difficulty, new Random());
    }

    GameSession(Difficulty difficulty, Random random) {
        this.difficulty = difficulty;
        this.random = random;
        enemySpawner = new RandomEnemySpawner(random, difficulty);
        shield = difficulty == Difficulty.HARD ? new ShieldSkill() : null;
    }

    public void moveHero(int x, int y) {
        hero.moveTo(x, y);
    }

    public EnumSet<GameEvent> update() {
        EnumSet<GameEvent> events = EnumSet.noneOf(GameEvent.class);
        if (isGameOver()) {
            return events;
        }
        boolean wasBossActive = boss != null;
        ticks++;
        int level = difficulty == Difficulty.EASY ? 0 : ticks / DIFFICULTY_TICKS;
        backgroundOffset = (backgroundOffset + 1) % AbstractFlyingObject.FIELD_HEIGHT;
        if (shield != null) {
            shield.update();
        }
        spawnEnemies(level);
        shoot(level);
        hero.forward();
        enemies.forEach(AbstractFlyingObject::forward);
        heroBullets.forEach(AbstractFlyingObject::forward);
        enemyBullets.forEach(AbstractFlyingObject::forward);
        supplies.forEach(AbstractFlyingObject::forward);
        resolveEnemyBullets(events);
        resolveHeroBullets(events);
        resolveEnemyCollisions();
        if (isGameOver()) {
            events.add(GameEvent.GAME_OVER);
        } else {
            collectSupplies(events);
        }
        removeInactiveObjects();
        if (wasBossActive != (boss != null)) {
            events.add(boss == null ? GameEvent.BOSS_LEFT : GameEvent.BOSS_ENTERED);
        }
        return events;
    }

    private void spawnEnemies(int level) {
        int spawnInterval = Math.max(20, difficulty.spawnInterval - level * 2);
        if (++spawnCounter >= spawnInterval) {
            spawnCounter = 0;
            int regularCount = enemies.size() - (boss == null ? 0 : 1);
            if (regularCount < difficulty.enemyLimit) {
                enemies.add(enemySpawner.createRegularEnemy(level));
            }
        }
        if (difficulty != Difficulty.EASY && boss == null && score >= nextBossScore) {
            boss = enemySpawner.createBoss();
            enemies.add(boss);
            nextBossScore += BOSS_SCORE_INTERVAL;
            bossShotCounter = 0;
        }
    }

    private void shoot(int level) {
        int heroInterval = difficulty == Difficulty.HARD ? Math.max(5, 20 - level * 2) : 20;
        if (++heroShotCounter >= heroInterval) {
            heroBullets.addAll(hero.shoot());
            heroShotCounter = 0;
        }
        int enemyInterval = difficulty == Difficulty.HARD
                ? Math.max(10, difficulty.enemyShootInterval - level * 2) : difficulty.enemyShootInterval;
        if (++enemyShotCounter >= enemyInterval) {
            for (AbstractEnemy enemy : enemies) {
                if (enemy != boss && enemy.getShootCount() > 0) {
                    enemyBullets.addAll(enemy.shoot());
                }
            }
            enemyShotCounter = 0;
        }
        if (boss != null && ++bossShotCounter >= enemyInterval * 2) {
            enemyBullets.addAll(boss.shoot());
            bossShotCounter = 0;
        }
    }

    private void resolveEnemyBullets(EnumSet<GameEvent> events) {
        for (BaseBullet bullet : enemyBullets) {
            if (!bullet.notValid() && hero.crash(bullet)) {
                bullet.vanish();
                if (!isShieldActive()) {
                    hero.decreaseHp(bullet.getPower());
                    events.add(GameEvent.BULLET_HIT);
                }
            }
        }
    }

    private void resolveHeroBullets(EnumSet<GameEvent> events) {
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractEnemy enemy : enemies) {
                if (!enemy.notValid() && enemy.crash(bullet)) {
                    bullet.vanish();
                    enemy.decreaseHp(bullet.getPower());
                    events.add(GameEvent.BULLET_HIT);
                    if (enemy.notValid()) {
                        score += enemy.getPoints();
                        dropSupplies(enemy);
                    }
                    break;
                }
            }
        }
    }

    private void resolveEnemyCollisions() {
        for (AbstractEnemy enemy : enemies) {
            if (!enemy.notValid() && hero.crash(enemy)) {
                enemy.vanish();
                if (!isShieldActive()) {
                    hero.decreaseHp(hero.getHp());
                }
            }
        }
    }

    private void dropSupplies(AbstractEnemy enemy) {
        int dropCount = enemy.getDropCount();
        for (int i = 0; i < dropCount; i++) {
            int x = enemy.getLocationX() + (i - dropCount / 2) * 30;
            int speed = Math.max(3, enemy.getSpeedY());
            supplies.add(new Supply(x, enemy.getLocationY(), speed, random.nextInt(5)));
        }
    }

    private void collectSupplies(EnumSet<GameEvent> events) {
        for (Supply supply : supplies) {
            if (supply.notValid() || !hero.crash(supply)) {
                continue;
            }
            switch (supply.getSprite()) {
                case HEALTH -> hero.restoreHp(30);
                case FIRE -> hero.boostFire(false);
                case SUPER_FIRE -> hero.boostFire(true);
                case BOMB -> {
                    enemies.forEach(AbstractEnemy::onBomb);
                    enemyBullets.forEach(AbstractFlyingObject::vanish);
                }
                case FREEZE -> {
                    enemies.forEach(AbstractEnemy::onFreeze);
                    for (BaseBullet bullet : enemyBullets) {
                        if (bullet instanceof EnemyBullet enemyBullet) {
                            enemyBullet.onFreeze();
                        }
                    }
                }
                default -> throw new IllegalStateException("Unexpected supply: " + supply.getSprite());
            }
            events.add(supply.getSprite() == Sprite.BOMB
                    ? GameEvent.BOMB_EXPLODED : GameEvent.SUPPLY_COLLECTED);
            supply.vanish();
        }
    }

    private void removeInactiveObjects() {
        enemies.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        supplies.removeIf(AbstractFlyingObject::notValid);
        if (boss != null && boss.notValid()) {
            boss = null;
        }
    }

    public HeroAircraft getHero() {
        return hero;
    }

    public List<? extends AbstractFlyingObject> getEnemies() {
        return enemies;
    }

    public List<? extends AbstractFlyingObject> getHeroBullets() {
        return heroBullets;
    }

    public List<? extends AbstractFlyingObject> getEnemyBullets() {
        return enemyBullets;
    }

    public List<? extends AbstractFlyingObject> getSupplies() {
        return supplies;
    }

    public int getBackgroundOffset() {
        return backgroundOffset;
    }

    public int getScore() {
        return score;
    }

    public boolean isShieldActive() {
        return shield != null && shield.isActive();
    }

    public boolean isGameOver() {
        return hero.getHp() == 0;
    }
}
