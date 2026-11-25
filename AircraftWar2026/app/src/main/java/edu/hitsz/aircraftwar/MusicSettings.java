package edu.hitsz.aircraftwar;

import android.content.Context;
import android.content.SharedPreferences;

final class MusicSettings {
    private static final String AUDIO_ENABLED = "audio_enabled";

    private MusicSettings() {
    }

    static boolean isEnabled(Context context) {
        return preferences(context).getBoolean(AUDIO_ENABLED, true);
    }

    static void setEnabled(Context context, boolean isEnabled) {
        preferences(context).edit().putBoolean(AUDIO_ENABLED, isEnabled).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }
}
