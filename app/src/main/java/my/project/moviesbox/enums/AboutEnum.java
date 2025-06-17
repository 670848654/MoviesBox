package my.project.moviesbox.enums;

import androidx.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.R;
import my.project.moviesbox.bean.SettingAboutBean;
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
    DATA_SOURCE(R.drawable.round_bug_report_24, Utils.getString(R.string.dataSourcesTitle), ""),
    AFFIRM(R.drawable.round_front_hand_24, Utils.getString(R.string.statementTitle), Utils.getString(R.string.statementSubContent)),
    TEST_MODEL(R.drawable.round_phonelink_setup_24, Utils.getString(R.string.testModelTitle), Utils.getString(R.string.testModelSubContent)),
    CACHE_DIRECTORY(R.drawable.round_snippet_folder_24, Utils.getString(R.string.cacheDirectoryTitle), ""),
    AUTHORIZATION_DIRECTORY(R.drawable.round_folder_shared_24, Utils.getString(R.string.authorizationDirectoryTitle), ""),
    LAST_BUILD_DATE(R.drawable.round_code_24, Utils.getString(R.string.lastBuildDateTitle), Utils.getString(R.string.lastBuildDateSubContent)),
    GITHUB(R.drawable.star, Utils.getString(R.string.githubTitle), Utils.getString(R.string.githubSubContent)),
    OPEN_SOURCE(R.drawable.github, Utils.getString(R.string.openSourceTitle), "");
    @DrawableRes
    private int icon;
    private String title;
    private String subTitle;

    public static List<SettingAboutBean> getSettingAboutBeanList() {
        List<SettingAboutBean> settingAboutBeans = new ArrayList<>();
        for (AboutEnum aboutEnum : AboutEnum.values()) {
            settingAboutBeans.add(new SettingAboutBean(aboutEnum.getIcon(), aboutEnum.getTitle(), aboutEnum.getSubTitle()));
        }
        return settingAboutBeans;
    }
}
