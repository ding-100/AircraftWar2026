package edu.hitsz.aircraftwar.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import edu.hitsz.aircraftwar.R;
import edu.hitsz.aircraftwar.game.GameEvent;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

final class MySoundPool {
    private final SoundPool soundPool;
    private final EnumMap<GameEvent, Integer> sounds = new EnumMap<>(GameEvent.class);
    private final Set<Integer> loaded = new HashSet<>();
    private boolean isReleased;

    MySoundPool(Context context, AudioAttributes attributes) {
        soundPool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attributes).build();
        soundPool.setOnLoadCompleteListener((pool, id, status) -> {
            if (!isReleased && status == 0) {
                loaded.add(id);
            }
        });
        load(context, GameEvent.BULLET_HIT, R.raw.bullet_hit);
        load(context, GameEvent.SUPPLY_COLLECTED, R.raw.get_supply);
        load(context, GameEvent.BOMB_EXPLODED, R.raw.bomb_explosion);
        load(context, GameEvent.GAME_OVER, R.raw.game_over);
    }

    private void load(Context context, GameEvent event, int resourceId) {
        sounds.put(event, soundPool.load(context, resourceId, 1));
    }

    void play(GameEvent event) {
        Integer id = sounds.get(event);
        if (loaded.contains(id)) {
            int priority = event == GameEvent.GAME_OVER ? 2 : 1;
            soundPool.play(id, 0.8f, 0.8f, priority, 0, 1f);
        }
    }

    void setPaused(boolean isPaused) {
        if (isPaused) {
            soundPool.autoPause();
        } else {
            soundPool.autoResume();
        }
    }

    void release() {
        isReleased = true;
        soundPool.release();
    }
}
