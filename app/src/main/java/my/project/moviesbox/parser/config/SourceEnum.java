package my.project.moviesbox.parser.config;

import static my.project.moviesbox.parser.config.SourceEnum.SourceStateEnum.ABNORMAL;
import static my.project.moviesbox.parser.config.SourceEnum.SourceStateEnum.DEPRECATED;
import static my.project.moviesbox.parser.config.SourceEnum.SourceStateEnum.NORMAL;
import static my.project.moviesbox.parser.config.SourceEnum.SourceTypeEnum.ANIME;
import static my.project.moviesbox.parser.config.SourceEnum.SourceTypeEnum.MOVIES;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.R;
import my.project.moviesbox.model.DownloadVideoModel;
import my.project.moviesbox.model.VideoModel;
import my.project.moviesbox.parser.bean.SourceDataBean;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
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
    TBYS(SourceIndexEnum.TBYS,
            "拖布影视",
            MOVIES.title,
            MOVIES.bg,
            "存在广告，资源一般",
            "tbys",
            "https://www.rainvi.com",
            "",
            "/index.php/vod/search/page/%s/wd/%s.html",
            true,
            "/index.php/vod/show%s.html",
            "",
            "",
            new ArrayList<>()),
    SILISILI(SourceIndexEnum.SILISILI,
            "嘶哩嘶哩",
            ANIME.title,
            ANIME.bg,
            "资源较好",
            "silisili",
            "https://www.silisili.link",
            "",
            "/vodsearch/?wd=%s&page=%s",
            true,
            "/vodsearch%spage/%s",
            "/static/player/AB/api.php?act=dm&m=get&id=%s%s", // 弹幕参数 视频标题、集数下标
            "/rss.xml",
            Arrays.asList(VideoModel.class.getName(), DownloadVideoModel.class.getName())), // 该站点获取播放地址需要使用POST请求
    I_YINGHUA(SourceIndexEnum.I_YINGHUA,
            "樱花动漫",
            ANIME.title,
            ANIME.bg,
            "资源一般，存在广告",
            "iyinghua",
            "http://www.iyinghua.com",
            "",
            "/search/%s/?page=%s",
            true,
            "/"+ DateUtils.getNowYear() +"/",
            "",
            "",
            new ArrayList<>()),
    //  网站关闭废弃
    ANFUNS(SourceIndexEnum.ANFUNS,
            "AnFuns",
            ANIME.title,
            ANIME.bg,
            "质量高，无国产动漫",
            "anfuns",
            "https://www.anfuns.vip",
            "",
            "/search/page/%s/wd/%s.html",
            true,
            "/type/%s.html", // /show/%s.html 2024年7月12日10:48:45发现分类检索已关闭
            "/vapi/AIRA/dmku/?ac=dm&type=xml&id=%s%s", // 弹幕参数 视频ID、集数下标
            "",
            new ArrayList<>()),
    LIBVIO(SourceIndexEnum.LIBVIO,
            "LIBVIO",
            MOVIES.title,
            MOVIES.bg,
            "质量高但并不是所有都能在线观看，可能存在Cloudflare、域名可能经常变更（可通过网站发布页查看最新域名）",
            "libvio",
            "https://www.libvio.pw",
            "https://www.libvio.app",
            "/search/%s----------%s---.html",
            true,
            "/show/%s--------%s---.html", // 暂不实现
            "",
            "",
            new ArrayList<>()),
    ZXZJ(SourceIndexEnum.ZXZJ,
            "在线之家",
            MOVIES.title,
            MOVIES.bg,
            "质量高但只有热门电影/电视剧（无国产），无广告，可能存在Cloudflare、域名可能经常变更（可通过网站发布页查看最新域名）",
            "zxzj",
            "https://www.zxzja.com",
            "https://www.zxzj.site",
            "/vodsearch/%s----------%s---.html",
            true,
            "/vodshow/%s--------%s---.html", // 暂不实现
            "",
            "",
            new ArrayList<>()),
