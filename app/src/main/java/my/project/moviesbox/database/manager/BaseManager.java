package my.project.moviesbox.database.manager;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import my.project.moviesbox.application.App;
import my.project.moviesbox.database.AppDatabase;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/19 13:36
 */
public abstract class BaseManager {
    public static AppDatabase database;
    public static ParserInterface parserInterface = ParserInterfaceFactory.getParserInterface();
    public static int source = parserInterface.getSource();

    /**
     * 数据库版本 1->2升级
     * 收藏、下载表新增字段 清单ID
     * 新增清单目录表
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE TFavorite ADD COLUMN directoryId TEXT");
            database.execSQL("ALTER TABLE TDownload ADD COLUMN directoryId TEXT");
            database.execSQL("CREATE TABLE TDirectory (`index` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, id TEXT, name TEXT, source INTEGER NOT NULL, type TEXT, createTime TEXT)");
        }
    };

    /**
     * 获得数据库实例
     * @return
     */
    public static AppDatabase getInstance() {
        if (database == null) {
            synchronized (BaseManager.class) {
                if (database == null) {
                    database = Room.databaseBuilder(App.getInstance(),
                                    AppDatabase.class,
                                    "appDatabase")
                            .allowMainThreadQueries()
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return database;
    }

    /**
     * 获取UUID
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取当前时间格式化字符串
     * @return
     */
    public static String getDateTimeStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
