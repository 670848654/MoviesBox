package my.project.moviesbox.strategy.danmu;

import my.project.moviesbox.parser.config.SourceEnum;

/**
 * @author Li
 * @version 1.0
 * @description: 弹幕工厂类
 * @date 2025/6/20 9:18
 */
public class DanmuStrategyFactory {

    public static DanmuStrategy getStrategy(SourceEnum.SourceIndexEnum sourceEnum, boolean resultIsJson) {
        switch (sourceEnum) {
            case SILISILI:
            case ANFUNS:
            case NYYY:
                return new DefaultXmlGetDanmuStrategy(resultIsJson);
            case GIRI_GIRI_LOVE:
                return new GiriGiriLoveDanmuStrategy(resultIsJson);
            case YJYS:
                return new YjysDanmuStrategy(resultIsJson);
            default:
                throw new IllegalArgumentException("不支持的 sourceEnum: " + sourceEnum);
        }
    }
}
