package edu.hitsz.aircraftwar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Switch;

import edu.hitsz.aircraftwar.game.Difficulty;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScreenInsets.applyTo(findViewById(R.id.menu_root));

        Switch music = findViewById(R.id.music_switch);
        music.setChecked(MusicSettings.isEnabled(this));
        music.setOnCheckedChangeListener((button, isChecked) ->
                MusicSettings.setEnabled(this, isChecked));

        findViewById(R.id.play_easy).setOnClickListener(view -> launch(Difficulty.EASY));
        findViewById(R.id.play_normal).setOnClickListener(view -> launch(Difficulty.NORMAL));
        findViewById(R.id.play_hard).setOnClickListener(view -> launch(Difficulty.HARD));
    }

    private void launch(Difficulty difficulty) {
        startActivity(GameActivity.createIntent(this, difficulty, MusicSettings.isEnabled(this)));
    }
}
