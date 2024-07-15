package my.project.moviesbox.parser.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.model.DownloadVideoModel;
import my.project.moviesbox.model.VideoModel;
import my.project.moviesbox.utils.DateUtils;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;

/**
  * @包名: my.project.moviesbox.parser.config
  * @类名: SourceEnum
  * @描述: 支持站点枚举类
  * @作者: Li Z
  * @日期: 2024/1/23 18:50
  * @版本: 1.0
 */
@Getter
@AllArgsConstructor
public enum SourceEnum {
    TBYS(ParserInterfaceFactory.SOURCE_TBYS,
            "拖布影视",
            "[影视]",
            "",
            "tbys",
            "https://www.rainvi.com",
            "",
            "/index.php/vod/search/page/%s/wd/%s.html",
            "/index.php/vod/show%s.html",
            "",
            new ArrayList<>()),
    SILISILI(ParserInterfaceFactory.SOURCE_SILISILI,
            "嘶哩嘶哩",
            "[动漫]",
            "",
            "silisili",
            "https://www.silisili.link",
            "https://weibass.github.io",
            "/vodsearch/?wd=%s&page=%s",
            "/vodsearch%spage/%s",
            "/static/player/AB/api.php?act=dm&m=get&id=%s%s", // 弹幕参数 视频标题、集数下标
            Arrays.asList(VideoModel.class.getName(), DownloadVideoModel.class.getName())), // 该站点获取播放地址需要使用POST请求
    I_YINGHUA(ParserInterfaceFactory.SOURCE_IYINGHUA,
            "樱花动漫",
            "[动漫]",
            "",
            "iyinghua",
            "http://www.iyinghua.io",
            "",
            "/search/%s/?page=%s",
            "/"+ DateUtils.getNowYear() +"/",
            "",
            new ArrayList<>()),
    ANFUNS(ParserInterfaceFactory.SOURCE_ANFUNS,
            "AnFuns",
            "[动漫]",
            "无国漫",
            "anfuns",
            "https://www.anfuns.cc",
            "",
            "/search/page/%s/wd/%s.html",
            "/type/%s.html", // /show/%s.html 2024年7月12日10:48:45发现分类检索已关闭
            "/vapi/AIRA/dmku/?ac=dm&type=xml&id=%s%s", // 弹幕参数 视频ID、集数下标
            new ArrayList<>()),

    LIBVIO(ParserInterfaceFactory.SOURCE_LIBVIO,
            "LIBVIO",
            "[影视]",
            "可能存在Cloudflare",
            "libvio",
            "https://www.libvio.pw",
            "https://www.libvio.app",
            "/search/%s----------%s---.html",
            "/show/%s--------%s---.html", // 暂不实现
            "",
            new ArrayList<>()),
    ZXZJ(ParserInterfaceFactory.SOURCE_ZXZJ,
            "在线之家",
            "[影视]",
            "可能存在Cloudflare",
            "zxzj",
            "https://www.zxzja.com",
            "https://www.zxzj.site",
            "/vodsearch/%s----------%s---.html",
            "/vodshow/%s--------%s---.html", // 暂不实现
            "",
            new ArrayList<>()),
    FIVEMOVIE(
            ParserInterfaceFactory.SOURCE_FIVEMOVIE,
            "555电影",
            "[影视]",
            "",
            "fiveMovie",
            "https://5look.site",
            "https://wu5dy.com",
            "/vodsearch/%s----------%s---.html",
            "/vodshow/%s-%s-%s-%s-%s----%s---%s.html",
            "",
            new ArrayList<>())
    ;

