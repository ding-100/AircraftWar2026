package edu.hitsz.aircraftwar.rank;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import edu.hitsz.aircraftwar.game.Difficulty;

import java.util.ArrayList;
import java.util.List;

public final class SQLiteRankDao extends SQLiteOpenHelper implements RankDao {
    private static final String TABLE = "rank_entries";
    private static final int VERSION = 1;
    private static SQLiteRankDao instance;

    public static synchronized RankDao getInstance(Context context) {
        if (instance == null) {
            instance = new SQLiteRankDao(context.getApplicationContext(), "rank.db");
        }
        return instance;
    }

    SQLiteRankDao(Context context, String databaseName) {
        super(context, databaseName, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("""
                CREATE TABLE rank_entries (
                    _id INTEGER PRIMARY KEY AUTOINCREMENT,
                    difficulty TEXT NOT NULL,
                    round_id TEXT NOT NULL UNIQUE,
                    name TEXT NOT NULL,
                    score INTEGER NOT NULL CHECK (score >= 0),
                    played_at INTEGER NOT NULL
                )
                """);
        database.execSQL("""
                CREATE INDEX rank_order ON rank_entries (difficulty, score DESC, played_at, _id)
                """);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        throw new IllegalStateException("Missing rank database migration from " + oldVersion
                + " to " + newVersion);
    }

    @Override
    public List<RankEntry> list(Difficulty difficulty) {
        List<RankEntry> entries = new ArrayList<>();
        String[] columns = {"_id", "name", "score", "played_at"};
        String[] filter = {difficulty.name()};
        try (Cursor cursor = getReadableDatabase().query(TABLE, columns, "difficulty = ?", filter,
                null, null, "score DESC, played_at ASC, _id ASC")) {
            while (cursor.moveToNext()) {
                entries.add(new RankEntry(cursor.getLong(0), cursor.getString(1), cursor.getInt(2),
                        cursor.getLong(3)));
            }
        }
        return entries;
    }

    @Override
    public void save(Difficulty difficulty, String roundId, String name, int score, long playedAt) {
        // A restored result can retry the same round without creating a second record.
        getWritableDatabase().execSQL("""
                INSERT INTO rank_entries (difficulty, round_id, name, score, played_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (round_id) DO NOTHING
                """, new Object[]{difficulty.name(), roundId, name, score, playedAt});
    }

    @Override
    public void delete(long id) {
        getWritableDatabase().delete(TABLE, "_id = ?", new String[]{Long.toString(id)});
    }
}
