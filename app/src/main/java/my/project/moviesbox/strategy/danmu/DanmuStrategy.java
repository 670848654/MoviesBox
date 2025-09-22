package my.project.moviesbox.strategy.danmu;

import my.project.moviesbox.contract.DanmuContract;

/**
 * @author Li
 * @version 1.0
 * @description: 弹幕策略接口
 * @date 2025/6/20 9:13
 */
public interface DanmuStrategy {
    /**
     * 获取弹幕信息
     * @param url
     * @param callback
     */
    void getDanmu(String url, DanmuContract.LoadDataCallback callback);

    /**
     * 解析弹幕内容
     * @param source
     * @param danmuResult
     * @param callback
     */
    void parseDanmu(String source, DanmuResultEnum danmuResult, DanmuContract.LoadDataCallback callback);
}
