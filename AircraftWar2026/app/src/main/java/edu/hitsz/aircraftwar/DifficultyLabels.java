package edu.hitsz.aircraftwar;

import edu.hitsz.aircraftwar.game.Difficulty;

final class DifficultyLabels {
    private DifficultyLabels() {
    }

    static int nameOf(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> R.string.difficulty_easy;
            case NORMAL -> R.string.difficulty_normal;
            case HARD -> R.string.difficulty_hard;
        };
    }
}
