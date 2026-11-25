package edu.hitsz.aircraftwar.music;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import edu.hitsz.aircraftwar.R;

import java.io.IOException;

/** Two looping tracks. Paused tracks keep their position until the round resets. */
final class MyMediaPlayer {
    private static final String TAG = "AircraftWarAudio";

    private final Track regular;
    private final Track boss;
    private boolean isBossActive;
    private boolean canPlay;

    MyMediaPlayer(Context context, AudioAttributes attributes) {
        regular = new Track(context, R.raw.bgm, attributes);
        boss = new Track(context, R.raw.bgm_boss, attributes);
    }

    void update(boolean canPlay, boolean isBossActive) {
        this.canPlay = canPlay;
        this.isBossActive = isBossActive;
        sync();
    }

    void reset() {
        regular.rewind();
        boss.rewind();
    }

    void release() {
        regular.release();
        boss.release();
    }

    private void sync() {
        if (isBossActive) {
            regular.setPlaying(false);
            boss.setPlaying(canPlay);
        } else {
            boss.setPlaying(false);
            regular.setPlaying(canPlay);
        }
    }

    private final class Track {
        private final MediaPlayer player = new MediaPlayer();
        private boolean isPrepared;
        private boolean isPlaying;

        Track(Context context, int resourceId, AudioAttributes attributes) {
            player.setAudioAttributes(attributes);
            player.setOnPreparedListener(prepared -> {
                isPrepared = true;
                prepared.setLooping(true);
                sync();
            });
            player.setOnErrorListener((failed, what, extra) -> {
                isPrepared = false;
                isPlaying = false;
                Log.w(TAG, "Music playback failed: " + what + "/" + extra);
                return true;
            });
            try (AssetFileDescriptor audio = context.getResources().openRawResourceFd(resourceId)) {
                player.setDataSource(audio.getFileDescriptor(), audio.getStartOffset(), audio.getLength());
                player.prepareAsync();
            } catch (IOException | IllegalArgumentException exception) {
                Log.w(TAG, "Could not prepare music resource " + resourceId, exception);
            }
        }

        void setPlaying(boolean shouldPlay) {
            if (!isPrepared || shouldPlay == isPlaying) {
                return;
            }
            if (shouldPlay) {
                player.start();
            } else {
                player.pause();
            }
            isPlaying = shouldPlay;
        }

        void rewind() {
            if (isPrepared) {
                player.seekTo(0);
            }
        }

        void release() {
            isPrepared = false;
            player.release();
        }
    }
}
