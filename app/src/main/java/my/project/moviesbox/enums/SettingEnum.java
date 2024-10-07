package my.project.moviesbox.enums;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.R;
import my.project.moviesbox.bean.SettingAboutBean;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.config
  * @类名: SettingEnum
  * @描述: 设置选项枚举
  * @作者: Li Z
  * @日期: 2024/1/24 13:38
  * @版本: 1.0
 */
@Getter
@AllArgsConstructor
public enum SettingEnum {
    SET_DOMAIN(R.drawable.round_http_24, Utils.getString(R.string.setDomainTitle), "", null),
    FUCK_CF(R.drawable.round_airline_stops_24, Utils.getString(R.string.enableByPassCF),Utils.getString(R.string.enableByPassCFContent), null),
    SNIFF(R.drawable.round_build_circle_24, Utils.getString(R.string.enableSniffTitle), Utils.getString(R.string.enableSniffSubContent), null),
    SET_VIDEO_PLAYER(R.drawable.round_video_settings_24, Utils.getString(R.string.setPlayerTitle), "", Utils.getArray(R.array.setPlayerItems)),
    SET_PLAYER_KERNEL(R.drawable.round_video_stable_24, Utils.getString(R.string.setPlayerKernelTitle), "", Utils.getArray(R.array.setPlayerKernelItems)),
    SET_OPEN_DANMU(R.drawable.danmu, Utils.getString(R.string.setDanmuTitle), "", Utils.getArray(R.array.setDanmuItems)),
    SET_THEME(R.drawable.baseline_style_24, Utils.getString(R.string.setThemeTitle), "", new Integer[]{
            AppCompatDelegate.MODE_NIGHT_NO, // 亮色主题
            AppCompatDelegate.MODE_NIGHT_YES, // 暗色主题
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY} // 跟随系统
    ),
    M3U8_QUEUE_NUM(R.drawable.round_dynamic_feed_24, Utils.getString(R.string.setM3u8Title), "", null),
    BACKUPS(R.drawable.round_settings_backup_restore_24, Utils.getString(R.string.setBackupsTitle), Utils.getString(R.string.setBackupsSubContent), null),
    REMOVE_ALL_DOWNLOADS(R.drawable.round_remove_circle_24, Utils.getString(R.string.setRemoveDownloadsTitle), Utils.getString(R.string.setRemoveDownloadsSubContent), null),
    CHECK_VERSION(R.drawable.round_sync_24, Utils.getString(R.string.currentVersionTitle), "", null),
    ABOUT(R.drawable.round_android_24, Utils.getString(R.string.aboutTitle), "", null);
    @DrawableRes
    private int icon;
    private String title;
    private String subTitle;
    private Object[] items;

    public static List<SettingAboutBean> getSettingAboutBeanList() {
        List<SettingAboutBean> settingAboutBeans = new ArrayList<>();
        for (SettingEnum settingEnum : SettingEnum.values()) {
            settingAboutBeans.add(new SettingAboutBean(settingEnum.getIcon(), settingEnum.getTitle(), settingEnum.getSubTitle(), settingEnum.getItems(), 0));
        }
        return settingAboutBeans;
    }
}
