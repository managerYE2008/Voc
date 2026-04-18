package com.example.volcabularycards.data;


import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.volcabularycards.data.dao.WordDao;

@Database(entities = {Word.class}, version = 5, exportSchema = false)
public abstract class WordDatabase extends RoomDatabase {

    private static volatile WordDatabase INSTANCE;
    private static final String DATABASE_NAME = "word_database";
    private static final String PREFS_NAME = "word_db_prefs";
    private static final String KEY_IS_FIRST_TIME = "is_first_time";

    public abstract WordDao wordDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE word ADD COLUMN annotation TEXT");
            database.execSQL("ALTER TABLE word ADD COLUMN image_path TEXT");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 添加 is_learning 字段，默认为 0 (false)
            database.execSQL("ALTER TABLE word ADD COLUMN is_learning INTEGER NOT NULL DEFAULT 0");
            
            database.execSQL("CREATE INDEX IF NOT EXISTS index_word_is_learning ON word(is_learning)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE word_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "text TEXT, " +
                    "meaning TEXT, " +
                    "last_review_time INTEGER NOT NULL, " +
                    "mastery_level REAL NOT NULL DEFAULT 0, " +
                    "is_learning INTEGER NOT NULL DEFAULT 0, " +
                    "annotation TEXT, " +
                    "image_path TEXT)");
            
            database.execSQL("INSERT INTO word_new (id, text, meaning, last_review_time, mastery_level, is_learning, annotation, image_path) " +
                    "SELECT id, text, meaning, IFNULL(last_review_time, 0), CAST(mastery_level AS REAL), IFNULL(is_learning, 0), annotation, image_path FROM word");
            
            database.execSQL("DROP TABLE word");
            
            database.execSQL("ALTER TABLE word_new RENAME TO word");
            
            database.execSQL("CREATE INDEX IF NOT EXISTS index_word_last_review_time ON word(last_review_time)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_word_mastery_level ON word(mastery_level)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_word_is_learning ON word(is_learning)");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP INDEX IF EXISTS index_word_last_review_time");
            database.execSQL("DROP INDEX IF EXISTS index_word_mastery_level");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_word_text ON word(text)");
        }
    };

    /**
     * 获取单例实例（线程安全）
     * @param context 应用上下文
     * @return WordDatabase 实例
     */
    public static WordDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WordDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            WordDatabase.class,
                            DATABASE_NAME
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                     .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 销毁数据库实例（用于测试或需要重建数据库时）
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    public static boolean isFirstTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_FIRST_TIME, true);
    }

    public static void setIsFirstTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_FIRST_TIME, false).apply();
    }
}
