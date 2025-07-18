package my.project.moviesbox.strategy.danmu;

import java.util.List;

import my.project.moviesbox.R;
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.parser.bean.DanmuDataBean;
import my.project.moviesbox.utils.Utils;

/**
 * @author Li
 * @version 1.0
 * @description: 弹幕策略基类
 * @date 2025/6/20 9:29
 */
public abstract class BaseDanmuStrategy implements DanmuStrategy {
    /**
     * 接口返回弹幕格式 true:json格式 false:xml格式
     */
    protected final boolean resultIsJson;

    public BaseDanmuStrategy(boolean resultIsJson) {
        this.resultIsJson = resultIsJson;
    }

    @Override
    public void getDanmu(String api, DanmuContract.LoadDataCallback callback) {
        doRequest(api, callback);
    }

    /**
     * 自行实现弹幕接口请求/响应
     * @param api       接口地址
     * @param callback  回调
     */
    protected abstract void doRequest(String api, DanmuContract.LoadDataCallback callback);

    /**
     * 统一的解析结果方法，子类只需准备数据源
     * @param source    接口返回的内容
     * @param callback  回调
     */
    @Override
    public void parseDanmu(String source, DanmuContract.LoadDataCallback callback) {
        if (Utils.isNullOrEmpty(source)) {
            callback.errorDanmu(Utils.getString(R.string.errorDanmuMsg));
            return;
        }

        if (resultIsJson) {
            try {
                List<DanmuDataBean> list = parseDanmuJson(source);
                callback.successDanmuJson(list);
            } catch (Exception e) {
                callback.errorDanmu("JSON解析失败: " + e.getMessage());
            }
        } else {
            callback.successDanmuXml(source);
        }
    }

    /**
     * 默认不实现，如需 JSON 支持请子类重写
     * @param jsonSource    json数据
     * @return
     */
    protected List<DanmuDataBean> parseDanmuJson(String jsonSource) {
        throw new UnsupportedOperationException("该弹幕源不支持 JSON 格式解析");
    }
}