//    存在Cloudflare无法绕过 废弃
    FIVEMOVIE(SourceIndexEnum.FIVE_MOVIE,
            "555电影",
            MOVIES.title,
            MOVIES.bg,
            "存在广告，资源一般，可能存在Cloudflare、域名可能经常变更（可通过网站发布页查看最新域名）",
            "fiveMovie",
            "https://www.555zxdy.com",
            "https://5wuga.com",
            "/vodsearch/%s----------%s---.html",
            true,
            "/vodshow/%s-%s-%s-%s-%s----%s---%s.html",
            "",
            "",
            new ArrayList<>()),
    YJYS(SourceIndexEnum.YJYS,
            "修罗影视", // 原 哔嘀影视->缘觉影视->修罗影视
            MOVIES.title,
            MOVIES.bg,
            "质量高，无广告但更新随缘",
            "yjys",
            "https://v.xlys.ltd.ua",
            "",
            "/search/%s/%s",
            false,
            "/s/%s/%s?type=%s&area=%s&year=%s&order=%s", // /s/dongzuo/2?type=0&area=美国&year=2024&order=0
            "/danmu/%s",
            "",
            new ArrayList<>()
    ),
    XBYY(SourceIndexEnum.XBYY,
            "小宝影院",
            MOVIES.title,
            MOVIES.bg,
            "质量不错，貌似无法直接访问需挂代理",
            "xbyy",
            "https://xiaoxintv.cc",
            "",
            "/index.php/vod/search/page/%s/wd/%s.html", // 参数1：分页 参数2：搜索内容
            true,
            "/index.php/vod/show/id/%s%s.html", // 第一个参数为 ID 第二个参数为拼接参数
            "",
            "",
            new ArrayList<>()
    ),
    NYYY(SourceIndexEnum.NYYY,
            "纽约影院",
            MOVIES.title,
            MOVIES.bg,
            "质量不错，请勿相信视频中的博彩广告",
            "nyyy",
           "https://nycvod.com",
            "",
            "/vodsearch/%s----------%s---.html",
            false,
            "/vodshow/%s-%s-%s-%s-%s-%s---%s---%s.html", // 1: 影视类型 2:地区 3:排序 4:类型 5:语言 6:字母 7:分页 8:年份
            "/index.php/danmu/art/id/%s.xml",
            "",
            new ArrayList<>()
            ),
    ;

    /**
     * 在站点配置类{@link ParserInterfaceFactory}中定义
     */
    private SourceIndexEnum sourceIndexEnum;
    /**
     * 源名称
     */
    private String sourceName;
    /**
     * 源类型
     */
    private String sourceType;
    @DrawableRes
    private int sourceTypeBg;
    /**
     * 源相关描述
     */
    private String sourceInfo;
    /**
     *  {@link SharedPreferencesUtils}存储名称key
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
     * 能否搜索
     */
    private boolean canSearch;
    /**
     * 分类参数地址
     */
    private String classificationUrl;
    /**
     * 弹幕参数地址
     */
    private String danmuUrl;
    /**
     * RSS订阅地址
     */
    private String rss;
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

    static {
        for (SourceEnum sourceEnum : SourceEnum.values()) {
            int sourceIndex = sourceEnum.sourceIndexEnum.index;
            TITLE_MAP.put(sourceIndex, sourceEnum.getSourceName());
            DOMAIN_URL_MAP.put(sourceIndex, sourceEnum.getDomainUrl());
            CACHE_TITLE_MAP.put(sourceIndex, sourceEnum.getCacheTitle());
            DANMU_MAP.put(sourceIndex, sourceEnum.getDanmuUrl());
            WEBSITE_RELEASE_MAP.put(sourceIndex, sourceEnum.getWebsiteRelease());
        }
    }

    /**
     * 获取默认站点列表
     * @return
     */
    public static List<SourceDataBean> getSourceDataBeanList() {
        List<SourceDataBean> sourceDataBeans = new ArrayList<>();
        for (SourceEnum sourceEnum : SourceEnum.values()) {
            sourceDataBeans.add(new SourceDataBean(
                    sourceEnum.getSourceName(),
                    sourceEnum.getSourceIndexEnum(),
                    sourceEnum.getSourceType(),
                    sourceEnum.getSourceTypeBg(),
                    sourceEnum.getSourceInfo(),
                    !Utils.isNullOrEmpty(sourceEnum.getDanmuUrl()),
                    !Utils.isNullOrEmpty(sourceEnum.getWebsiteRelease()),
                    sourceEnum.getWebsiteRelease(),
                    !Utils.isNullOrEmpty(sourceEnum.getRss()),
                    sourceEnum.getRss()
            ));
        }
        return sourceDataBeans;
    }

    public static List<SourceDataBean> getTurnOnHiddenFeaturesList() {
        List<SourceDataBean> sourceDataBeans = new ArrayList<>();
        for (SourceEnum sourceEnum : SourceEnum.values()) {
            sourceDataBeans.add(new SourceDataBean(
                    sourceEnum.getSourceName(),
                    sourceEnum.getSourceIndexEnum(),
                    sourceEnum.getSourceType(),
                    sourceEnum.getSourceTypeBg(),
                    sourceEnum.getSourceInfo(),
                    !Utils.isNullOrEmpty(sourceEnum.getDanmuUrl()),
                    !Utils.isNullOrEmpty(sourceEnum.getWebsiteRelease()),
                    sourceEnum.getWebsiteRelease(),
                    !Utils.isNullOrEmpty(sourceEnum.getRss()),
                    sourceEnum.getRss()
            ));
        }
        return sourceDataBeans;
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
      * @包名: my.project.moviesbox.parser.config
      * @类名: SourceEnum
      * @描述: 源下标、状态等信息枚举
      * @作者: Li Z
      * @日期: 2024/8/3 22:06
      * @版本: 1.0
     */
    @Getter
    @AllArgsConstructor
    public enum SourceIndexEnum {
        // 拖布影视
        TBYS(0, NORMAL, ""),
        // 嘶哩嘶哩
        SILISILI(1, NORMAL, ""),
        // 樱花动漫
        I_YINGHUA(2, NORMAL, ""),
        // AnFuns动漫
        ANFUNS(3, DEPRECATED, "2025年6月16日访问显示网站已关闭，估计是无了"),
        // LIBVIO
        LIBVIO(4, NORMAL, ""),
        // 在线之家
        ZXZJ(5, NORMAL, ""),
        // 555电影
        FIVE_MOVIE(6, DEPRECATED, "经常更换域名、出现Cloudflare，失效后将不再维护"),
        // 7/8/9 自用暂不开源
        // 缘觉影视 2024年12月5日14:03:55发现站长回归 更名为修罗影视
        YJYS(10, ABNORMAL, "影视搜索存在服务器验证请手动验证，部分视频调用接口存在二次验证（不支持），尝试支持该站的M3U8协议播放（如果存在MP4的播放地址优先使用，不可播放再尝试M3U8协议的播放地址，不一定能播放成功），测试中可能存在应用崩溃"),
        // 小宝影院
        XBYY(11, ABNORMAL, "仅支持但并未经过详细测试，M3U8协议播放列表尝试过滤广告"),
        // 纽约影院
        NYYY(12, ABNORMAL, "影视搜索存在服务器验证请手动验证（无数据请刷新重试，这个网站有时返回的就是无数据~）"),
        ;
        public int index;
        public SourceStateEnum stateEnum;
        private String msg;

        public static SourceIndexEnum fromIndex(int index) {
            for (SourceIndexEnum source : values()) {
                if (source.index == index) {
                    return source;
                }
            }
            throw new IllegalArgumentException("Invalid index: " + index);
        }
    }

    /**
      * @包名: my.project.moviesbox.parser.config
      * @类名: SourceEnum
      * @描述: 源支持状态枚举
      * @作者: Li Z
      * @日期: 2024/8/3 22:06
      * @版本: 1.0
     */
    @Getter
    @AllArgsConstructor
    public enum SourceStateEnum {
        UNDONE(0, R.color.undone), // 计划中，并不保证最终支持该站点
        NORMAL(1, R.color.normal), // 解析正常
        ABNORMAL(2, R.color.abnormal), // 部分解析异常
        DEPRECATED(-1, R.color.deprecated) // 废弃，不再维护
        ;
        private int state;
        @ColorInt
        private int color;
    }

    /**
      * @包名: my.project.moviesbox.parser.config
      * @类名: SourceEnum
      * @描述: 源类型枚举
      * @作者: Li Z
      * @日期: 2024/8/3 22:07
      * @版本: 1.0
     */
    @Getter
    @AllArgsConstructor
    public enum SourceTypeEnum {
        MOVIES("影视", R.drawable.source_type_movie),
        ANIME("动漫", R.drawable.source_type_anime);
        public String title;
        @DrawableRes
        private int bg;
    }
}
