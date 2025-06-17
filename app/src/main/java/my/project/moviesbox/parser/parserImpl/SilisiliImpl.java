package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.TAG_LIST;
import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.SILISILI;
import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.model.DanmuModel;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.parser.bean.TodayUpdateBean;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.parser.bean.rss.SilisiliRssFeed;
import my.project.moviesbox.parser.config.ItemStyleEnum;
import my.project.moviesbox.parser.config.MultiItemEnum;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.parser.config.WeekEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.md5.DigestUtils;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.HomeFragment;
import my.project.moviesbox.view.PlayerActivity;
import my.project.moviesbox.view.TextListActivity;
import my.project.moviesbox.view.TopticListActivity;
import my.project.moviesbox.view.WeekActivity;
import okhttp3.FormBody;

/**
  * @包名: my.project.moviesbox.parser.impl
  * @类名: SilisiliImpl
  * @描述: 嘶哩嘶哩站点解析实现
  * @作者: Li Z
  * @日期: 2024/1/23 16:38
  * @版本: 1.0
 */
public class SilisiliImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.SILISILI.getPostRequestMethod();
    }

    /**
     * 站点名称
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#sourceName
     */
    @Override
    public String getSourceName() {
        return SourceEnum.SILISILI.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#SILISILI
     */
    @Override
    public int getSource() {
        return SILISILI.index;
    }


    /**
     * 站点默认域名
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#domainUrl
     */
    @Override
    public String getDefaultDomain() {
        return ParserInterface.super.getDefaultDomain();
    }

    /**
     * 设置网页源代码编码
     * <p>不设置默认使用 UTF-8</p>
     *
     * @return
     */
    @Override
    public String setCharset() {
        return ParserInterface.super.setCharset();
    }

    /**
     * 站点请求头部
     * <p>某些网站访问需要添加请求头部</p>
     *
     * @return
     */
    @Override
    public Map<String, String> requestHeaders() {
        Map<String, String> headers = new HashMap<>();
        // tips：该网站直接访问是进不去的，需要验证cookie中是否存在silisili=on
        headers.put("Cookie", "silisili=on;path=/;max-age=86400");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Origin", getDefaultDomain());
        headers.put("Sec-Ch-Ua", "\"Microsoft Edge\";v=\"117\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"117\"");
        headers.put("Sec-Ch-Ua-Mobile", "?0");
        headers.put("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.60");
        return headers;
    }

    /**
     * 站点图片访问头部
     * <p>某些网站访问图片需要添加请求头部</p>
     *
     * @return
     */
    @Override
    public Map<String, String> setImgHeaders() {
        return null;
    }

    /**
     * 站点分页开始页码
     *
     * @return
     */
    @Override
    public int startPageNum() {
        return 1;
    }

    /**
     * 站点影视列表获取分页总数
     *
     * @param source 网页源代码
     * @return 获取失败或只有一页时应返回 {@link #startPageNum}
     */
    @Override
    public int parserPageCount(String source) {
        Document document = Jsoup.parse(source);
        Elements searchPageElements = document.select("ul.list-page > li");
        Elements vodListPageElements = document.select("div.page > a");
        if (searchPageElements.size() > 0) { // 搜索界面的分页规则
            return getPageCount(searchPageElements);
        } else if (vodListPageElements.size() > 0){ // 视频列表界面的分页规则
            return getPageCount(vodListPageElements);
        } else
            return startPageNum();
    }

    /**
     * APP首页内容解析接口
     * <p>{@link MultiItemEnum}: 列表ITEM样式</p>
     * <p>{@link ItemStyleEnum}: 影视数据列表视图样式</p>
     * @see MainDataBean
     * @see MainDataBean.Item
     * @param source 网页源代码
     * @return {@link List<MainDataBean>}
     */
    @Override
    public List<MainDataBean> parserMainData(String source) {
        try {
            Document document = Jsoup.parse(source);
            List<MainDataBean> mainDataBeans = new ArrayList<>();
            MainDataBean mainDataBean;
            mainDataBean = new MainDataBean();
            mainDataBean.setDataType(TAG_LIST.getType());
            List<MainDataBean.Tag> tags = new ArrayList<>();
            tags.add(new MainDataBean.Tag(HomeTagEnum.WEEK.name, HomeTagEnum.WEEK.content, WeekActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.XFRM.name, HomeTagEnum.XFRM.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.XFGM.name, HomeTagEnum.XFGM.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.WJDM.name, HomeTagEnum.WJDM.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DMJC.name, HomeTagEnum.DMJC.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.RMZT.name, HomeTagEnum.RMZT.content, TopticListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.PHB.name, HomeTagEnum.PHB.content, TextListActivity.class));
            mainDataBean.setTags(tags);
            mainDataBeans.add(mainDataBean);
            // 推荐
            Elements recommendLi = document.select("div.focus").select("div.swiper-slide");
            recommendLi.select("div.swiper-slide-votitle > span").remove();
            mainDataBean = new MainDataBean();
            mainDataBean.setTitle("热门推荐");
            mainDataBean.setHasMore(false);
            // 高>宽图片建议使用列表模式而不是banner
//        mainDataBean.setDataType(Utils.isPad() ? MainDataBean.ITEM_LIST : MainDataBean.BANNER_LIST);
            mainDataBean.setDataType(ITEM_LIST.getType());
            List<MainDataBean.Item> items = new ArrayList<>();
            for (Element recommend : recommendLi) {
                MainDataBean.Item item = new MainDataBean.Item();
                String title = recommend.select("div.swiper-slide-votitle").text();
                String url = recommend.select("a").attr("href");
                String img = getImg(recommend.select("a").attr("style"));
                String episodes = "";
                for (Element div : recommend.select("div")) {
                    if (div.attr("style").contains("ff5c7ca6")) {
                        episodes = div.text();
                        break;
                    }
                }
                item.setTitle(title);
                item.setUrl(url);
                item.setImg(img);
                item.setEpisodes(episodes);
                items.add(item);
            }
            mainDataBean.setItems(items);
            mainDataBeans.add(mainDataBean);
            // 今日热门
            Elements hotTodayLi = document.select("div.index_slide_r > div.sliderlist > div.sliderli");
            mainDataBean = new MainDataBean();
            mainDataBean.setTitle("今日热门");
            mainDataBean.setHasMore(false);
            mainDataBean.setDataType(ITEM_LIST.getType());
            items = new ArrayList<>();
            for (Element hotToday : hotTodayLi) {
                MainDataBean.Item homeItemBean = new MainDataBean.Item();
                String title = hotToday.select("div.list-body").text();
                String url = hotToday.select("a").attr("href");
                String img = getImg(hotToday.select("i.thumb").attr("style"));
                String episodes = hotToday.select("time.d-inline-block").text();
                homeItemBean.setTitle(title);
                homeItemBean.setUrl(url);
                homeItemBean.setImg(img);
                homeItemBean.setEpisodes(episodes);
                items.add(homeItemBean);
            }
            mainDataBean.setItems(items);
            mainDataBeans.add(mainDataBean);
            // 站内推荐
            Elements recommendLeftList = document.select("div.illust-list-left > div.list-left-img-box > a");
            Elements recommendRightList = document.select("div.illust-list-right > div.list-right-img-box > div.list-box > a");
            Elements recommendList = new Elements();
            recommendList.addAll(recommendLeftList);
            recommendList.addAll(recommendRightList);
            if (recommendList.size() > 0) {
                mainDataBean = new MainDataBean();
                mainDataBean.setTitle("站内推荐");
                mainDataBean.setHasMore(false);
                mainDataBean.setDataType(ITEM_LIST.getType());
                items = new ArrayList<>();
                for (Element a : recommendList) {
                    if (!a.attr("href").contains("voddetail")) continue;
                    MainDataBean.Item homeItemBean = new MainDataBean.Item();
                    String title = a.select("span").text();
                    String url = a.attr("href");
                    String imgSrc = a.select("img").attr("src");
                    String img = getImg((imgSrc.isEmpty() || imgSrc.contains("load")) ? a.select("img").attr("data-url") : imgSrc);
                    homeItemBean.setTitle(title);
                    homeItemBean.setUrl(url);
                    homeItemBean.setImg(img);
                    items.add(homeItemBean);
                }
                mainDataBean.setItems(items);
                mainDataBeans.add(mainDataBean);
            }
            // 更新动态
            Elements updateLi = document.select("article.article");
            updateLi.select("span.arc_v2").remove();
            mainDataBean = new MainDataBean();
            mainDataBean.setTitle("更新动态");
            mainDataBean.setHasMore(false);
            mainDataBean.setDataType(ITEM_LIST.getType());
            items = new ArrayList<>();
            for (Element update : updateLi) {
                MainDataBean.Item homeItemBean = new MainDataBean.Item();
                String title = update.select("h2.entry-title").text();
                String url = update.select("h2.entry-title > a").attr("href");
                String img = update.select("img.scrollLoading").attr("src");
                String episodes = update.select("div.entry-meta").text();
                homeItemBean.setTitle(title);
                homeItemBean.setUrl(url);
                homeItemBean.setImg(img);
                homeItemBean.setEpisodes(episodes);
                items.add(homeItemBean);
            }
            if (items.size() > 0) {
                mainDataBean.setItems(items);
                mainDataBeans.add(mainDataBean);
            }
            logInfo("首页内容", mainDataBeans.toString());
            return mainDataBeans;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserMainData error", e.getMessage());
        }
        return null;
    }

    /**
     * 详情内容解析接口
     *
     * @param url    源地址
     * @param source 网页源代码
     * @return {@link DetailsDataBean}
     */
    @Override
    public DetailsDataBean parserDetails(String url, String source) {
        try {
            DetailsDataBean detailsDataBean = new DetailsDataBean();
            Document document = Jsoup.parse(source);
            String title = document.select("h1.entry-title").first().ownText().trim();
            String img = getImg(getImg(document.select("div.v_sd_l > img").attr("src")));
            detailsDataBean.setTitle(title);
            //番剧图片
            detailsDataBean.setImg(img);
            //番剧地址
            detailsDataBean.setUrl(url);
            // tag
            Elements tags = document.select("p.data").select("a");
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tags) {
                if (tag.text().isEmpty()) continue;
                tagTitles.add(tag.text().toUpperCase());
                tagUrls.add(tag.attr("href"));
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);
            Elements span = document.select("p.data span.text-muted");
            for (Element s : span) {
                if (s.text().contains("更新：")) {
                        detailsDataBean.setUpdateTime(s.parent().text());
                    break;
                }
            }
            detailsDataBean.setScore(document.select("div.v_sd_r").select("span.data-favs-num").text());
            Elements desc = document.select("div.v_cont");
            desc.select("div.v_sd").remove();
            desc.select("span").remove();
            detailsDataBean.setIntroduction(desc.text().replaceAll("^(\\s+)", ""));
            // 获取所有播放列表
            Elements playTitleList = document.select("div.play-pannel-box");
            if (playTitleList.size() > 0) {
                // 解析播放列表
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                for (Element element : playTitleList) {
                    DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                    String playListName = element.select("div.widget-title").text();
                    String playListTip = element.select("span.pull-right").text();
                    String playListTitle;
                    if (playListTip.isEmpty())
                        playListTitle = playListName;
                    else
                        playListTitle = playListName + "  ["+playListTip+"]";
                    if (playListTitle.contains("下载")) continue;
                    dramas.setListTitle(playListName);
                    Elements liList = element.select("ul > li");
                    List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
                    int index = 0;
                    for (Element drama : liList) {
                        index += 1;
                        String name = drama.select("a").text();
                        String watchUrl = drama.select("a").attr("href");
//                    Log.e("dramaStr - > " , dramaStr + "- > " + watchUrl);
                        dramasItems.add(new DetailsDataBean.DramasItem(index, name, watchUrl, false));
                    }
                    dramas.setDramasItemList(dramasItems);
                    dramasList.add(dramas);
                }
                detailsDataBean.setDramasList(dramasList);
                // 解析剧集相关多季 该网点无
                // 解析推荐列表
                Elements recommendElements = document.select("div.vod_hl_list").select("a"); //相关推荐
                if (recommendElements.size() > 0) {
                    List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
                    for (int i = 0, size = recommendElements.size(); i < size; i++) {
                        String recommendTitle = recommendElements.get(i).select("div.list-body").text();
                        String recommendImg = getImg(recommendElements.get(i).select("i.thumb").attr("style"));
                        String recommendUrl = recommendElements.get(i).attr("href");
                        recommendList.add(new DetailsDataBean.Recommend(recommendTitle, recommendImg, recommendUrl));
                    }
                    detailsDataBean.setRecommendList(recommendList);
                }
            }
            logInfo("详情信息", detailsDataBean.toString());
            return detailsDataBean;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserDetails error", e.getMessage());
        }
        return null;
    }

    /**
     * 通过播放地址获取当前播放源的所有剧集(用于播放界面选集)
     *
     * @param source     网页源代码
     * @param listSource
     * @param dramaStr
     * @return {@link DetailsDataBean.DramasItem}
     */
    @Override
    public List<DetailsDataBean.DramasItem> parserNowSourcesDramas(String source, int listSource, String dramaStr) {
        try {
            Document document = Jsoup.parse(source);
            List<DetailsDataBean.DramasItem> dramasItemList = new ArrayList<>();
            // 2024年4月3日13:55:42 发现该播放列表只会返回当前源的列表 不需要下面的判断了
            Elements aList = document.select("a");
            if (aList.size() > 0) {
                int index = 0;
                for (Element a : aList) {
                    index += 1;
                    String dramaTitle = a.text();
                    String dramaUrl = a.attr("href");
                    dramasItemList.add(new DetailsDataBean.DramasItem(index, dramaTitle, dramaUrl, false));
                }
            }
            /*Elements dataElement = document.select("ul.playlist");
            if (dataElement.size() > 0) {
                Elements playing = null;
                for (int i=0, size=dataElement.size(); i<size; i++) {
                    Elements playElements = dataElement.get(i).select("a"); //剧集列表
                    for (Element dramaList : playElements) {
                        String watchUrl = dramaList.attr("href");
                        // 因为dramaStr第一个一定是最后播放的地址，根据这个地址判断上次播放是那个源！
                        if (dramaStr.startsWith(watchUrl)) {
                            playing = playElements;
                            break;
                        }
                    }
                }
                if (playing != null) {
                    int index = 0;
                    for (Element element : playing) {
                        index += 1;
                        String dramaTitle = element.text();
                        String dramaUrl = element.attr("href");
                        dramasItemList.add(new DetailsDataBean.DramasItem(index, dramaTitle, dramaUrl, false));
                    }
                }
            }*/
            logInfo("播放列表信息", dramasItemList.toString());
            return dramasItemList;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserNowSourcesDramas error", e.getMessage());
        }
        return null;
    }

    /**
     * 设置分类组是否为多选联动
     *
     * @return
     */
    @Override
    public boolean setClassificationGroupMultipleChoices() {
        return true;
    }

    /**
     * 获取剧集分类列表接口 (分类)
     *
     * @param source
     * @return {@link List<ClassificationDataBean>}
     */
    @Override
    public List<ClassificationDataBean> parserClassificationList(String source) {
        // 分类网站有问题，暂时不解析 TODO
        return null;
    }

    /**
     * 获取剧集列表集合接口 (分类)
     *
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserClassificationVodList(String source) {
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select("article.article");
            if (elements.size() > 0) {
                for (int i = 0, size = elements.size(); i < size; i++) {
                    VodDataBean item = new VodDataBean();
                    Element header = elements.get(i).getElementsByTag("header").get(0);
                    header.select("span").remove();
                    item.setTitle(header.text());
                    item.setUrl(elements.get(i).select("div.entry-media > a").attr("href"));
                    item.setImg(getImg(elements.get(i).select("div.entry-media > a > img").attr("src")));
                    String tags = elements.get(i).select(".entry-meta").text().replaceAll(" ", "");
                    if (!Utils.isNullOrEmpty(tags)) {
                        String[] tagArr = tags.split("/");
                        if (tagArr.length > 0)
                            item.setEpisodesTag(tagArr[tagArr.length - 1]);
                    }
                    items.add(item);
                }
                logInfo("分类列表数据", items.toString());
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserClassificationVodList error", e.getMessage());
        }
        return null;
    }

    /**
     * 获取剧集列表集合接口 (搜索)
     *
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserSearchVodList(String source) {
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select("article.post-list");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.select("div.search-image").select("a").attr("title"));
                    bean.setUrl(item.select("div.search-image").select("a").attr("href"));
                    bean.setImg(getImg(item.select("div.search-image").select("img").attr("srcset")));
                    String tags = item.select(".entry-meta").text().replaceAll(" ", "");
                    if (!Utils.isNullOrEmpty(tags)) {
                        String[] tagArr = tags.split("/");
                        if (tagArr.length > 0)
                            bean.setEpisodesTag(tagArr[tagArr.length - 1]);
                    }
                    items.add(bean);
                }
                logInfo("搜索列表数据", items.toString());
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserSearchVodList error", e.getMessage());
        }
        return null;
    }

    /**
     * 拖布影视的详情TAG地址 [其他影视列表]
     *
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserVodList(String source) {
        try {
            Document document = Jsoup.parse(source);
            Elements elements = document.select("article.post-list");
            if (elements.size() > 0) {
                // 搜索类型的界面解析
                return parserSearchVodList(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserVodList error", e.getMessage());
        }
        return null;
    }

    /**
     * 设置分类数据列表参数长度
     * <p>根据站点分类地址自行实现如何定义参数</p>
     * <p>将在 {@link HomeFragment}中跳转
     * {@link ClassificationVodListActivity}时初始化数组，确保长度避免使用时出现下标越界</p>
     *
     * @return
     */
    @Override
    public int setClassificationParamsSize() {
        // [0] 为 HomeTagEnum.content 值 [1] 为 分页参数 固定格式
        return 2;
    }

    /**
     * 获取分类数据列表地址
     * <p>注：分页参数强制为数组最后一位</p>
     *
     * @param params 分类相关参数数组，通过{@link #setClassificationParamsSize()}定义的参数数组长度进行分类数据地址
     * @return
     */
    @Override
    public String getClassificationUrl(String[] params) {
        // [0] 为 HomeTagEnum.content 值 [1] 为 分页参数 固定格式
        return String.format(params[0], getDefaultDomain(), params[1]);
    }

    /**
     * 获取搜索地址
     * <p>固定格式：下标0为搜索参数 1为页码</p>
     * <p>注：分页参数强制为数组最后一位</p>
     *
     * @param params 搜索参数 params[0]:关键字 params[1]:分页参数
     * @return
     */
    @Override
    public String getSearchUrl(String[] params) {
        // https://www.silisili.link/vodsearch/?wd=%s&page=%s
        // 固定格式：下标0为搜索参数 1为页码
        return getDefaultDomain() + String.format(SourceEnum.SILISILI.getSearchUrl(), params[0], params[1]);
    }

    /**
     * 影视列表地址
     * <p>一般用于相关界面点击某个控件跳转的影视列表界面，自行实现相关解析方案</p>
     *
     * @param url  访问地址
     * @param page 分页参数
     * @return
     */
    @Override
    public String getVodListUrl(String url, int page) {
        if (url.contains("vodsearch")) {
            // 详情TAG点击的地址 https://www.silisili.link/vodsearch/class/%E5%8A%A8%E7%94%BB/page/1
            return getDefaultDomain() + url + "page/"+page;
        }
        return null;
    }

    /**
     * 弹幕接口地址
     * <p>只用于在线播放界面{@link PlayerActivity}</p>
     *
     * @param params 弹幕接口参数数组，需要自行实现{@link PlayerActivity#getDanmuParams()}传参
     * @return
     */
    @Override
    public String getDanmuUrl(String[] params) {
        // 该站点查询弹幕只需要标题和下标
        return getDefaultDomain() + String.format(SourceEnum.SILISILI.getDanmuUrl(), params[0], params[1]);
    }

    /**
     * 弹幕接口返回是否为JSON
     * <p>注：弹幕只有两种格式 XML/JsonObject</p>
     * <p>JSON弹幕需自行实现弹幕解析{@link DanmuModel#getDanmu(DanmuContract.LoadDataCallback, String...)}</p>
     *
     * @return true JSON格式 false XML格式
     */
    @Override
    public boolean getDanmuResultJson() {
        return false;
    }

    /**
     * 获取影视播放地址
     * <p>注：可能存在多个播放地址，兼容返回LIST</p>
     *
     * @param source 网页源代码
     * @return
     */
    @Override
    public List<DialogItemBean> getPlayUrl(String source, boolean isDownload) {
        List<DialogItemBean> urls = new ArrayList<>();
        String url = getJsonData(true, source);
        urls.add(new DialogItemBean(url, url.contains("m3u8") ? M3U8 : MP4));
        return urls;
    }

    /**
     * 通过定义的需要POST请求的类名获取POST固定参数，自行实现
     *
     * @param className {@link SourceEnum#postRequestMethod}中定义的类名
     * @return {@link FormBody}
     */
    @Override
    public FormBody getPostFormBodyByClassName(String className) {
        if (getPostMethodClassName().contains(className)) {
            // 获取播放地址
            return new FormBody.Builder().add("player", "sili").build();
        }
        return null;
    }

    /**
     * 一般为新番时间表接口
     *
     * @param source 网页源代码
     * @return {@link List<WeekDataBean>}
     */
    @Override
    public List<WeekDataBean> parserWeekDataList(String source) {
        try {
            List<WeekDataBean> weekDataBeans = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements weekElements = document.select("div.week_item").select("ul.tab-content");
            if (weekElements.size() > 0) {
                // 改网站周日排在第一位 重新排序
                Element sunday = weekElements.get(0);
                weekElements.remove(0);
                weekElements.add(sunday);
                for (int i=0,size=WeekEnum.values().length; i<size; i++) {
                    Elements weekLi = weekElements.get(i).select("li");
                    int week = WeekEnum.values()[i].getIndex();
                    List<WeekDataBean.WeekItem> weekItems = new ArrayList<>();
                    for (Element li : weekLi) {
                        String style = li.select("a.item-cover span").attr("style");
                        String regex = "https://[^\\s\\)]+";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(style);
                        weekItems.add(new WeekDataBean.WeekItem(
                                li.select("a.item-cover").attr("title"),
                                matcher.find() ? matcher.group() : "",
                                li.select("a.item-cover").attr("href"),
                                li.select("p.num").text(),
                                ""
                        ));
                    }
                    weekDataBeans.add(new WeekDataBean(week, weekItems));
                }
                logInfo("时间表数据", weekDataBeans.toString());
            }
            return weekDataBeans;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserWeekDataList error", e.getMessage());
        }
        return null;
    }

    /**
     * 动漫专题数据列表接口
     *
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserTopticList(String source) {
        try {
            Document document = Jsoup.parse(source);
            Elements elements = document.select("div.search-image > a");
            if (elements.size() > 0) {
                List<VodDataBean> items = new ArrayList<>();
                for (Element element : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setVodItemStyleType(VodItemStyleEnum.STYLE_16_9.getType());
                    bean.setTitle(element.attr("title"));
                    bean.setUrl(element.attr("href"));
                    bean.setImg(element.select("img").attr("src"));
                    items.add(bean);
                }
                logInfo("动漫专题数据", items.toString());
                return items;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserTopticList error", e.getMessage());
        }
        return null;
    }

    /**
     * 动漫专题视频列表接口
     *
     * @param source
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserTopticVodList(String source) {
        try {
            Document document = Jsoup.parse(source);
            Elements elements = document.select("div.topic-item").select("a");
            if (elements.size() > 0) {
                List<VodDataBean> items = new ArrayList<>();
                for (Element element : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(element.select("div.list-body").text());
                    bean.setUrl(element.attr("href"));
                    bean.setImg(getImg(element.select("i").attr("style")));
                    items.add(bean);
                }
                logInfo("动漫专题数据", items.toString());
                return items;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserTopticVodList error", e.getMessage());
        }
        return null;
    }

    /**
     * 获动漫专题地址
     * <p>注：一般只有分页参数，因此写死为分页参数</p>
     *
     * @param url  地址
     * @param page 分页参数
     * @return
     */
    @Override
    public String getTopticUrl(String url, int page) {
        if (url.contains("%s"))
            return String.format(url, getDefaultDomain());
        else
            return getDefaultDomain() + url;
    }

    /**
     * 获取文本列表地址
     * <p>一般用于排行榜等，无分页</p>
     *
     * @param url 访问地址
     * @return
     */
    @Override
    public String getTextUrl(String url) {
        return String.format(url, getDefaultDomain());
    }

    /**
     * 文本列表接口
     * <p>一般用于排行榜等</p>
     *
     * @param source 网页源代码
     * @return {@link List<TextDataBean>}
     */
    @Override
    public List<TextDataBean> parserTextList(String source) {
        try {
            List<TextDataBean> textDataBeans = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select("div.top-item-box");
            if (elements.size() > 0) {
                for (Element e : elements) {
                    String topTitle = e.select("div.widget-title").text();
                    Elements items = e.select("div.top-item");
                    for (Element item : items) {
                        String subTitle = item.select("h5").text();
                        Elements as = item.select("ul.top-list > li > a");
                        List<TextDataBean.Item> itemList = new ArrayList<>();
                        int index = 0;
                        for (Element a : as) {
                            index += 1;
                            TextDataBean.Item rankItem = new TextDataBean.Item();
//                            rankItem.setIndex(a.select("span.badge").text());
                            rankItem.setIndex(String.valueOf(index));
                            rankItem.setTitle(a.select("span.tit").text());
                            rankItem.setUrl(a.attr("href"));
                            a.select("span.remen").remove();
                            rankItem.setContent("热度:"+a.select("span.score").text());
                            itemList.add(rankItem);
                        }
                        textDataBeans.add(new TextDataBean(topTitle + "["+subTitle+"]", itemList));
                    }
                }
                logInfo("排行榜数据", textDataBeans.toString());
            }
            return textDataBeans;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserTextList error", e.getMessage());
        }
        return null;
    }

    /**
     * 获取订阅地址
     * @return
     */
    @Override
    public String getRssUrl() {
        return getDefaultDomain()+SourceEnum.SILISILI.getRss();
    }

    /**
     * 通过站点的RSS订阅获取今日更新数据
     *
     * @param xml 网页源代码
     * @return {@link List<TodayUpdateBean>}
     */
    @Override
    public List<TodayUpdateBean> parserRss(String xml) {
        List<TodayUpdateBean> todayUpdateBeans = new ArrayList<>();
        Serializer serializer = new Persister();
        try {
            SilisiliRssFeed rssFeed = serializer.read(SilisiliRssFeed.class, xml);
            if (rssFeed != null && rssFeed.getChannel() != null) {
                // 获取今日日期
                String nowDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                for (SilisiliRssFeed.Item item : rssFeed.getChannel().getItems()) {
                    String[] itemTitleArr = item.getTitle().split(" ");
                    // 获取更新集数信息 永远是数组最后一个
                    String info = itemTitleArr[itemTitleArr.length - 1];
                    StringBuilder title = new StringBuilder();
                    for (int i=0; i<itemTitleArr.length - 1; i++) {
                        if (i != 0)
                            title.append(" ");
                        title.append(itemTitleArr[i]);
                    }
                    String url = item.getLink();
                    // 正则表达式，用于匹配 /voddetail/8kp7777Z/
                    String regex = "(\\/voddetail\\/[^\\/]+\\/)";
                    // 编译正则表达式
                    Pattern pattern = Pattern.compile(regex);
                    // 创建匹配器对象
                    Matcher matcher = pattern.matcher(url);
                    if (matcher.find()) {
                        // 输出匹配的结果
                        url = matcher.group(1);
                    }
                    String pubDate = item.getPubDate();
                    String update = pubDate.split(" ")[0];
                    if (nowDate.equals(update))
                        todayUpdateBeans.add(new TodayUpdateBean(title.toString(), info, pubDate, url));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return todayUpdateBeans;
    }

    // -------------------------------- 该解析通用方法 --------------------------------
    private final static Pattern IMG_PATTERN = Pattern.compile("http(.*)");

    /**
     * 统一分页正则处理
     * @param elements
     * @return
     */
    private int getPageCount(Elements elements) {
        String pageCount = elements.get(elements.size() - 2).text().replaceAll("\\.", "");
        try {
            return Integer.valueOf(pageCount);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 处理图片
     * @param text
     * @return
     */
    private static String getImg(String text) {
        Matcher m = IMG_PATTERN.matcher(text);
        while (m.find())
            return m.group().replaceAll("\\)", "").replaceAll(";", "");
        return text;
    }

    /**
     * 首页TAG枚举
     */
    @Getter
    @AllArgsConstructor
    public enum HomeTagEnum {
        WEEK("番剧时间表", "%s"),
        XFRM("新番日漫", "%s/vodtype/xinfanriman-%s/"),
        XFGM("新番国漫", "%s/vodtype/xinfanguoman-%s/"),
        WJDM("完结动漫", "%s/vodtype/dongmanfanju-%s/"),
        DMJC("动漫剧场", "%s/vodtype/juchang-%s/"),
        RMZT("热门专题", "%s/topic/"),
        PHB("排行榜", "%s/map.html");

        private String name;
        private String content;
    }

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 获取数据
     * @param getPlayUrl true 返回播放地址 false 返回分集HTML
     * @param jsonStr 解密数据
     * @return
     */
    public static String getJsonData(boolean getPlayUrl, String jsonStr) {
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            String data = jsonObject.getString(getPlayUrl? "url" : "fenjihtml");
            String result = Utils.isNullOrEmpty(data) ? "" : data;
            LogUtil.logInfo(getPlayUrl ? "播放地址" : "剧集列表", result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 解密数据
     * @param encryptData 加密数据
     * @return
     */
    public static String getDecodeData(String encryptData) {
        logInfo("加密数据", encryptData);
        try {
            Security.addProvider(new BouncyCastleProvider());
            String params1 = encryptData.substring(9);
            String params2 = encryptData.substring(0, 9);
            params2 = DigestUtils.md5DigestAsHex((params2).getBytes());
            String ivT = params2.substring(0, 16);
            String keyT = params2.substring(16);
            IvParameterSpec iv = new IvParameterSpec(ivT.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec sKeySpec = new SecretKeySpec(keyT.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(params1.getBytes()));
            String result = new String(original, StandardCharsets.UTF_8);
            LogUtil.logInfo("解密后", result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
