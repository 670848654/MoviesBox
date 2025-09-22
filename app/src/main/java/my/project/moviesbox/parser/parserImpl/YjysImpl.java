package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;
import static my.project.moviesbox.parser.config.MultiItemEnum.BANNER_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.TAG_LIST;
import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.YJYS;
import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;

import com.github.luben.zstd.ZstdInputStream;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.digest.DigestUtils;

import org.brotli.dec.BrotliInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.bean.DomainDataBean;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.parser.config.ItemStyleEnum;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.parser.config.WeekEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.sourceCustomView.VerifySearchActivity;
import my.project.moviesbox.strategy.danmu.DanmuResultEnum;
import my.project.moviesbox.strategy.danmu.DanmuStrategyFactory;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.PlayerActivity;
import my.project.moviesbox.view.WeekActivity;
import my.project.moviesbox.view.fragment.HomeFragment;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 修罗影视（原缘觉影视）站点解析实现
 * @date 2024/7/27 13:17
 */
public class YjysImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.YJYS.getPostRequestMethod();
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
        return SourceEnum.YJYS.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#YJYS
     */
    @Override
    public int getSource() {
        return YJYS.index;
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
        headers.put("Referer", getDefaultDomain() + "/");
        headers.put("Sec-Ch-Ua", "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"");
        headers.put("Sec-Ch-Ua-Mobile", "?0");
        headers.put("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.put("Sec-Fetch-Dest", "iframe");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "same-site");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
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
        try {
            Document document = Jsoup.parse(source);
            // 获取尾页的链接元素
            Element lastPageLink = document.select("ul.pagination li.page-item:contains(尾页) a").first();
            // 提取 href 中的页码
            String lastPageHref = lastPageLink.attr("href");
            String lastPageNumber=  lastPageHref.substring(lastPageHref.lastIndexOf('/') + 1);
            return Integer.valueOf(removeString(lastPageNumber, ';'));
        } catch (Exception e) {
            e.printStackTrace();
            return startPageNum();
        }
    }

    /**
     * APP首页内容解析接口
     * <p>{@link ItemStyleEnum}: 影视数据列表视图样式</p>
     *
     * @param source 网页源代码
     * @return {@link List< MainDataBean >}
     * @see MainDataBean
     * @see MainDataBean.Item
     */
    @Override
    public List<MainDataBean> parserMainData(String source) {
        try {
            Document document = Jsoup.parse(source);
            List<MainDataBean> mainDataBeans = new ArrayList<>();
            // tag内容解析
            MainDataBean mainDataBean = new MainDataBean();
            mainDataBean.setDataType(TAG_LIST.getType());
            List<MainDataBean.Tag> tags = new ArrayList<>();
            tags.add(new MainDataBean.Tag(HomeTagEnum.WEEK.name, HomeTagEnum.WEEK.content, WeekActivity.class));
            mainDataBean.setTags(tags);
            mainDataBeans.add(mainDataBean);
            /*************************** 解析banner内容开始 ***************************/
            Elements bannerList = document.getElementById("carousel-captions").select("a.carousel-item");
            if (bannerList.size() == 0)
                return null;
            List<MainDataBean.Item> bannerItems = new ArrayList<>();
            MainDataBean bannerBean = new MainDataBean();
            bannerBean.setHasMore(false);
            bannerBean.setDataType(BANNER_LIST.getType());
            bannerBean.setVodItemType(STYLE_16_9);
            for (Element a : bannerList) {
                MainDataBean.Item item = new MainDataBean.Item();
                // 名称
                item.setTitle(a.select("h3").text());
                item.setEpisodes(a.select("p").text());
                // 地址
                item.setUrl(a.attr("href"));
                // 图片
                item.setImg(a.select("img").attr("src"));
                bannerItems.add(item);
            }
            bannerBean.setItems(bannerItems);
            mainDataBeans.add(bannerBean);
            /*************************** 解析banner内容结束 ***************************/
            /*************************** 解析list内容开始 ***************************/
            Elements headers = document.select(".page-header");
            Elements vodItems = document.select(".container.px-1.px-md-2.my-2 .row-cards");
            for (int i=0,size=headers.size(); i<size; i++) {
                Element header = headers.get(i);
                String headerTitle = header.select(".page-title").text();
                String moreUrl = header.select("span.d-sm-inline a").attr("href");
                MainDataBean contentBean = new MainDataBean();
                // 更多跳转地址
                contentBean.setDataType(ITEM_LIST.getType());
                contentBean.setTitle(headerTitle);
                contentBean.setHasMore(!moreUrl.isEmpty());
                contentBean.setMore(moreUrl);
                Elements vods = vodItems.get(i).select(".card");
                List<MainDataBean.Item> vodItemList = new ArrayList<>();
                for (Element vod : vods) {
                    MainDataBean.Item item = new MainDataBean.Item();
                    String title = vod.select("h3").text(); // 标题
                    // 当为电影时 这是评分
                    Elements ribbonTop = vod.select("div.ribbon-top");
                    // 当为多集时 这个集数
                    Elements episodes = vod.select("span.badge.bg-pink");
                    item.setTitle(title);
                    item.setUrl(vod.select("a").attr("href"));
                    item.setImg(vod.select("img").attr("data-src"));
                    item.setEpisodes(Utils.isNullOrEmpty(ribbonTop) ? episodes.text() : ribbonTop.text());
                    vodItemList.add(item);
                }

                if (vodItems.size() > 0) {
                    contentBean.setItems(vodItemList);
                    mainDataBeans.add(contentBean);
                }
            }
            /*************************** 解析list内容结束 ***************************/
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
            String title = document.select(".d-sm-block.d-md-none").text();
            if (title.isEmpty())
                return null;
            detailsDataBean.setTitle(title);
            // 影视图片
            String img = document.select(".col-md-auto.col-5.cover-lg-max-25 img").attr("src");
            detailsDataBean.setImg(img);
            //影视地址
            detailsDataBean.setUrl(url);
            detailsDataBean.setUpdateTime(document.select(".bg-purple-lt").text());
            detailsDataBean.setScore(document.select(".bg-green-lt").text());
//            detailsDataBean.setIntroduction(document.select(".text-truncate .text-orange").text());
            Element synopsis = document.getElementById("synopsis");
            if (!Utils.isNullOrEmpty(synopsis))
                detailsDataBean.setIntroduction(synopsis.select(".card-body").text());
            Elements tagList = document.select("p.mb-0.mb-md-2");
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tagList) {
                String strong = tag.select("strong").text();
                if (strong.contains("类型")) {
                    Elements aList = tag.select("a");
                    for (Element a : aList) {
                        tagTitles.add(a.text());
                        tagUrls.add(a.attr("href"));
                    }
                    break;
                }
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);
            // 解析播放列表
            Elements playElements = document.select(".d-flex").select(".me-2");
            if (playElements.size() > 0) {
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                dramas.setListTitle("默认播放列表");
                List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
                int index = 0;
                for (Element drama : playElements) {
                    dramasItems.add(new DetailsDataBean.DramasItem(index++, drama.text(), drama.attr("href"), false));
                }
                dramas.setDramasItemList(dramasItems);
                dramasList.add(dramas);
                detailsDataBean.setDramasList(dramasList);
            }
            // 解析推荐列表
            Elements recommendElements = document.select(".related .row-cards .col-lg"); //相关推荐
            if (recommendElements.size() > 0) {
                List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
                for (Element recommend : recommendElements) {
                    String recommendTitle = recommend.select("h4.text-truncate").text();
                    String recommendImg = recommend.select("img").attr("src");
                    String recommendUrl = recommend.select("a").attr("href");
                    recommendList.add(new DetailsDataBean.Recommend(recommendTitle, recommendImg, recommendUrl));
                }
                detailsDataBean.setRecommendList(recommendList);
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
            Elements dataElement = document.select(".btn-group a");
            if (dataElement.size() > 0) {
                int index = 0;
                for (Element element : dataElement) {
                    String dramaUrl = element.select("a").attr("href");
                    String dramaTitle = element.select("a").text();
                    dramasItemList.add(new DetailsDataBean.DramasItem(index++, dramaTitle, dramaUrl, dramaStr.contains(dramaUrl)));
                }
            }
            logInfo("播放列表[播放界面]内容", dramasItemList.toString());
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
     * @return {@link List< ClassificationDataBean >}
     */
    @Override
    public List<ClassificationDataBean> parserClassificationList(String source) {
        try {
            Document document = Jsoup.parse(source);
            Elements elements = document.select(".all-filter-wrapper dl");
            int index = 1;
            List<ClassificationDataBean> classificationDataBeans = new ArrayList<>();
            for (Element element : elements) {
                String dt = element.select("dt").text();
                if (dt.contains("资源分类"))
                    continue;
                ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                classificationDataBean.setClassificationTitle(dt);
                classificationDataBean.setIndex(index++);
                Elements aList = element.select("a");
                List<ClassificationDataBean.Item> itemList = new ArrayList<>();
                if (dt.contains("影视类型"))
                    for (int i=0,size=aList.size(); i<size; i++) {
                        Element a = aList.get(i);
                        String title = a.text();
                        String href = a.attr("href");
                        String start ="/s/";
                        String end ="?";
                        String result = href.substring(href.indexOf(start) + start.length(), href.indexOf(end));
                        itemList.add(new ClassificationDataBean.Item(title, removeString(result, ';'), i == 0));
                    }
                else
                    for (int i=0,size=aList.size(); i<size; i++) {
                        Element a = aList.get(i);
                        String title = a.text();
                        String url = title;
                        switch (title) {
                            case "不限":
                                url = "";
                                break;
                            case "更新时间":
                                url = "0";
                                break;
                            case "豆瓣评分":
                                url = "1";
                                break;
                        }
                        itemList.add(new ClassificationDataBean.Item(title, url, i == 0));
                    }
                classificationDataBean.setItemList(itemList);
                classificationDataBeans.add(classificationDataBean);
            }
            logInfo("分类列表信息", classificationDataBeans.toString());
            return classificationDataBeans;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserClassificationList error", e.getMessage());
        }
        return null;
    }

    private String removeString(String str, char remove) {
        // 查找第一个";"号的位置，并截取之前的部分
        int semicolonIndex= str.indexOf(remove);
        if (semicolonIndex != -1) {
            // 去除";"号之后的数据
            str= str.substring(0, semicolonIndex);
        }
        return str;
    }

    /**
     * 获取剧集列表集合接口 (分类)
     *
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserClassificationVodList(String source) {
        return parserVodList(source);
    }

    @Override
    public Class searchOpenClass() {
        return VerifySearchActivity.class;
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
            List<VodDataBean> vodDataBeans  = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Element verifyCode = document.getElementById("verifyCode");
            if (Utils.isNullOrEmpty(verifyCode)) {
                Elements videoList = document.select(".col-12 .row");
                if (videoList.size() == 0)
                    return vodDataBeans;
                for (Element video : videoList) {
                    VodDataBean item = new VodDataBean();
                    String videoName = video.select(".search-movie-title").text(); // 标题
//                String dateTime = video.select("p.text-muted").text(); // r日期
//                Elements ribbonTop = video.select(".ribbon-top"); // 评分
                    item.setTitle(videoName);
                    item.setUrl(video.select(".search-movie-title").attr("href"));
                    item.setImg(video.select("img").attr("src"));
//                item.setEpisodesTag(dateTime);
//                item.setTopLeftTag(Utils.isNullOrEmpty(ribbonTop) ? "" : ribbonTop.text());
                    vodDataBeans.add(item);
                }
                logInfo("搜索视频列表数据", vodDataBeans.toString());
                return vodDataBeans;
            } else
                return null;
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
            List<VodDataBean> vodDataBeans  = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements videoList = document.select(".card.card-sm.card-link");
            if (videoList.size() == 0)
                return vodDataBeans;
            for (Element video : videoList) {
                VodDataBean item = new VodDataBean();
                String videoName = video.select(".card-title").text(); // 标题
                String dateTime = video.select("p.text-muted").text(); // r日期
                Elements ribbonTop = video.select(".ribbon-top"); // 评分
                item.setTitle(videoName);
                item.setUrl(video.select("a").attr("href"));
                item.setImg(video.select("img").attr("src"));
                item.setEpisodesTag(dateTime);
                item.setTopLeftTag(Utils.isNullOrEmpty(ribbonTop) ? "" : ribbonTop.text());
                vodDataBeans.add(item);
            }
            logInfo("视频列表数据", vodDataBeans.toString());
            return vodDataBeans;
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
        return 6;
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
        // /s/dongzuo/2?type=0&area=美国&year=2024&order=0
        // params[1] dongzuo  params[2] area params[3] year params[4] order params[5] page
        String url = getDefaultDomain() + (params[0].startsWith("/") ? params[0] : "/" + params[0]);
        int index = url.indexOf('?');
        if (index != -1) {
            // 在 '?' 前面追加内容
            url = url.substring(0, index) + "/" + params[5] + url.substring(index);
        } else {
            // 如果没有 '?'，直接追加
            url = url + "/" + params[5];
        }
        if (!Utils.isNullOrEmpty(params[1])) {
            String regex = "/s/([^/]+)/";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);

            // 使用正则表达式进行替换
            if (matcher.find()) {
                url = matcher.replaceFirst("/s/" + params[1] + "/");
            }
        }
        if (!Utils.isNullOrEmpty(params[2]))
            url += "&area=" + params[2];
        if (!Utils.isNullOrEmpty(params[3]))
            url += "&year=" + params[3];
        if (!Utils.isNullOrEmpty(params[4]))
            url += "&order=" + params[4];
        LogUtil.logInfo("getClassificationUrl", url);
        return url;
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
        String url = getDefaultDomain() + String.format(SourceEnum.YJYS.getSearchUrl(), params[0], params[1]);
        LogUtil.logInfo("getSearchUrl", url);
        return url;
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
        url = url.startsWith("/") ? url : "/" + url;
        url = url.startsWith("https") ? url : getDefaultDomain() + url;
        int index = url.indexOf('?');
        if (index != -1) {
            // 在 '?' 前面追加内容
            url = url.substring(0, index) + "/" + page + url.substring(index);
        } else {
            // 如果没有 '?'，直接追加
            url = url + "/" + page;
        }
        LogUtil.logInfo("getVodListUrl", url);
        return url;
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
       try {
           String url = params[0];
           if (!url.startsWith("http"))
               url = getDefaultDomain() + url;
           String source = OkHttpUtils.getInstance().performSyncRequestAndHeader(url);
           // 提取PID
           String pid = "";
           Document doc = Jsoup.parse(source);
           Elements scriptElements = doc.select("script");
           for (Element script : scriptElements) {
               String content = script.html();
               if (content.contains("pid")) {
                   // 使用正则表达式提取 pid 的值
                   Pattern pattern = Pattern.compile("var\\s+pid\\s*=\\s*(\\d+);");
                   Matcher matcher = pattern.matcher(content);
                   if (matcher.find()) {
                       pid = matcher.group(1);
                   }
                   break;
               }
           }
           LogUtil.logInfo("影视pid", pid);
           if (Utils.isNullOrEmpty(pid))
               return null;
           return getDefaultDomain() + String.format(SourceEnum.YJYS.getDanmuUrl(), pid);
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
    }

    /**
     * 弹幕接口返回是否为JSON
     * <p>注：弹幕只有两种格式 XML/JsonObject</p>
     * <p>JSON弹幕需自行实现弹幕解析{@link DanmuStrategyFactory#getStrategy}</p>
     * @return @return {@link DanmuResultEnum#XML} Or {@link DanmuResultEnum#JSON}
     */
    @Override
    public DanmuResultEnum getDanmuResult() {
        return DanmuResultEnum.JSON;
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
        try {
            // 提取PID
            String pid = "";
            Document doc = Jsoup.parse(source);
            Elements scriptElements = doc.select("script");
            for (Element script : scriptElements) {
                String content = script.html();
                if (content.contains("pid")) {
                    // 使用正则表达式提取 pid 的值
                    Pattern pattern = Pattern.compile("var\\s+pid\\s*=\\s*(\\d+);");
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        pid = matcher.group(1);
                    }
                    break;
                }
            }
            LogUtil.logInfo("影视pid", pid);
            if (Utils.isNullOrEmpty(pid))
                return null;
            return new ArrayList<>(getVodPlayUrl(pid));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取视频链接
     * @param pid
     * @return
     */
    private List<DialogItemBean> getVodPlayUrl(String pid) {
        List<DialogItemBean> playUrls = new ArrayList<>();
        try {
            long timestamp = new Date().getTime();
            String plaintext = String.format("%s-%s", pid, timestamp);
            String md5Hash = DigestUtils.md5Hex(plaintext);
            String key = md5Hash.substring(0, 16);

            SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            String base64Encrypted = Base64.encodeBase64String(encryptedBytes);

            // 将加密后的结果转换为Hex格式
            String hexEncrypted = base64ToHex(base64Encrypted);
            String linesApi = "%s/lines?t=%s&sg=%s&pid=%s";
            String urlString = String.format(linesApi, getDefaultDomain(), timestamp, hexEncrypted, pid);
            LogUtil.logInfo("linesApi", urlString);
            // 发送HTTP请求，处理响应
            String getResult = OkHttpUtils.getInstance().performSyncRequestAndHeader(urlString);
            LogUtil.logInfo("linesApi result", getResult);
            JSONObject jsonObject = new JSONObject(getResult);
            JSONObject data = jsonObject.getJSONObject("data");
            // 如果没有返回任何视频链接则使用API接口
            if (!data.has("url3") && !data.has("m3u8") && !data.has("m3u8_2") && !data.has("tos")) {
                // 都不包含 提示为暂不开放的影视 使用API
                getPlayUrlEncrypt(pid, playUrls, "666", "");
            }
            // 包含TOS使用API接口+参数type = {tos -> value}
            if (data.has("tos")) {
                // 使用API
                String type = data.getString("tos");
                getPlayUrlEncrypt(pid, playUrls, "888", "?type=" + type);
            }
            if (data.has("url3")) {
                String url = data.getString("url3");
                String[] urlArr = url.split(",");
                for (String playUrl : urlArr) {
                    playUrl = playUrl.replace("p16-ad-sg.ibyteimg.com", "lf6-pipixia-ckv-tos.pstatp.com");
                    playUrls.add(new DialogItemBean(playUrl, playUrl.contains("m3u8") ? M3U8 : MP4));
                }
            }
            if (data.has("m3u8")) {
                String[] m3u8Arr = data.getString("m3u8").split(",");
                setDialogItemBean(m3u8Arr, playUrls);
            }
            if (data.has("m3u8_2")) {
                String[] m3u8Arr = data.getString("m3u8_2").split(",");
                setDialogItemBean(m3u8Arr, playUrls);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playUrls;
    }

    private void setDialogItemBean(String[] m3u8Arr, List<DialogItemBean> playUrls) throws Exception {
        for (String s : m3u8Arr) {
            String s1 = s.replace("https://www.bde4.cc", getDefaultDomain());
            String[] sources = s1.split("#");
            String sourceName = sources.length > 1 ? sources[1] : "";
            s1 = removeString(s1, '#');
            LogUtil.logInfo("m3u8 url", s1);
            String localPath = saveLocalM3U8Path(s1, sourceName);
            if (!Utils.isNullOrEmpty(localPath)) {
                playUrls.add(new DialogItemBean(localPath, M3U8, s.contains("sp")));
            }
        }
    }

    private String saveLocalM3U8Path(String url, String sourceName) throws Exception {
        // 建立 HTTP 连接
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Referer", getDefaultDomain() + "/");
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setDoInput(true);

        try (InputStream in = connection.getInputStream()) {
            byte[] responseBytes = readBytes(in);
            byte[] processedData = processData(responseBytes);
            // 存入缓存
            return "file:/" + Utils.writeToFile(processedData, (sourceName.isEmpty() ? UUID.randomUUID().toString() : sourceName) + ".m3u8");
        } finally {
            connection.disconnect();
        }
    }

    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[16384];
        int bytesRead;
        while ((bytesRead = inputStream.read(chunk)) != -1) {
            buffer.write(chunk, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    private static byte[] processData(byte[] data) throws IOException {
        byte[] slicedData = new byte[data.length - 3354];
        System.arraycopy(data, 3354, slicedData, 0, slicedData.length);

        // 解压缩数据
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(slicedData))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[16384];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            String decompressedString = new String(out.toByteArray(), StandardCharsets.UTF_8);
//            LogUtil.logInfo("decompressedString", decompressedString);
            // 替换 TS URL 部分
            decompressedString = decompressedString.replaceAll("((?:[0-9A-F]+){2,}\\.ts)", "https://vod.xlys.me/$1");
//            LogUtil.logInfo("replaceAll", decompressedString);
            return decompressedString.getBytes(StandardCharsets.UTF_8);
        }
    }

    private static String base64ToHex(String base64String) {
        byte[] decodedBytes = Base64.decodeBase64(base64String);
        StringBuilder hexString = new StringBuilder();
        for (byte b : decodedBytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private final String ENCRYPT_API = "%s/god/%s%s";

    /**
     * 通过API接口获取视频地址
     * @param pid
     * @param playUrls
     * @param verifyCode
     * @param type
     * @throws Exception
     */
    private void getPlayUrlEncrypt(String pid, List<DialogItemBean> playUrls, String verifyCode, String type) throws Exception {
        // 当前时间戳（毫秒级）
        long timestamp = new Date().getTime();
        // 拼接 pid 和时间戳
        String plaintext = String.format("%s-%s", pid, timestamp);
        // 生成 AES 密钥
        String md5Hash = md5(plaintext).substring(0, 16); // 获取前16个字符
        SecretKeySpec secretKey = new SecretKeySpec(md5Hash.getBytes(StandardCharsets.UTF_8), "AES");
        // 使用 AES 加密
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        // 将加密结果转换为 Base64 字符串
        String encryptedBase64 = Base64.encodeBase64String(encryptedBytes);
        // 将 Base64 字符串转换为 Hex
        String sg = base64ToHex(encryptedBase64).toUpperCase(Locale.ROOT);
        String urlStr = String.format(ENCRYPT_API, getDefaultDomain(), pid, type);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(urlStr);
        stringBuilder.append(urlStr.contains("?") ? "&" : "?");
        stringBuilder.append("t=");
        stringBuilder.append(timestamp);
        stringBuilder.append("&sg=");
        stringBuilder.append(sg);
        stringBuilder.append("&verifyCode=");
        stringBuilder.append(verifyCode);
        LogUtil.logInfo("GET API", stringBuilder.toString());
        Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.set("Accept", "application/json, text/javascript, */*; q=0.01");
        headersBuilder.set("Accept-Encoding", "gzip, deflate, br, zstd");
        headersBuilder.set("Accept-Language", "zh-CN,zh;q=0.9");
        headersBuilder.set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headersBuilder.set("Origin", getDefaultDomain());
        headersBuilder.set("Priority", "u=1, i");
        headersBuilder.set("Sec-CH-UA", "\"Microsoft Edge\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"");
        headersBuilder.set("Sec-CH-UA-Mobile", "?1");
        headersBuilder.set("Sec-CH-UA-Platform", "\"Android\"");
        headersBuilder.set("Sec-Fetch-Dest", "empty");
        headersBuilder.set("Sec-Fetch-Mode", "cors");
        headersBuilder .set("Sec-Fetch-Site", "same-origin");
        headersBuilder.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Mobile Safari/537.36 Edg/127.0.0.0");
        headersBuilder.set("X-Requested-With", "XMLHttpRequest");
        // 构建请求参数
        /*FormBody formBody = new FormBody.Builder()
                .add("t", String.valueOf(timestamp))
                .add("sg", sg)
                .add("verifyCode", verifyCode)
                .build();*/
//        try (Response response = OkHttpUtils.getInstance().doPostDefault(stringBuilder.toString(), headersBuilder.build(), formBody)) {
        try (Response response = OkHttpUtils.getInstance().performSyncRequestAndHeader(stringBuilder.toString(), headersBuilder.build())) {
            // 获取响应码
            int responseCode = response.code();
            if (responseCode == 200) {
                String encoding = response.header("Content-Encoding");
                LogUtil.logInfo("压缩数据格式", encoding);
                byte[] responseBodyBytes = response.body().bytes();
                if ("br".equalsIgnoreCase(encoding)) {
                    byte[] bytes = unZipBrotli(responseBodyBytes);
                    String json = new String(bytes);
                    LogUtil.logInfo("api result", json);
                    parserJson(json, playUrls);
                } else if ("zstd".equalsIgnoreCase(encoding)) {
                    String json = decompressZstd(responseBodyBytes);
                    LogUtil.logInfo("api result", json);
                    parserJson(json, playUrls);
                } else {
                    String json = response.body().string();
                    LogUtil.logInfo("api result", json);
                    parserJson(json, playUrls);
                }
            }
        }
    }

    /**
     * 对brotli格式的数据解码
     *
     * @param text 需要处理的数据
     * @return 处理后的数据
     */
    private static byte[] unZipBrotli(byte[] text) {
        try (BrotliInputStream brotliInputStream = new BrotliInputStream(new ByteArrayInputStream(text));
             ByteArrayOutputStream swapStream = new ByteArrayOutputStream()) {
            //buff用于存放循环读取的临时数据
            byte[] buff = new byte[100];
            int rc;
            while ((rc = brotliInputStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            return swapStream.toByteArray();
        } catch (IOException ioException) {
            return null;
        }
    }

    /**
     * 对zstd格式的数据解码
     * @param text
     * @return
     * @throws IOException
     */
    public String decompressZstd(byte[] text) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(text);
             ZstdInputStream zis = new ZstdInputStream(bais)) {

            byte[] buffer = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int n;

            while ((n = zis.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, n, "UTF-8"));
            }

            return sb.toString();
        }
    }

    /**
     * 解析返回数据JSON
     * @param json
     * @param playUrls
     * @throws JSONException
     */
    private void parserJson(String json, List<DialogItemBean> playUrls) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("url")) {
            String playUrl = jsonObject.getString("url");
            playUrls.add(new DialogItemBean(playUrl, playUrl.contains("m3u8") ? M3U8 : MP4, true));
        }
    }

    // 计算 MD5 哈希值
    public static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * messageDigest.length);
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 通过定义的需要POST请求的类名获取POST固定参数，自行实现
     *
     * @param className {@link SourceEnum#postRequestMethod}中定义的类名
     * @return {@link FormBody}
     */
    @Override
    public FormBody getPostFormBodyByClassName(String className) {
        return null;
    }

    @Override
    public int setWeekItemType() {
        return WeekDataBean.ITEM_TYPE_2;
    }

    /**
     * 一般为新番时间表接口
     *
     * @param source 网页源代码
     * @return {@link List< WeekDataBean >}
     */
    @Override
    public List<WeekDataBean> parserWeekDataList(String source) {
        try {
            List<WeekDataBean> weekDataBeans = new ArrayList<>();
            Document document = Jsoup.parse(source);
            for (int i=0,size=7; i<size; i++) {
                int week = WeekEnum.values()[i].getIndex();
                Element weekDom = document.getElementById("tabs-"+(i+1));
                if (Utils.isNullOrEmpty(weekDom))
                    return null;
                Elements weekA = weekDom.select("a");
                List<WeekDataBean.WeekItem> weekItems = new ArrayList<>();
                for (Element a : weekA) {
                    weekItems.add(new WeekDataBean.WeekItem(
                            a.select("span.text-truncate").text(),
                            3,
                            a.attr("href"),
                            a.select("span.text-muted").text(),
                            ""
                    ));
                }
                weekDataBeans.add(new WeekDataBean(week, weekItems));
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
        return null;
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
        return null;
    }

    /**
     * 文本列表接口
     * <p>一般用于排行榜等</p>
     *
     * @param source 网页源代码
     * @return {@link List< TextDataBean >}
     */
    @Override
    public List<TextDataBean> parserTextList(String source) {
        return null;
    }

    public String getValue(String s) {
        return Utils.isNullOrEmpty(s) ? "" : s;
    }

    @Override
    public DomainDataBean parserDomain(String source) {
        try {
            Document document = Jsoup.parse(source);
            List<DomainDataBean.Domain> domainList = new ArrayList<>();
            Elements aElements = document.select("a");
            for (Element a : aElements) {
                String title = a.text();
                String href = a.attr("href");
                href = href.startsWith("http") ? href : "https://"+href;
                domainList.add(new DomainDataBean.Domain(title, href));
            }
            if (domainList.size() > 0)
                return new DomainDataBean().success(domainList);
            else
                return new DomainDataBean().error("未能正确获取到最新域名数据，请自行通过发布页查看");
        } catch (Exception e) {
            e.printStackTrace();
            return new DomainDataBean().error("获取最新域名失败："+e.getMessage());
        }
    }

    /**
     * 首页TAG枚举
     */
    @Getter
    @AllArgsConstructor
    public enum HomeTagEnum {
        WEEK("更新时间表", "%s");

        private String name;
        private String content;
    }
}
