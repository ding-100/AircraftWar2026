package edu.hitsz.aircraftwar.rank;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import edu.hitsz.aircraftwar.R;

import java.util.function.BiConsumer;

public final class RankEntryDialog {
    private final Activity activity;
    private final AlertDialog dialog;
    private final EditText nameInput;
    private final EditText scoreInput;
    private final int roundScore;

    public RankEntryDialog(Activity activity, int roundScore, BiConsumer<String, Integer> onSave) {
        this.activity = activity;
        this.roundScore = roundScore;
        View form = activity.getLayoutInflater().inflate(R.layout.dialog_rank_entry, null);
        nameInput = form.findViewById(R.id.entry_name);
        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        scoreInput = form.findViewById(R.id.entry_score);
        scoreInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        if (roundScore >= 0) {
            TextView result = form.findViewById(R.id.entry_final_score);
            result.setText(activity.getString(R.string.final_score, roundScore));
            result.setVisibility(View.VISIBLE);
            scoreInput.setVisibility(View.GONE);
        }

        dialog = new AlertDialog.Builder(activity)
                .setTitle(roundScore >= 0 ? R.string.save_round : R.string.add_rank)
                .setView(form)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> submit(onSave));
    }

    private void submit(BiConsumer<String, Integer> onSave) {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError(activity.getString(R.string.enter_player_name));
            nameInput.requestFocus();
            return;
        }

        int score;
        try {
            score = roundScore >= 0 ? roundScore : Integer.parseInt(scoreInput.getText().toString());
            if (score < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            scoreInput.setError(activity.getString(R.string.enter_valid_score));
            scoreInput.requestFocus();
            return;
        }
        onSave.accept(name, score);
    }

    public void setSaving(boolean isSaving) {
        dialog.setCancelable(!isSaving);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!isSaving);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!isSaving);
        nameInput.setEnabled(!isSaving);
        scoreInput.setEnabled(!isSaving);
    }

    public void onDismiss(Runnable action) {
        dialog.setOnDismissListener(unused -> action.run());
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
