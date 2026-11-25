package edu.hitsz.aircraftwar.rank;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import edu.hitsz.aircraftwar.game.Difficulty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class SQLiteRankDaoTest {
    private Context context;
    private String databaseName;
    private SQLiteRankDao dao;

    @Before
    public void openTestDatabase() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        databaseName = "rank-test-" + UUID.randomUUID() + ".db";
        dao = new SQLiteRankDao(context, databaseName);
    }

    @After
    public void removeTestDatabase() {
        dao.close();
        context.deleteDatabase(databaseName);
    }

    @Test
    public void ordersByScoreThenTimeAndFiltersByDifficulty() {
        dao.save(Difficulty.EASY, "1", "阿青", 80, 200);
        dao.save(Difficulty.EASY, "2", "Player's best", 80, 100);
        dao.save(Difficulty.EASY, "3", "小路", 30, 150);
        dao.save(Difficulty.EASY, "4", "Zero", 0, 10);
        dao.save(Difficulty.HARD, "5", "星云", 900, 300);

        List<RankEntry> easy = dao.list(Difficulty.EASY);
        assertEquals(4, easy.size());
        assertEquals("Player's best", easy.get(0).name());
        assertEquals("阿青", easy.get(1).name());
        assertEquals(30, easy.get(2).score());
        assertEquals(0, easy.get(3).score());
        assertEquals("星云", dao.list(Difficulty.HARD).get(0).name());
        assertTrue(dao.list(Difficulty.NORMAL).isEmpty());
    }

    @Test
    public void retryingTheSameRoundKeepsOneRecordAfterReopen() {
        dao.save(Difficulty.NORMAL, "round-1", "Pilot", 70, 100);
        dao.close();
        dao = new SQLiteRankDao(context, databaseName);
        dao.save(Difficulty.NORMAL, "round-1", "Different name", 80, 200);

        List<RankEntry> rows = dao.list(Difficulty.NORMAL);
        assertEquals(1, rows.size());
        assertEquals("Pilot", rows.get(0).name());
        assertEquals(70, rows.get(0).score());
    }

    @Test
    public void deletingByIdKeepsOtherMatchingRowsAndPersistsDeletion() {
        dao.save(Difficulty.EASY, "round-1", "Pilot", 70, 100);
        dao.save(Difficulty.EASY, "round-2", "Pilot", 70, 100);
        List<RankEntry> before = dao.list(Difficulty.EASY);

        dao.delete(before.get(0).id());
        dao.close();
        dao = new SQLiteRankDao(context, databaseName);

        List<RankEntry> rows = dao.list(Difficulty.EASY);
        assertEquals(1, rows.size());
        assertEquals(before.get(1).id(), rows.get(0).id());
        dao.delete(rows.get(0).id());
        assertTrue(dao.list(Difficulty.EASY).isEmpty());
    }
}
