package edu.hitsz.aircraftwar.rank;

import edu.hitsz.aircraftwar.game.Difficulty;

import java.util.List;

public interface RankDao {
    List<RankEntry> list(Difficulty difficulty);

    void save(Difficulty difficulty, String roundId, String name, int score, long playedAt);

    void delete(long id);
}
