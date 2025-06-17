package my.project.moviesbox.config;

import android.content.Context;
import android.content.res.Resources;

import my.project.moviesbox.R;

/**
 * @author Li
 * @version 1.0
 * @description: 读取配置文件
 * @date 2024/5/17 13:30
 */
public class ConfigManager {
    private static final String TAG = "ConfigManager";
    private static Context context;
    private static volatile ConfigManager instance;
    private final Resources resources;

    public static void init(Context context) {
        ConfigManager.context = context.getApplicationContext();
    }

    private ConfigManager() {
        resources = context.getResources();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    public boolean isAppInfo() {
        return resources.getBoolean(R.bool.appInfo);
    }

    public boolean isTurnOnHiddenFeatures() {
        return resources.getBoolean(R.bool.turnOnHiddenFeatures);
    }

    public boolean isIgnoreTs() {
        return resources.getBoolean(R.bool.ignoreTs);
    }

    public boolean isRemoveAdTs() {
        return resources.getBoolean(R.bool.removeAdTs);
    }

    public int getMaxTsQueueNum() {
        return resources.getInteger(R.integer.maxTsQueueNum);
    }

    public int getPlayer() {
        return resources.getInteger(R.integer.player);
    }

    public int getPlayerKernel() {
        return resources.getInteger(R.integer.playerKernel);
    }

    public int getUserSpeed() {
        return resources.getInteger(R.integer.userSpeed);
    }

    public boolean isHideProgress() {
        return resources.getBoolean(R.bool.hideProgress);
    }

    public boolean isPlayNextVideo() {
        return resources.getBoolean(R.bool.playNextVideo);
    }

    public boolean isOpenDanmu() {
        return resources.getBoolean(R.bool.openDanmu);
    }

    public boolean isAnimationEnable() {
        return resources.getBoolean(R.bool.animationEnable);
    }

    public boolean isAnimationFirstOnly() {
        return resources.getBoolean(R.bool.animationFirstOnly);
    }

    public String getAnimationDefault() {
        return resources.getString(R.string.animationDefault);
    }

    public String[] getSuffering() {
        return resources.getStringArray(R.array.suffering);
    }

    public boolean isEnableSniffing() {
        return resources.getBoolean(R.bool.enableSniffing);
    }

    public int getSniffTimeout() {
        return resources.getInteger(R.integer.sniffTimeOut);
    }

    public boolean isEnableByPassCF() {
        return resources.getBoolean(R.bool.enableByPassCF);
    }

    public int getByPassCFTimeout() {
        return resources.getInteger(R.integer.byPassCFTimeout);
    }

    public int getFavoriteQueryLimit() {
        return resources.getInteger(R.integer.favoriteQueryLimit);
    }

    public int getHistoryQueryLimit() {
        return resources.getInteger(R.integer.historyQueryLimit);
    }

    public int getDownloadQueryLimit() {
        return resources.getInteger(R.integer.downloadQueryLimit);
    }

    public int getDownloadDataQueryLimit() {
        return resources.getInteger(R.integer.downloadDataQueryLimit);
    }

}
