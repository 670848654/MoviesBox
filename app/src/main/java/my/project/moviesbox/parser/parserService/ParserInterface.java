package my.project.moviesbox.parser.parserService;

import androidx.annotation.LayoutRes;

import java.util.List;
import java.util.Map;

import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.model.DanmuModel;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.view.PlayerActivity;
import okhttp3.FormBody;

/**
  * @包名: my.project.moviesbox.parser.parserService
  * @类名: ParserInterface
  * @描述: 站点数据解析接口
  * @作者: Li Z
  * @日期: 2024/1/26 13:38
  * @版本: 1.0
 */
public interface ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     * @see SourceEnum#postRequestMethod
     * @return
     */
    List<String> getPostMethodClassName();

    /**
     * 站点名称
     * <p>请在站点枚举类中配置</p>
     * @see SourceEnum#sourceName
     * @return
     */
    String getSourceName();

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     * @see SourceEnum#source
     * @return
     */
    int getSource();

    /**
     * 站点默认域名
     * <p>请在站点枚举类中配置</p>
     * @see SourceEnum#domainUrl
     * @return
     */
    default String getDefaultDomain() {
        return SharedPreferencesUtils.getUserSetDomain(getSource());
    }

    /**
     * 设置网页源代码编码
     * <p>默认使用 UTF-8</p>
     * @return
     */
    default String setCharset() {
        return "UTF-8";
    }

    /**
     * 站点请求头部
     * <p>某些网站访问需要添加请求头部</p>
     * @return
     */
    Map<String,String> requestHeaders();

    /**
     * 站点图片访问头部
     * <p>某些网站访问图片需要添加请求头部</p>
     * @return
     */
    Map<String,String> setImgHeaders();

    /**
     * 站点分页开始页码
     * @return
     */
    int startPageNum();

    /**
     * 站点影视列表获取分页总数
     * @param source 网页源代码
     * @return 获取失败或只有一页时应返回 {@link #startPageNum}
     */
    default int parserPageCount(String source) {
        return startPageNum();
    }

    /**
     * 设置影视数据列表视图样式
     * @see VodDataBean#ITEM_TYPE_0
     * @see VodDataBean#ITEM_TYPE_1
     * @return {@link LayoutRes}
     */
    @LayoutRes
    default int setVodListItemType() {
        return VodDataBean.ITEM_TYPE_0;
    }

    /**
     * APP首页内容解析接口
     * <p>{@link MainDataBean#TAG_LIST}: TAG列表内容</p>
     * <p>{@link MainDataBean#BANNER_LIST}: 轮播列表内容</p>
     * <p>{@link MainDataBean#ITEM_LIST}: 内容块列表内容 如：热门、推荐板块内容</p>
     * <p>{@link MainDataBean.Item#ITEM_TYPE_0}: 影视数据列表视图样式</p>
     * <p>{@link MainDataBean.Item#ITEM_TYPE_1}: 影视数据列表视图样式</p>
     * @see MainDataBean
     * @see MainDataBean.Item
     * @param source 网页源代码
     * @return {@link List<MainDataBean>}
     */
    List<MainDataBean> parserMainData(String source);

    /**
     * 详情内容解析接口
     * @param url 源地址
     * @param source 网页源代码
     * @return {@link DetailsDataBean}
     */
    DetailsDataBean parserDetails(String url, String source);

    /**
     * 通过播放地址获取当前播放源的所有剧集(用于播放界面选集)
     * @param source 网页源代码
     * @return {@link DetailsDataBean.DramasItem}
     */
    List<DetailsDataBean.DramasItem> parserNowSourcesDramas(String source, int listSource, String dramaStr);

    /**
     * 设置分类组是否为多选联动
     * @return
     */
    boolean setClassificationGroupMultipleChoices();

    /**
     * 获取剧集分类列表接口 (分类)
     * @param source
     * @return {@link List<ClassificationDataBean>}
     */
    List<ClassificationDataBean> parserClassificationList(String source);

    /**
     * 获取剧集列表集合接口 (分类)
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    VodDataBean parserClassificationVodList(String source);

    /**
     * 获取剧集列表集合接口 (搜索)
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    VodDataBean parserSearchVodList(String source);

    /**
     *  拖布影视的详情TAG地址 [其他影视列表]
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    VodDataBean parserVodList(String source);

    /**
     * 设置分类数据列表参数长度
     * <p>根据站点分类地址自行实现如何定义参数</p>
     * <p>将在 {@link my.project.moviesbox.view.HomeFragment}中跳转
     * {@link my.project.moviesbox.view.ClassificationVodListActivity}时初始化数组，确保长度避免使用时出现下标越界</p>
     * @return
     */
    int setClassificationParamsSize();

    /**
     * 获取分类数据列表地址
     * <p>注：分页参数强制为数组最后一位</p>
     * @param params 分类相关参数数组，通过{@link #setClassificationParamsSize()}定义的参数数组长度进行分类数据地址
     * @return
     */
    String getClassificationUrl(String[] params);

    /**
     * 获取搜索地址
     * <p>固定格式：下标0为搜索参数 1为页码</p>
     * <p>注：分页参数强制为数组最后一位</p>
     * @param params 搜索参数 params[0]:关键字 params[1]:分页参数
     * @return
     */
    String getSearchUrl(String[] params);

    /**
     * 影视列表地址
     * <p>一般用于相关界面点击某个控件跳转的影视列表界面，自行实现相关解析方案</p>
     * @param url 访问地址
     * @param page 分页参数
     * @return
     */
    String getVodListUrl(String url, int page);

    /**
     * 弹幕接口地址
     * <p>只用于在线播放界面{@link PlayerActivity}</p>
     * @param params 弹幕接口参数数组，需要自行实现{@link PlayerActivity#getDanmuParams()}传参
     * @return
     */
    String getDanmuUrl(String[] params);

    /**
     * 弹幕接口返回是否为JSON
     * <p>注：弹幕只有两种格式 XML/JsonObject</p>
     * <p>JSON弹幕需自行实现弹幕解析{@link DanmuModel#getDanmu(DanmuContract.LoadDataCallback, String...)}</p>
     * @return true JSON格式 false XML格式
     */
    boolean getDanmuResultJson();

    /**
     * 获取影视播放地址
     * <p>注：可能存在多个播放地址，兼容返回LIST</p>
     * @param source 网页源代码
     * @return
     */
    List<String> getPlayUrl(String source);

    /**
     * 通过定义的需要POST请求的类名获取POST固定参数，自行实现
     * @param className {@link SourceEnum#postRequestMethod}中定义的类名
     * @return {@link FormBody}
     */
    FormBody getPostFormBodyByClassName(String className);


    /****************************** 以下为通用横、竖屏列表数量接口 START ******************************/
    /**
     * 默认视频列表一行显示几个内容
     * @param isPad 是否为平板
     * @param isPortrait 是否为竖屏
     * @return 返回不能为0！！！ 需自己实现 平板、手机横竖屏显示数量
     */
    default int setVodListItemSize(boolean isPad, boolean isPortrait) {
        return isPad ? (isPortrait ? 5 : 8) : (isPortrait ? 3 : 5);
    }

    /**
     * 默认收藏列表一行显示几个内容
     * @param isPad 是否为平板
     * @param isPortrait 是否为竖屏
     * @return 返回不能为0！！！ 需自己实现 平板、手机横竖屏显示数量
     */
    default int setFavoriteListItemSize(boolean isPad, boolean isPortrait) {
        return isPad ? (isPortrait ? 5 : 8) : (isPortrait ? 3 : 5);
    }

    /**
     * 默认历史记录列表一行显示几个内容
     * @param isPad 是否为平板
     * @param isPortrait 是否为竖屏
     * @return 返回不能为0！！！ 需自己实现 平板、手机横竖屏显示数量
     */
    default int setHistoryListItemSize(boolean isPad, boolean isPortrait) {
        return isPad ? 5 : (isPortrait ? 1 : 2);
    }

    /**
     * 默认下载列表一行显示几个内容
     * @param isPad 是否为平板
     * @param isPortrait 是否为竖屏
     * @return 返回不能为0！！！ 需自己实现 平板、手机横竖屏显示数量
     */
    default int setDownloadListItemSize(boolean isPad, boolean isPortrait) {
        return isPad ? 5 : (isPortrait ? 1 : 2);
    }

    /**
     * 默认下载子列表一行显示几个内容
     * @param isPad 是否为平板
     * @param isPortrait 是否为竖屏
     * @return 返回不能为0！！！ 需自己实现 平板、手机横竖屏显示数量
     */
    default int setDownloadDataListItemSize(boolean isPad, boolean isPortrait) {
        return isPad ? 5 : (isPortrait ? 1 : 2);
    }

    /**
     * 默认详情剧集展开列表一行显示几个内容
     * @param isPad 是否为平板
     * @return 返回不能为0！！！
     */
    default int setDetailExpandListItemSize(boolean isPad) {
        return isPad ? 8 : 4;
    }
    /****************************** 以下为通用横、竖屏列表数量接口 END ******************************/

    /****************************** 以下为[动漫网站]特殊解析数据接口 START ******************************/
    /**
     * 设置时间表数据列表样式
     * @see WeekDataBean#ITEM_TYPE_0
     * @see WeekDataBean#ITEM_TYPE_1
     * @see WeekDataBean#ITEM_TYPE_2
     * @return {@link LayoutRes}
     * @return
     */
    @LayoutRes
    default int setWeekItemType() {
        return WeekDataBean.ITEM_TYPE_0;
    }
    /**
     * 一般为新番时间表接口
     * @param source 网页源代码
     * @return {@link List<WeekDataBean>}
     */
    List<WeekDataBean> parserWeekDataList(String source);

    /**
     * 时间列表一行显示几个内容
     * @param isPad 是否为平板
     * @param isPortrait 是否为竖屏
     * @return 返回不能为0！！！ 需自己实现 平板、手机横竖屏显示数量
     */
    default int setWeekItemListItemSize(boolean isPad, boolean isPortrait) {
        if (setWeekItemType() == WeekDataBean.ITEM_TYPE_2)
            return isPad ? (isPortrait ? 4 : 6) : (isPortrait ? 2 : 3);
        else
            return setVodListItemSize(isPad, isPortrait);
    }

    /**
     * 动漫专题数据列表接口
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    VodDataBean parserTopticList(String source);

    /**
     * 设置动漫专题数据列表样式
     * @see VodDataBean#ITEM_TYPE_0
     * @see VodDataBean#ITEM_TYPE_1
     * @return {@link LayoutRes}
     */
    @LayoutRes
    default int setTopticListItemType() {
        return VodDataBean.ITEM_TYPE_1;
    }

    /**
     * 动漫专题视频列表接口
     * @param source
     * @return {@link VodDataBean}
     */
    VodDataBean parserTopticVodList(String source);

    /**
     * 获动漫专题地址
     * <p>注：一般只有分页参数，因此写死为分页参数</p>
     * @param url 地址
     * @param page 分页参数
     * @return
     */
    String getTopticUrl(String url, int page);

    /**
     * 动漫专题列表一行显示几个内容
     * @param isPad 是否为平板
     * @param isPortrait 是否为竖屏
     * @return 返回不能为0！！！ 需自己实现 平板、手机横竖屏显示数量
     */
    default int setTopticItemListItemSize(boolean isPad, boolean isPortrait) {
        return isPad ? (isPortrait ? 3 : 5) : (isPortrait ? 2 : 4);
    }

    /**
     * 获取文本列表地址
     * <p>一般用于排行榜等，无分页</p>
     * @param url 访问地址
     * @return
     */
    String getTextUrl(String url);

    /**
     * 文本列表接口
     * <p>一般用于排行榜等</p>
     * @param source 网页源代码
     * @return {@link List<TextDataBean>}
     */
    List<TextDataBean> parserTextList(String source);
    /****************************** 以下为[动漫网站]特殊解析数据接口 END ******************************/
}
