package my.project.moviesbox.parser.parserService;

import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.fromIndex;

import my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum;
import my.project.moviesbox.parser.parserImpl.AnFunsImpl;
import my.project.moviesbox.parser.parserImpl.FiveMovieImpl;
import my.project.moviesbox.parser.parserImpl.IYingHuaImpl;
import my.project.moviesbox.parser.parserImpl.LibvioImpl;
import my.project.moviesbox.parser.parserImpl.SilisiliImpl;
import my.project.moviesbox.parser.parserImpl.TbysImpl;
import my.project.moviesbox.parser.parserImpl.XbyyImpl;
import my.project.moviesbox.parser.parserImpl.YjysImpl;
import my.project.moviesbox.parser.parserImpl.ZxzjImpl;
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
    private static volatile ParserInterface parserInterface;

    public static ParserInterface getParserInterface() {
        if (parserInterface == null) {
            synchronized (ParserInterfaceFactory.class) {
                if (parserInterface == null) {
                    int source = SharedPreferencesUtils.getDefaultSource();
                    SourceIndexEnum sourceEnum = fromIndex(source);
                    switch (sourceEnum) {
                        case TBYS:
                            parserInterface = new TbysImpl();
                            break;
                        case SILISILI:
                            parserInterface = new SilisiliImpl();
                            break;
                        case I_YINGHUA:
                            parserInterface = new IYingHuaImpl();
                            break;
                        case ANFUNS:
                            parserInterface = new AnFunsImpl();
                            break;
                        case LIBVIO:
                            parserInterface = new LibvioImpl();
                            break;
                        case ZXZJ:
                            parserInterface = new ZxzjImpl();
                            break;
                        case FIVE_MOVIE:
                            parserInterface = new FiveMovieImpl();
                            break;
                        case YJYS:
                            parserInterface = new YjysImpl();
                            break;
                        case XBYY:
                            parserInterface = new XbyyImpl();
                            break;
                    }
                }
            }
        }
        return parserInterface;
    }
}
