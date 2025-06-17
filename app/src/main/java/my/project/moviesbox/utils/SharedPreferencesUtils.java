package my.project.moviesbox.utils;

import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum;
import static my.project.moviesbox.parser.config.SourceEnum.getCacheTitleBySource;
import static my.project.moviesbox.parser.config.SourceEnum.getDomainUrlBySource;

import android.content.Context;
import android.content.SharedPreferences;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.ConfigManager;
import my.project.moviesbox.enums.SettingEnum;

/**
  * @包名: my.project.moviesbox.utils
  * @类名: SharedPreferencesUtils
  * @描述: 数据存储工具类
  * @作者: Li Z
  * @日期: 2024/1/23 15:28
  * @版本: 1.0
 */
public class SharedPreferencesUtils {
    private static final String FILE_NAME = "appData";

    /**
     * 保存数据
     * @param key
     * @param object
     */
    public static void setParam(String key, Object object) {
        String type = object.getClass().getSimpleName();
        SharedPreferences sp = App.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        switch (type) {
            case "String":
                editor.putString(key, (String) object);
                break;
            case "Integer":
                editor.putInt(key, (Integer) object);
                break;
            case "Boolean":
                editor.putBoolean(key, (Boolean) object);
                break;
            case "Float":
                editor.putFloat(key, (Float) object);
                break;
            case "Long":
                editor.putLong(key, (Long) object);
                break;
        }
        editor.commit();
    }

    /**
     * 获取数据
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object getParam(String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = App.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        switch (type) {
            case "String":
                return sp.getString(key, (String) defaultObject);
            case "Integer":
                return sp.getInt(key, (Integer) defaultObject);
            case "Boolean":
                return sp.getBoolean(key, (Boolean) defaultObject);
            case "Float":
                return sp.getFloat(key, (Float) defaultObject);
            case "Long":
                return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /********************************************************** 系统相关开始 **********************************************************/
    /**
     * 保存授权的保存目录rui
     * @param uri
     */
    public static void setDataSaveUri(String uri) {
        setParam("dataSaveUri", uri);
    }

    /**
     * 获取授权的保存目录rui
     * @return
     */
    public static String getDataSaveUri() {
        return (String) getParam("dataSaveUri", "");
    }

    /**
     * 设置数据保存位置名称后缀
     * @param suffix
     */
    public static void setDataNameSuffix(String suffix) {
        setParam("data_name_suffix", suffix);
    }

    /**
     * 获取数据保存位置名称后缀
     * @return
     */
    public static String getDataName() {
        return (String) getParam("data_name_suffix", "");
    }

