package my.project.moviesbox.strategy.danmu;

import my.project.moviesbox.parser.config.SourceEnum;

/**
 * @author Li
 * @version 1.0
 * @description: 弹幕工厂类
 * @date 2025/6/20 9:18
 */
public class DanmuStrategyFactory {

    public static DanmuStrategy getStrategy(SourceEnum.SourceIndexEnum sourceEnum) {
        switch (sourceEnum) {
            case SILISILI:
            case ANFUNS:
            case NYYY:
                return new DefaultXmlGetDanmuStrategy();
            case GIRI_GIRI_LOVE:
                return new GiriGiriLoveDanmuStrategy();
            case YJYS:
                return new YjysDanmuStrategy();
            default:
                throw new IllegalArgumentException("不支持的源sourceEnum: " + sourceEnum);
        }
    }
}
