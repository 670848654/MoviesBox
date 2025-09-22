package my.project.moviesbox.custom;

import java.util.HashSet;

import master.flame.danmaku.controller.DanmakuFilters;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;

/**
 * @author Li
 * @version 1.0
 * @description: 弹幕组合过滤器
 * @date 2025/8/22 14:27
 */
public class ComboDanmakuFilter extends DanmakuFilters.BaseDanmakuFilter<Object> {
    /**
     * 弹幕内容最大长度
     */
    private static final int MAX_LENGTH = 50;
    /**
     * 2秒内重复视为刷屏
     */
    private static final long REPEAT_INTERVAL = 2000;
    /**
     * 0.5秒内过密丢弃
     */
    private static final long MIN_INTERVAL = 500;

    private final HashSet<String> recentContents = new HashSet<>();
    private long lastRepeatCheckTime = 0;
    private long lastDanmakuShowTime = 0;

    @Override
    public boolean filter(BaseDanmaku danmaku, int index, int totalsizeInScreen, DanmakuTimer timer, boolean fromCachingTask, DanmakuContext config) {
        if (danmaku == null || danmaku.text == null) return true;
        String content = danmaku.text.toString();
        // 1. 过滤超长弹幕
        if (content.length() > MAX_LENGTH) {
            return true;
        }
        long now = System.currentTimeMillis();
        // 2. 过滤刷屏（短时间内相同内容）
        if (recentContents.contains(content) && (now - lastRepeatCheckTime < REPEAT_INTERVAL)) {
            return true;
        }
        // 3. 过滤时间过密（与上条弹幕间隔太小）
        if (Math.abs(danmaku.getTime() - lastDanmakuShowTime) < MIN_INTERVAL) {
            return true;
        }
        // 通过过滤，保留
        recentContents.add(content);
        lastRepeatCheckTime = now;
        lastDanmakuShowTime = danmaku.getTime();
        return false;
    }

    @Override
    public void setData(Object data) {

    }

    @Override
    public void reset() {

    }
}