package com.example.volcabularycards.data;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.volcabularycards.data.dao.WordDao;

@Database(entities = {Word.class}, version = 2, exportSchema = false)
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
                    ).addMigrations(MIGRATION_1_2)
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
