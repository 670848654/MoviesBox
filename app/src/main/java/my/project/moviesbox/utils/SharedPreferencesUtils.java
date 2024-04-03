package my.project.moviesbox.utils;

import android.content.Context;
import android.content.SharedPreferences;

import my.project.moviesbox.R;
import my.project.moviesbox.application.App;
import my.project.moviesbox.config.SettingEnum;
import my.project.moviesbox.parser.config.ParserInterfaceFactory;
import my.project.moviesbox.parser.config.SourceEnum;

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
        if ("String".equals(type)) {
            editor.putString(key, (String) object);
        } else if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) object);
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
        if ("String".equals(type)) {
            return sp.getString(key, (String) defaultObject);
        } else if ("Integer".equals(type)) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if ("Boolean".equals(type)) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if ("Float".equals(type)) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if ("Long".equals(type)) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /********************************************************** 系统相关开始 **********************************************************/
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
        return (boolean) getParam("app_info", false);
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
        return (boolean) getParam("ignoreTs", false);
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
        return (boolean) getParam("removeAdTs", false);
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
        return (int) getParam("maxTsQueueNum", 20);
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
        return (int) getParam("defaultSource", ParserInterfaceFactory.SOURCE_TBYS);
    }

    /**
     * 通过数据源获取用户设置的域名
     * @param source
     * @return
     */
    public static String getUserSetDomain(int source) {
        return (String) getParam(SourceEnum.getCacheTitleBySource(source), SourceEnum.getDomainUrlBySource(source));
    }

    /**
     * 保存用户设置的域名
     * @param source
     * @param domain
     */
    public static void setUserSetDomain(int source, String domain) {
        setParam(SourceEnum.getCacheTitleBySource(source), domain);
    }
    /********************************************************** 解析源相关结束 **********************************************************/

    /********************************************************** 播放器相关开始 **********************************************************/
    /**
     * 读取用户设置的默认打开播放器配置
     * @return
     */
    public static int getUserSetOpenVidePlayer() {
        return (Integer) getParam( "player", 0);
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
        return (Integer) getParam( "player_kernel", 1);
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
        return (Integer) getParam( "user_speed", 15);
    }

    /**
     * 保存用户设置的设置快进/快退时长配置
     * @param speed
     */
    public static void setUserSetSpeed(int speed) {
        setParam("user_speed", speed);
    }

    /**
     * 读取播放时是否隐藏进度进度条配置
     * @return
     */
    public static boolean getUserSetHideProgress() {
        return (boolean) getParam("hide_progress", false);
    }

    /**
     * 保存播放时是否隐藏进度进度条配置
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
        return (boolean) getParam("play_next_video", false);
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
        return (boolean) getParam("open_danmu", true);
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
