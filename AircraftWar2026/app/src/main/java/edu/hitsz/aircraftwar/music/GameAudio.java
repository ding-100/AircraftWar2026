package edu.hitsz.aircraftwar.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

import edu.hitsz.aircraftwar.game.GameEvent;

import java.util.Set;

/** Receives game events on the main thread and owns the Activity's audio resources. */
public final class GameAudio {
    private final AudioManager audioManager;
    private final AudioFocusRequest focusRequest;
    private final MyMediaPlayer music;
    private final MySoundPool effects;
    private boolean isForeground;
    private boolean hasAudioFocus;
    private boolean isBossActive;
    private boolean isFinished;
    private boolean isReleased;

    public GameAudio(Context context) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        music = new MyMediaPlayer(context, attributes);
        effects = new MySoundPool(context, attributes);
        audioManager = context.getSystemService(AudioManager.class);
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attributes)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this::onFocusChanged)
                .build();
    }

    public void startRound() {
        isFinished = false;
        isBossActive = false;
        music.reset();
        syncPlayback();
    }

    public void setForeground(boolean isForeground) {
        this.isForeground = isForeground;
        if (isForeground) {
            hasAudioFocus = audioManager.requestAudioFocus(focusRequest)
                    == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            hasAudioFocus = false;
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
        syncPlayback();
    }

    public void playEvents(Set<GameEvent> events) {
        if (events.contains(GameEvent.BOSS_ENTERED)) {
            isBossActive = true;
        } else if (events.contains(GameEvent.BOSS_LEFT)) {
            isBossActive = false;
        }
        if (events.contains(GameEvent.GAME_OVER)) {
            isFinished = true;
        }
        syncPlayback();
        if (canPlay()) {
            for (GameEvent event : events) {
                effects.play(event);
            }
        }
    }

    public void release() {
        setForeground(false);
        isReleased = true;
        music.release();
        effects.release();
    }

    private void onFocusChanged(int change) {
        if (isReleased) {
            return;
        }
        hasAudioFocus = change == AudioManager.AUDIOFOCUS_GAIN;
        syncPlayback();
    }

    private void syncPlayback() {
        boolean canPlay = canPlay();
        music.update(canPlay && !isFinished, isBossActive);
        effects.setPaused(!canPlay);
    }

    private boolean canPlay() {
        return isForeground && hasAudioFocus;
    }
}
