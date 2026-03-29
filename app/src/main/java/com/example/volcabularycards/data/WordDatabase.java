package com.example.volcabularycards.data;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.volcabularycards.data.dao.WordDao;

@Database(entities = {Word.class}, version = 3, exportSchema = false)
public abstract class WordDatabase extends RoomDatabase {

    private static volatile WordDatabase INSTANCE;
    private static final String DATABASE_NAME = "word_database";

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
            
            // 为 is_learning 字段添加索引（如果需要快速查询）
            database.execSQL("CREATE INDEX IF NOT EXISTS index_word_is_learning ON word(is_learning)");
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
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
}
