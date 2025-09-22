package my.project.moviesbox.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Li
 * @version 1.0
 * @description: 搜索记录管理器
 * @date 2025/9/9 10:12
 */
public class SearchHistoryManager {
    private static final String PREF_NAME = "search_history";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY = 20; // 最多保存20条

    public static void saveHistory(Context context, String keyword) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyStr = sp.getString(KEY_HISTORY, "");
        List<String> historyList = new ArrayList<>(Arrays.asList(historyStr.split(",")));

        // 去重
        historyList.remove(keyword);
        historyList.add(0, keyword);

        // 限制数量
        if (historyList.size() > MAX_HISTORY) {
            historyList = historyList.subList(0, MAX_HISTORY);
        }

        // 保存
        sp.edit().putString(KEY_HISTORY, TextUtils.join(",", historyList)).apply();
    }

    public static List<String> getHistory(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyStr = sp.getString(KEY_HISTORY, "");
        if (TextUtils.isEmpty(historyStr)) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(historyStr.split(",")));
    }
}
