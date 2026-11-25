package edu.hitsz.aircraftwar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import edu.hitsz.aircraftwar.game.Difficulty;

public final class MainActivity extends Activity {
    private BaseGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = new BaseGame(this, Difficulty.EASY);
        FrameLayout root = new FrameLayout(this);
        ScreenInsets.applyTo(root);
        root.addView(game);
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.setScreenActive(true);
    }

    @Override
    protected void onPause() {
        game.setScreenActive(false);
        super.onPause();
    }
}
