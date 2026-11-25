package edu.hitsz.aircraftwar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView background = findViewById(R.id.preview_background);
        background.setImageBitmap(GameImages.decode(getResources(), R.drawable.background_easy));

        ImageView hero = findViewById(R.id.preview_hero);
        hero.setImageBitmap(GameImages.decode(getResources(), R.drawable.hero));
    }
}
