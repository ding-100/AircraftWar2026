package edu.hitsz.aircraftwar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.hitsz.aircraftwar.game.Difficulty;
import edu.hitsz.aircraftwar.game.Sprite;

import java.util.EnumMap;

final class GameImages {
    final Bitmap background;
    private final EnumMap<Sprite, Bitmap> sprites = new EnumMap<>(Sprite.class);

    GameImages(Resources resources, Difficulty difficulty) {
        int backgroundId = switch (difficulty) {
            case EASY -> R.drawable.background_easy;
            case NORMAL -> R.drawable.background_normal;
            case HARD -> R.drawable.background_hard;
        };
        background = BitmapFactory.decodeResource(resources, backgroundId);
        for (Sprite sprite : Sprite.values()) {
            sprites.put(sprite, BitmapFactory.decodeResource(resources, drawableId(sprite)));
        }
    }

    Bitmap get(Sprite sprite) {
        return sprites.get(sprite);
    }

    private static int drawableId(Sprite sprite) {
        return switch (sprite) {
            case HERO -> R.drawable.hero;
            case MOB -> R.drawable.mob;
            case ELITE -> R.drawable.elite;
            case ELITE_PRO -> R.drawable.elite_pro;
            case ELITE_PLUS -> R.drawable.elite_plus;
            case BOSS -> R.drawable.boss;
            case HERO_BULLET -> R.drawable.bullet_hero;
            case ENEMY_BULLET -> R.drawable.bullet_enemy;
            case HEALTH -> R.drawable.prop_blood;
            case FIRE -> R.drawable.prop_bullet;
            case SUPER_FIRE -> R.drawable.prop_bullet_plus;
            case FREEZE -> R.drawable.prop_freeze;
            case BOMB -> R.drawable.prop_bomb;
            case SHIELD -> R.drawable.shield;
        };
    }
}
