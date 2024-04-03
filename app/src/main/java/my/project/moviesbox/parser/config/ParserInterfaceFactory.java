package my.project.moviesbox.parser.config;

import my.project.moviesbox.parser.parserImpl.AnFunsImpl;
import my.project.moviesbox.parser.parserImpl.FiveMovieImpl;
import my.project.moviesbox.parser.parserImpl.IYingHuaImpl;
import my.project.moviesbox.parser.parserImpl.LibvioImpl;
import my.project.moviesbox.parser.parserImpl.SilisiliImpl;
import my.project.moviesbox.parser.parserImpl.TbysImpl;
import my.project.moviesbox.parser.parserImpl.ZxzjImpl;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.utils.SharedPreferencesUtils;

/**
  * @包名: my.project.moviesbox.parser.config
  * @类名: ParserInterfaceFactory
  * @描述: 支持站点配置类
  * @作者: Li Z
  * @日期: 2024/2/5 20:11
  * @版本: 1.0
 */
public class ParserInterfaceFactory {
    // 拖布影视
    public static final int SOURCE_TBYS = 0;
    // 嘶哩嘶哩
    public static final int SOURCE_SILISILI = 1;
    // 樱花动漫 iyinghua
    public static final int SOURCE_IYINGHUA = 2;
    // AnFuns动漫
    public static final int SOURCE_ANFUNS = 3;
    // LIBVIO
    public static final int SOURCE_LIBVIO = 4;
    // 在线之家
    public static final int SOURCE_ZXZJ = 5;
    // 555电影
    public static final int SOURCE_FIVEMOVIE = 6;

    private static volatile ParserInterface parserInterface;

    public static ParserInterface getParserInterface() {
        if (parserInterface == null) {
            synchronized (ParserInterfaceFactory.class) {
                if (parserInterface == null) {
                    switch (SharedPreferencesUtils.getDefaultSource()) {
                        case SOURCE_TBYS:
                            parserInterface = new TbysImpl();
                            break;
                        case SOURCE_SILISILI:
                            parserInterface = new SilisiliImpl();
                            break;
                        case SOURCE_IYINGHUA:
                            parserInterface = new IYingHuaImpl();
                            break;
                        case SOURCE_ANFUNS:
                            parserInterface = new AnFunsImpl();
                            break;
                        case SOURCE_LIBVIO:
                            parserInterface = new LibvioImpl();
                            break;
                        case SOURCE_ZXZJ:
                            parserInterface = new ZxzjImpl();
                            break;
                        case SOURCE_FIVEMOVIE:
                            parserInterface = new FiveMovieImpl();
                            break;
                    }
                }
            }
        }
        return parserInterface;
    }
}
