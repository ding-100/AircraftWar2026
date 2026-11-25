package edu.hitsz.aircraftwar.game;

import edu.hitsz.aircraftwar.game.aircraft.AbstractEnemy;
import edu.hitsz.aircraftwar.game.aircraft.BossEnemy;
import edu.hitsz.aircraftwar.game.aircraft.EliteEnemy;
import edu.hitsz.aircraftwar.game.aircraft.EnemyPlus;
import edu.hitsz.aircraftwar.game.aircraft.EnemyPro;
import edu.hitsz.aircraftwar.game.aircraft.MobEnemy;

import java.util.Random;

import static edu.hitsz.aircraftwar.game.AbstractFlyingObject.FIELD_WIDTH;

final class RandomEnemySpawner {
    private final Random random;
    private final Difficulty difficulty;
    private int bossHp;

    RandomEnemySpawner(Random random, Difficulty difficulty) {
        this.random = random;
        this.difficulty = difficulty;
        bossHp = difficulty.bossHp;
    }

    AbstractEnemy createRegularEnemy(int level) {
        int y = random.nextInt(39);
        int hpBonus = level * 5;
        int mobSpeed = difficulty.mobSpeed + level;
        return switch (random.nextInt(4)) {
            case 0 -> new MobEnemy(spawnX(Sprite.MOB), y, mobSpeed, 30 + hpBonus);
            case 1 -> new EliteEnemy(spawnX(Sprite.ELITE), y, mobSpeed + 2, 30 + hpBonus);
            case 2 -> new EnemyPro(spawnX(Sprite.ELITE_PRO), y, horizontalSpeed(),
                    mobSpeed + 4, 60 + hpBonus);
            default -> new EnemyPlus(spawnX(Sprite.ELITE_PLUS), y, horizontalSpeed(),
                    mobSpeed + 4, 90 + hpBonus);
        };
    }

    BossEnemy createBoss() {
        BossEnemy boss = new BossEnemy(FIELD_WIDTH / 2, Sprite.BOSS.height / 2,
                difficulty.bossSpeed, bossHp);
        if (difficulty == Difficulty.HARD) {
            bossHp += 100;
        }
        return boss;
    }

    private int spawnX(Sprite sprite) {
        return sprite.width / 2 + random.nextInt(FIELD_WIDTH - sprite.width + 1);
    }

    private int horizontalSpeed() {
        return random.nextInt(7) - 3;
    }
}