    /**
     * 在站点配置类{@link ParserInterfaceFactory}中定义
     */
    private int source;
    /**
     * 源名称
     */
    private String sourceName;
    /**
     * 源类型
     */
    private String sourceType;
    private String sourceInfo;
    /**
     *  {@link SharedPreferencesUtils}存储名称key，能是英文
     */
    private String cacheTitle;
    /**
     * 源地址
     */
    private String domainUrl;
    /**
     * 网站发布页
     */
    private String websiteRelease;
    /**
     * 搜索参数地址
     */
    private String searchUrl;
    /**
     * 分类参数地址
     */
    private String classificationUrl;
    /**
     * 弹幕参数地址
     */
    private String danmuUrl;
    /**
     *  post请求路劲集合 {@link my.project.moviesbox.model}包下类名称
     */
    private List<String> postRequestMethod;
    /**
     * key 解析源
     * value 站点标题
     */
    private static final Map<Integer, String> TITLE_MAP = new HashMap<>();
    /**
     * key 解析源
     * value 站点默认地址
     */
    private static final Map<Integer, String> DOMAIN_URL_MAP = new HashMap<>();
    /**
     * key 解析源
     * value 站点默认存储名称
     */
    private static final Map<Integer, String> CACHE_TITLE_MAP = new HashMap<>();
    /**
     * key 解析源
     * value 站点默认弹幕API地址
     */
    private static final Map<Integer, String> DANMU_MAP = new HashMap<>();
    /**
     * key 解析源 value 站点发布页地址
     */
    private static final Map<Integer, String> WEBSITE_RELEASE_MAP = new HashMap<>();
    /**
     * APP支持的站点解析集合
     * title+type拼接 如：拖布影视 [影视]
     */
    private static final String[] SOURCES_ARR = new String[SourceEnum.values().length];

    private static final String SOURCE_NAME_HTML = "<b>%s&nbsp;&nbsp;%s</b>";
    private static final String SOURCE_DANMU_HTML = "<br><font color='#31bdec'>[弹幕]</font>";
    private static final String SOURCE_INFO_HTML = "<br>⚠️<font color='#ffb800'>%s</font><br>";

    static {
        int index = 0;
        for (SourceEnum sourceEnum : SourceEnum.values()) {
            TITLE_MAP.put(sourceEnum.getSource(), sourceEnum.getSourceName());
            DOMAIN_URL_MAP.put(sourceEnum.getSource(), sourceEnum.getDomainUrl());
            CACHE_TITLE_MAP.put(sourceEnum.getSource(), sourceEnum.getCacheTitle());
            DANMU_MAP.put(sourceEnum.getSource(), sourceEnum.getDanmuUrl());
            WEBSITE_RELEASE_MAP.put(sourceEnum.getSource(), sourceEnum.getWebsiteRelease());
            // 样式
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format(SOURCE_NAME_HTML, sourceEnum.sourceType, sourceEnum.sourceName));
            stringBuilder.append(Utils.isNullOrEmpty(sourceEnum.getDanmuUrl()) ? "" : SOURCE_DANMU_HTML);
            stringBuilder.append(Utils.isNullOrEmpty(sourceEnum.sourceInfo) ? "" : String.format(SOURCE_INFO_HTML, sourceEnum.sourceInfo));
            SOURCES_ARR[index] = stringBuilder.toString();
            index++;
        }
    }

    /**
     * 通过source获取源标题
     * @param source 当前解析源
     * @return 源枚举配置的源地址
     */
    public static String getTitleBySource(int source) {
        return TITLE_MAP.get(source);
    }

    /**
     * 通过source获取默认源地址
     * @param source 当前解析源
     * @return 源枚举配置的源地址
     */
    public static String getDomainUrlBySource(int source) {
        return DOMAIN_URL_MAP.get(source);
    }

    /**
     * 通过source获取存储名称
     * @param source 当前解析源
     * @return 源枚举配置的源存储名称
     */
    public static String getCacheTitleBySource(int source) {
        return CACHE_TITLE_MAP.get(source);
    }

    /**
     * 通过source获取是否有弹幕配置
     * @param source 当前解析源
     * @return 源枚举是否配置了弹幕API
     */
    public static boolean hasDanmuConfigBySource(int source) {
        return !Utils.isNullOrEmpty(DANMU_MAP.get(source));
    }

    /**
     * 通过source获取源发布页地址
     * @param source source – 当前解析源
     * @return 源枚举配置的源发布页地址
     */
    public static String getWebsiteReleaseBySource(int source) {
        return WEBSITE_RELEASE_MAP.get(source);
    }

    /**
     * 获取支持站点名称类型数组信息
     * @return 源枚举支持的站点解析集合
     */
    public static String[] getSourcesArr() {
        return SOURCES_ARR;
    }
}
