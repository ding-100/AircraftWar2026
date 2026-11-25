package edu.hitsz.aircraftwar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.FrameLayout;
import android.widget.TextView;

import edu.hitsz.aircraftwar.game.Difficulty;
import edu.hitsz.aircraftwar.game.GameEvent;
import edu.hitsz.aircraftwar.music.GameAudio;

public final class GameActivity extends Activity {
    private static final String EXTRA_DIFFICULTY = "difficulty";
    private static final String EXTRA_SOUND_ENABLED = "sound_enabled";
    private static final String STATE_SCORE = "score";
    private static final int IN_PROGRESS = -1;

    private final Handler gameHandler = new Handler(Looper.getMainLooper(), this::handleGameMessage);
    private FrameLayout gameContainer;
    private BaseGame game;
    private GameAudio audio;
    private Difficulty difficulty;
    private int finalScore = IN_PROGRESS;
    private boolean isResumed;
    private AlertDialog resultDialog;

    static Intent createIntent(Context context, Difficulty difficulty, boolean isSoundEnabled) {
        return new Intent(context, GameActivity.class)
                .putExtra(EXTRA_DIFFICULTY, difficulty.name())
                .putExtra(EXTRA_SOUND_ENABLED, isSoundEnabled);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ScreenInsets.applyTo(findViewById(R.id.game_root));
        gameContainer = findViewById(R.id.game_container);
        difficulty = Difficulty.fromName(getIntent().getStringExtra(EXTRA_DIFFICULTY));
        if (savedInstanceState != null) {
            finalScore = savedInstanceState.getInt(STATE_SCORE, IN_PROGRESS);
        }
        TextView title = findViewById(R.id.game_difficulty);
        title.setText(DifficultyLabels.nameOf(difficulty));
        findViewById(R.id.game_back).setOnClickListener(view -> finish());

        if (getIntent().getBooleanExtra(EXTRA_SOUND_ENABLED, true)) {
            audio = new GameAudio(getApplicationContext());
        }
        if (finalScore == IN_PROGRESS) {
            startRound();
        }
    }

    private void startRound() {
        finalScore = IN_PROGRESS;
        gameContainer.removeAllViews();
        game = new BaseGame(this, difficulty, gameHandler);
        gameContainer.addView(game);
        if (audio != null) {
            audio.startRound();
        }
        game.setScreenActive(isResumed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        if (audio != null) {
            audio.setForeground(true);
        }
        if (finalScore == IN_PROGRESS) {
            game.setScreenActive(true);
        } else {
            showResult();
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        if (game != null) {
            game.setScreenActive(false);
        }
        if (audio != null) {
            audio.setForeground(false);
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SCORE, finalScore);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        gameHandler.removeCallbacksAndMessages(null);
        if (audio != null) {
            audio.release();
        }
        super.onDestroy();
    }

    private boolean handleGameMessage(Message message) {
        if (message.what != BaseGame.FRAME_EVENTS || isFinishing()) {
            return false;
        }
        BaseGame.FrameEvents frame = (BaseGame.FrameEvents) message.obj;
        if (audio != null) {
            audio.playEvents(frame.events());
        }
        if (frame.events().contains(GameEvent.GAME_OVER)) {
            finalScore = frame.score();
            showResult();
        }
        return true;
    }

    private void showResult() {
        if (!isResumed || isFinishing() || resultDialog != null) {
            return;
        }
        resultDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.game_over)
                .setMessage(getString(R.string.final_score, finalScore))
                .setPositiveButton(R.string.play_again, (dialog, button) -> startRound())
                .setNegativeButton(R.string.return_to_menu, (dialog, button) -> finish())
                .setCancelable(false)
                .create();
        resultDialog.setOnDismissListener(dialog -> resultDialog = null);
        resultDialog.show();
    }
}
