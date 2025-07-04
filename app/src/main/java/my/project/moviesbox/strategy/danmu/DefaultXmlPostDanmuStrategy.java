package my.project.moviesbox.strategy.danmu;

import my.project.moviesbox.contract.DanmuContract;

/**
 * @author Li
 * @version 1.0
 * @description: 默认返回XML格式弹幕策略实现(POST请求)
 * @date 2025/6/20 9:22
 */
public class DefaultXmlPostDanmuStrategy extends BaseDanmuStrategy {
    public DefaultXmlPostDanmuStrategy(boolean resultIsJson) {
        super(resultIsJson);
    }

    @Override
    protected void doRequest(String url, DanmuContract.LoadDataCallback callback) {

    }
}