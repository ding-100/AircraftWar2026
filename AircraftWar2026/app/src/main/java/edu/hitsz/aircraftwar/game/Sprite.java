package edu.hitsz.aircraftwar.game;

// Sizes in playfield coordinates, matching the original drawable pixels.
public enum Sprite {
    HERO(100, 83),
    MOB(75, 50),
    ELITE(105, 68),
    ELITE_PRO(100, 83),
    ELITE_PLUS(105, 68),
    BOSS(300, 200),
    HERO_BULLET(10, 25),
    ENEMY_BULLET(10, 18),
    HEALTH(33, 30),
    FIRE(35, 33),
    SUPER_FIRE(34, 34),
    FREEZE(35, 35),
    BOMB(34, 34),
    SHIELD(132, 132);

    public final int width;
    public final int height;

    Sprite(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
