package edu.hitsz.aircraftwar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import edu.hitsz.aircraftwar.game.Difficulty;
import edu.hitsz.aircraftwar.rank.RankAdapter;
import edu.hitsz.aircraftwar.rank.RankDao;
import edu.hitsz.aircraftwar.rank.RankEntry;
import edu.hitsz.aircraftwar.rank.RankEntryDialog;
import edu.hitsz.aircraftwar.rank.SQLiteRankDao;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RankActivity extends Activity {
    private static final String TAG = "AircraftWarRank";
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String SCORE_KEY = "score";
    private static final String ROUND_KEY = "round_id";
    private static final int NO_PENDING_SCORE = -1;

    // One process-wide queue orders reads and writes even when a ranking screen is recreated.
    private static final ExecutorService DATABASE_QUEUE = Executors.newSingleThreadExecutor(
            task -> new Thread(task, "AircraftWar-RankDB"));

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final EnumMap<Difficulty, Button> filters = new EnumMap<>(Difficulty.class);

    private RankDao dao;
    private RankAdapter adapter;
    private TextView status;
    private Button addButton;
    private Difficulty difficulty;
    private int pendingScore;
    private String pendingRoundId;
    private boolean isWriting;
    private int queryVersion;
    private RankEntryDialog entryDialog;

    static Intent createIntent(Context context, Difficulty difficulty) {
        return new Intent(context, RankActivity.class).putExtra(DIFFICULTY_KEY, difficulty.name());
    }

    static Intent createResultIntent(Context context, Difficulty difficulty, int score, String roundId) {
        return createIntent(context, difficulty).putExtra(SCORE_KEY, score).putExtra(ROUND_KEY, roundId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        ScreenInsets.applyTo(findViewById(R.id.rank_root));
        Bundle state = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        difficulty = Difficulty.fromName(state == null ? null : state.getString(DIFFICULTY_KEY));
        pendingScore = state == null ? NO_PENDING_SCORE : state.getInt(SCORE_KEY, NO_PENDING_SCORE);
        pendingRoundId = state == null ? null : state.getString(ROUND_KEY);
        dao = SQLiteRankDao.getInstance(this);

        setupViews();
        loadRanking();
        if (pendingScore >= 0 && pendingRoundId != null) {
            showEntryDialog(pendingScore, pendingRoundId);
        }
    }

    private void setupViews() {
        status = findViewById(R.id.rank_status);
        addButton = findViewById(R.id.rank_add);
        addButton.setOnClickListener(view -> showEntryDialog(NO_PENDING_SCORE, UUID.randomUUID().toString()));
        findViewById(R.id.rank_back).setOnClickListener(view -> finish());

        ListView list = findViewById(R.id.rank_list);
        adapter = new RankAdapter(this, this::confirmDelete);
        list.setEmptyView(status);
        list.setAdapter(adapter);

        filters.put(Difficulty.EASY, findViewById(R.id.rank_easy));
        filters.put(Difficulty.NORMAL, findViewById(R.id.rank_normal));
        filters.put(Difficulty.HARD, findViewById(R.id.rank_hard));
        filters.forEach((mode, button) -> {
            button.setSelected(mode == difficulty);
            button.setOnClickListener(view -> selectDifficulty(mode));
        });
    }

    private void selectDifficulty(Difficulty selected) {
        if (difficulty == selected || isWriting) {
            return;
        }
        difficulty = selected;
        filters.forEach((mode, button) -> button.setSelected(mode == selected));
        loadRanking();
    }

    private void loadRanking() {
        Difficulty requested = difficulty;
        int version = ++queryVersion;
        showStatus(R.string.rank_loading, false);
        adapter.replaceRecords(List.of());
        runOnDatabase(() -> dao.list(requested), records -> {
            if (version == queryVersion) {
                showRecords(records);
            }
        }, () -> {
            if (version == queryVersion) {
                showStatus(R.string.rank_load_failed, true);
            }
        });
    }

    private void showEntryDialog(int roundScore, String roundId) {
        if (isWriting || entryDialog != null) {
            return;
        }
        Difficulty entryDifficulty = difficulty;
        entryDialog = new RankEntryDialog(this, roundScore, (name, score) -> {
            long playedAt = System.currentTimeMillis();
            saveChange(() -> dao.save(entryDifficulty, roundId, name, score, playedAt), () -> {
                pendingScore = NO_PENDING_SCORE;
                pendingRoundId = null;
                entryDialog.dismiss();
            });
        });
        entryDialog.onDismiss(() -> {
            entryDialog = null;
            if (roundScore >= 0) {
                pendingScore = NO_PENDING_SCORE;
                pendingRoundId = null;
            }
        });
    }

    private void confirmDelete(RankEntry entry) {
        if (isWriting) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_rank)
                .setMessage(getString(R.string.delete_rank_prompt, entry.name()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, button) ->
                        saveChange(() -> dao.delete(entry.id()), () -> {}))
                .show();
    }

    private void saveChange(Runnable change, Runnable onSuccess) {
        setWriting(true);
        Difficulty requested = difficulty;
        runOnDatabase(() -> {
            change.run();
            return dao.list(requested);
        }, records -> {
            setWriting(false);
            showRecords(records);
            onSuccess.run();
        }, () -> {
            setWriting(false);
            Toast.makeText(this, R.string.rank_write_failed, Toast.LENGTH_SHORT).show();
        });
    }

    private void setWriting(boolean isWriting) {
        this.isWriting = isWriting;
        addButton.setEnabled(!isWriting);
        filters.values().forEach(button -> button.setEnabled(!isWriting));
        if (entryDialog != null) {
            entryDialog.setSaving(isWriting);
        }
    }

    private void showRecords(List<RankEntry> records) {
        showStatus(R.string.rank_empty, false);
        adapter.replaceRecords(records);
    }

    private void showStatus(int textId, boolean isRetry) {
        status.setText(textId);
        status.setOnClickListener(isRetry ? view -> loadRanking() : null);
        status.setClickable(isRetry);
    }

    private <T> void runOnDatabase(Supplier<T> task, Consumer<T> onSuccess, Runnable onError) {
        DATABASE_QUEUE.execute(() -> {
            try {
                T result = task.get();
                onActiveScreen(() -> onSuccess.accept(result));
            } catch (SQLException exception) {
                Log.e(TAG, "Ranking storage failed", exception);
                onActiveScreen(onError);
            }
        });
    }

    private void onActiveScreen(Runnable change) {
        mainHandler.post(() -> {
            if (!isDestroyed() && !isFinishing()) {
                change.run();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putString(DIFFICULTY_KEY, difficulty.name());
        state.putInt(SCORE_KEY, pendingScore);
        state.putString(ROUND_KEY, pendingRoundId);
        super.onSaveInstanceState(state);
    }
}