    /**
     * 启动时检查是否存在保存位置后缀
     */
    public static void shouldSetDataName() {
        if (getDataName().isEmpty()) // 使用时间作为数据目录后缀
            SharedPreferencesUtils.setDataNameSuffix(String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 是否显示首次弹窗
     * @return
     */
    public static boolean appMainInfo() {
        return (boolean) getParam("app_info", ConfigManager.getInstance().isAppInfo());
    }

    /**
     * 不再显示首次弹窗
     */
    public static void setAppMainInfo() {
        setParam("app_info", true);
    }
    /********************************************************** 系统相关结束 **********************************************************/

    /******************************************************** M3U8配置相关开始 ********************************************************/
    /**
     * 设置是否开启忽略ts
     * @param ignoreTs
     */
    public static void setIgnoreTs(boolean ignoreTs) {
        setParam("ignoreTs", ignoreTs);
    }

    /**
     * 获取是否开启忽略ts
     * @return
     */
    public static boolean getIgnoreTs() {
        return (boolean) getParam("ignoreTs", ConfigManager.getInstance().isIgnoreTs());
    }

    /**
     * 设置是否开启移除广告切片
     * @param removeAdTs
     */
    public static void setRemoveAdTs(boolean removeAdTs) {
        setParam("removeAdTs", removeAdTs);
    }

    /**
     * 获取是否开启移除广告切片
     * @return
     */
    public static boolean getRemoveAdTs() {
        return (boolean) getParam("removeAdTs", ConfigManager.getInstance().isRemoveAdTs());
    }

    /**
     * 设置移除广告切片正则
     * @param removeAdTsReg
     */
    @Deprecated
    public static void setRemoveAdTsRegs(String removeAdTsReg) {
        setParam("removeAdTsReg", removeAdTsReg);
    }

    /**
     * 获取移除广告切片正则
     * @return
     */
    @Deprecated
    public static String getRemoveAdTsRegs() {
        return (String) getParam("removeAdTsReg", Utils.getString(R.string.setM3u8RegRemoveAdDefaultConfig));
    }

    /**
     * 设置ts同时下载数量
     * @return
     */
    public static void setMaxTsQueueNum(int num) {
        setParam("maxTsQueueNum", num);
    }

    /**
     * 获取ts同时下载数量
     * @return
     */
    public static int getMaxTsQueueNum() {
        return (int) getParam("maxTsQueueNum", ConfigManager.getInstance().getMaxTsQueueNum());
    }
    /******************************************************** M3U8配置相关结束 ********************************************************/

    /********************************************************** 解析源相关开始 **********************************************************/
    /**
     * 设置当前APP源
     * @return
     */
    public static void setDefaultSource(int source) {
        setParam("defaultSource", source);
    }

    /**
     * 获取当前APP源
     * @return
     */
    public static int getDefaultSource() {
        return (int) getParam("defaultSource", SourceIndexEnum.TBYS.index);
    }

    /**
     * 通过数据源获取用户设置的域名
     * @param source
     * @return
     */
    public static String getUserSetDomain(int source) {
        return (String) getParam(getCacheTitleBySource(source), getDomainUrlBySource(source));
    }

    /**
     * 保存用户设置的域名
     * @param source
     * @param domain
     */
    public static void setUserSetDomain(int source, String domain) {
        setParam(getCacheTitleBySource(source), domain);
    }

/*
    */
/**
     * 通过数据源获取用户设置的域名
     * @return
     *//*

    public static String getSubDomain() {
        return (String) getParam(getCacheTitleBySource(getDefaultSource()) + "_subdomain", "");
    }

    */
/**
     * 保存用户设置的域名
     * @param subDomain
     *//*

    public static void setSubDomain(String subDomain) {
        setParam(getCacheTitleBySource(getDefaultSource()) + "_subdomain", subDomain);
    }
*/

    /**
     * 设置是否开启视频嗅探
     * @param enable
     */
    public static void setEnableSniff(boolean enable) {
        setParam("enableSniff", enable);
    }

    /**
     * 获取是否开启视频嗅探
     * @return
     */
    public static boolean getEnableSniff() {
        return (boolean) getParam("enableSniff", ConfigManager.getInstance().isEnableSniffing());
    }

    /**
     * 设置视频嗅探超时时间
     * @param time
     */
    public static void setSniffTimeout(int time) {
        setParam("sniffTimeout", time);
    }

    /**
     * 获取视频嗅探超时时间
     * @return
     */
    public static int getSniffTimeout() {
        return (int) getParam("sniffTimeout", ConfigManager.getInstance().getSniffTimeout());
    }

    /**
     * 设置开启绕过CF
     * @param enable
     */
    public static void setByPassCF(boolean enable) {
        setParam("byPassCF", enable);
    }

    /**
     * 获取是否开启绕过CF
     * @return
     */
    public static boolean getByPassCF() {
        return (boolean) getParam("byPassCF", false);
    }

    /**
     * 设置绕过CF超时时间
     * @param time
     */
    public static void setByPassCFTimeout(int time) {
        setParam("byPassCFTimeout", time);
    }

    /**
     * 获取绕过CF超时时间
     * @return
     */
    public static int getByPassCFTimeout() {
        return (int) getParam("byPassCFTimeout", ConfigManager.getInstance().getByPassCFTimeout());
    }
    /********************************************************** 解析源相关结束 **********************************************************/

    /********************************************************** 播放器相关开始 **********************************************************/
    /**
     * 读取用户设置的默认打开播放器配置
     * @return
     */
    public static int getUserSetOpenVidePlayer() {
        return (Integer) getParam( "player", ConfigManager.getInstance().getPlayer());
    }

    /**
     * 保存用户设置的设默播放器内核
     * @param index {@link SettingEnum#SET_VIDEO_PLAYER} items数组下标
     */
    public static void setUserSetOpenVidePlayer(int index) {
        setParam("player", index);
    }

    /**
     * 读取用户设置的默播放器内核
     * @return
     */
    public static int getUserSetPlayerKernel() {
        return (Integer) getParam( "player_kernel", ConfigManager.getInstance().getPlayerKernel());
    }

    /**
     * 保存用户设置的设默认打开播放器配置
     * @param index {@link SettingEnum#SET_VIDEO_PLAYER} items数组下标
     */
    public static void setUserSetPlayerKernel(int index) {
        setParam("player_kernel", index);
    }

    /**
     * 读取用户设置的设置快进/快退时长配置
     * @return
     */
    public static int getUserSetSpeed() {
        return (Integer) getParam( "user_speed", ConfigManager.getInstance().getUserSpeed());
    }

    /**
     * 保存用户设置的设置快进/快退时长配置
     * @param speed
     */
    public static void setUserSetSpeed(int speed) {
        setParam("user_speed", speed);
    }

    /**
     * 读取播放时是否隐藏底部进度条配置
     * @return
     */
    public static boolean getUserSetHideProgress() {
        return (boolean) getParam("hide_progress", ConfigManager.getInstance().isHideProgress());
    }

    /**
     * 保存播放时是否隐藏底部进度条配置
     * @param set
     */
    public static void setUserSetHideProgress(boolean set) {
        setParam("hide_progress", set);
    }

    /**
     * 读取是否自动播放下一集配置
     * @return
     */
    public static boolean getUserAutoPlayNextVideo() {
        return (boolean) getParam("play_next_video", ConfigManager.getInstance().isPlayNextVideo());
    }

    /**
     * 保存是否自动播放下一集配置
     * @param set
     */
    public static void setUserAutoPlayNextVideo(boolean set) {
        setParam("play_next_video", set);
    }

    /**
     * 读取播放器是否开启弹幕配置
     * @return
     */
    public static boolean getUserSetOpenDanmu() {
        return (boolean) getParam("open_danmu", ConfigManager.getInstance().isOpenDanmu());
    }

    /**
     * 保存播放器是否开启弹幕配置
     * @param set
     */
    public static void setUserSetOpenDanmu(boolean set) {
        setParam("open_danmu", set);
    }
    /********************************************************** 播放器相关结束 **********************************************************/
}
