package my.project.moviesbox.config;

import androidx.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.R;
import my.project.moviesbox.bean.SettingAboutBean;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/2/27 9:38
 */
@Getter
@AllArgsConstructor
public enum AboutEnum {
    DATA_SOURCE(R.drawable.round_bug_report_24, Utils.getString(R.string.dataSourcesTitle), "", true),
    AFFIRM(R.drawable.round_front_hand_24, Utils.getString(R.string.statementTitle), Utils.getString(R.string.statementSubContent), true),
    TEST_MODEL(R.drawable.round_phonelink_setup_24, Utils.getString(R.string.testModelTitle), Utils.getString(R.string.testModelSubContent), true),
    CACHE_DIRECTORY(R.drawable.round_snippet_folder_24, Utils.getString(R.string.cacheDirectoryTitle), "", true),
    LAST_BUILD_DATE(R.drawable.round_code_24, Utils.getString(R.string.lastBuildDateTitle), Utils.getString(R.string.lastBuildDateSubContent), true),
    VIP_VIDEO_PARSER(R.drawable.vip_url, Utils.getString(R.string.vipVideoParserTitle), Utils.getString(R.string.vipVideoParserSubTitle), SharedPreferencesUtils.getTurnOnHiddenFeatures()),
    GITHUB(R.drawable.star, Utils.getString(R.string.githubTitle), Utils.getString(R.string.githubSubContent), true),
    OPEN_SOURCE(R.drawable.github, Utils.getString(R.string.openSourceTitle), "", true);
    @DrawableRes
    private int icon;
    private String title;
    private String subTitle;
    private boolean visibility;

    public static List<SettingAboutBean> getSettingAboutBeanList() {
        List<SettingAboutBean> settingAboutBeans = new ArrayList<>();
        for (AboutEnum aboutEnum : AboutEnum.values()) {
            if (aboutEnum.isVisibility())
                settingAboutBeans.add(new SettingAboutBean(aboutEnum.getIcon(), aboutEnum.getTitle(), aboutEnum.getSubTitle()));
        }
        return settingAboutBeans;
    }

    public static List<SettingAboutBean> getTurnOnHiddenFeaturesList() {
        List<SettingAboutBean> settingAboutBeans = new ArrayList<>();
        for (AboutEnum aboutEnum : AboutEnum.values()) {
            if (!aboutEnum.isVisibility())
                settingAboutBeans.add(new SettingAboutBean(aboutEnum.getIcon(), aboutEnum.getTitle(), aboutEnum.getSubTitle()));
        }
        return settingAboutBeans;
    }
}
