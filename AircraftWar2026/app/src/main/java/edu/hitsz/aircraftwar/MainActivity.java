package edu.hitsz.aircraftwar;

import android.app.Activity;
import android.os.Bundle;

public final class MainActivity extends Activity {
    private BaseGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = new BaseGame(this);
        setContentView(game);
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
