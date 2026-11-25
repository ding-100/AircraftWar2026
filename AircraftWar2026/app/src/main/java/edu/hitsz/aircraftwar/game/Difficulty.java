package edu.hitsz.aircraftwar.game;

public enum Difficulty {
    EASY(3, 50, 30, 6, 0, 0),
    NORMAL(5, 40, 20, 8, 5, 300),
    HARD(6, 30, 15, 10, 6, 400);

    final int enemyLimit;
    final int spawnInterval;
    final int enemyShootInterval;
    final int mobSpeed;
    final int bossSpeed;
    final int bossHp;

    Difficulty(int enemyLimit, int spawnInterval, int enemyShootInterval, int mobSpeed,
               int bossSpeed, int bossHp) {
        this.enemyLimit = enemyLimit;
        this.spawnInterval = spawnInterval;
        this.enemyShootInterval = enemyShootInterval;
        this.mobSpeed = mobSpeed;
        this.bossSpeed = bossSpeed;
        this.bossHp = bossHp;
    }

    public static Difficulty fromName(String name) {
        for (Difficulty difficulty : values()) {
            if (difficulty.name().equals(name)) {
                return difficulty;
            }
        }
        return EASY;
    }
}
