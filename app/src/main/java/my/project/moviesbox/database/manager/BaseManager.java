package my.project.moviesbox.database.manager;

import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import my.project.moviesbox.application.App;
import my.project.moviesbox.database.AppDatabase;
import my.project.moviesbox.parser.config.ParserInterfaceFactory;
import my.project.moviesbox.parser.parserService.ParserInterface;

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
