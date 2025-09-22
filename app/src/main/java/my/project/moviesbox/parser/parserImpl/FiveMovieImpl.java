package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;
import static my.project.moviesbox.parser.config.MultiItemEnum.BANNER_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.TAG_LIST;
import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.FIVE_MOVIE;
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

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.ArrayList;
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
import my.project.moviesbox.parser.config.MultiItemEnum;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.parser.config.WeekEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.PlayerActivity;
import my.project.moviesbox.view.TextListActivity;
import my.project.moviesbox.view.TopticListActivity;
import my.project.moviesbox.view.VodListActivity;
import my.project.moviesbox.view.WeekActivity;
import my.project.moviesbox.view.fragment.HomeFragment;
import okhttp3.FormBody;

/**
  * @包名: my.project.moviesbox.parser.parserImpl
  * @类名: FiveMovieImpl
  * @描述: 555电影站点解析实现
  * @deprecated: 经常换地址、出现Cloudflare，失效后不再维护
  * @作者: Li Z
  * @日期: 2024/2/8 9:39
  * @版本: 1.0
 */
@Deprecated
public class FiveMovieImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.FIVEMOVIE.getPostRequestMethod();
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
        return SourceEnum.FIVEMOVIE.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#FIVE_MOVIE
     */
    @Override
    public int getSource() {
        return FIVE_MOVIE.index;
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
     * <p>默认使用 UTF-8</p>
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
        // tips：该网站搜索需要cookie中是否存在searchneed=ok
        headers.put("Cookie", "searchneed=ok");
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
        int pageCount = startPageNum();
        Document document = Jsoup.parse(source);
        Element pageElement = document.getElementById("page");
        if (pageElement == null)
            return startPageNum();
        Elements pageList = pageElement.select("a");
        if (pageList.size() > 0) {
            Element lastElement = pageList.get(pageList.size()-1);
            String regex = "-(\\d+)-";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(lastElement.attr("href"));
            if (matcher.find()) {
                pageCount = Integer.parseInt(matcher.group(1));
            }
            // 其他分页
            regex = "/(\\d+)\\.";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(lastElement.attr("href"));
            if (matcher.find()) {
                pageCount = Integer.parseInt(matcher.group(1));
            }
        }
        LogUtil.logInfo("pageCount", pageCount+"");
        return pageCount;
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
            MainDataBean mainDataBean = new MainDataBean();
            mainDataBean.setDataType(TAG_LIST.getType());
            List<MainDataBean.Tag> tags = new ArrayList<>();
            tags.add(new MainDataBean.Tag(HomeTagEnum.WEEK.name, HomeTagEnum.WEEK.content, WeekActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.NETFLIX.name, HomeTagEnum.NETFLIX.content, VodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DY.name, HomeTagEnum.DY.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DSJ.name, HomeTagEnum.DSJ.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.FL.name, HomeTagEnum.FL.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DM.name, HomeTagEnum.DM.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZYJL.name, HomeTagEnum.ZYJL.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.JRGX.name, HomeTagEnum.JRGX.content, VodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZT.name, HomeTagEnum.ZT.content, TopticListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.PHB.name, HomeTagEnum.PHB.content, TextListActivity.class));
            mainDataBean.setTags(tags);
            mainDataBeans.add(mainDataBean);
            /*************************** 解析banner内容开始 ***************************/
            Elements bannerList = document.select(".main .content .container-slide .swiper .swiper-wrapper .swiper-slide");
            if (bannerList.size() == 0)
                return null;
            List<MainDataBean.Item> bannerItems = new ArrayList<>();
            MainDataBean bannerBean = new MainDataBean();
            bannerBean.setTitle("影视推荐");
            bannerBean.setHasMore(false);
            bannerBean.setDataType(BANNER_LIST.getType());
            bannerBean.setVodItemType(STYLE_16_9);
            for (Element element : bannerList) {
                MainDataBean.Item item = new MainDataBean.Item();
                Elements a = element.select("a.banner");
                if (a.size() > 0) {
                    // 名称
                    item.setTitle(element.select(".mobile-v-info .v-title").text());
                    item.setEpisodes(element.select(".mobile-v-info .v-ins p").first().text());
                    // 地址
                    item.setUrl(a.attr("href"));
                    // 图片
                   /* String patternString = "url\\(([^\\)]+)\\)";
                    Pattern pattern = Pattern.compile(patternString);
                    Matcher matcher = pattern.matcher(a.attr("style"));
                    while (matcher.find()) {
                        item.setImg(matcher.group(1));
                    }*/
                    item.setImg(a.attr("data-bg").replaceAll("&amp;", "&"));
                    bannerItems.add(item);
                }
            }
            bannerBean.setItems(bannerItems);
            mainDataBeans.add(bannerBean);
            /*************************** 解析banner内容结束 ***************************/
            /*************************** 解析list内容开始 ***************************/
            Elements moduleList = document.select(".homepage .main .content .module");
            for (Element module : moduleList) {
                String title = module.select("h2").text();
                if (title.contains("追剧周表") || title.contains("影视资讯")) {
                } else {
                    boolean itemType = title.contains("本月最佳电影") || title.contains("独家专题");
                    MainDataBean contentBean = new MainDataBean();
                    if (itemType)
                        contentBean.setVodItemType(STYLE_16_9);
                    List<MainDataBean.Item> vodItems = new ArrayList<>();
                    contentBean.setDataType(ITEM_LIST.getType());
                    contentBean.setTitle(title);
                    Elements more = module.select(".module-heading-more");
                    boolean isTopic = false;
                    if (more.size() > 0) {
                        contentBean.setHasMore(true);
                        String moreUrl = more.attr("href");
                        if (moreUrl.contains("vodshow")) {
                            // 影视分类
                            String regex = "/(\\d+)-";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(moreUrl);
                            if (matcher.find()) {
                                contentBean.setMore(matcher.group(1));
                            } else {
                                contentBean.setHasMore(false);
                            }
                        } else if (moreUrl.contains("topic")) {
                            // 专题
                            isTopic = true;
                            contentBean.setHasMore(true);
                            contentBean.setMore(TbysImpl.prependTextBeforeDot(moreUrl, "/page/%s"));
                            contentBean.setOpenMoreClass(TopticListActivity.class);
                        } else if (moreUrl.contains("vodtype")) {
                            // 影视分类
                            String regex = "/(\\d+).";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(moreUrl);
                            if (matcher.find()) {
                                contentBean.setMore(matcher.group(1));
                            } else {
                                contentBean.setHasMore(false);
                            }
                        }
                    }
                    Elements aList = module.select(".module-items a");
                    for (Element a : aList) {
                        MainDataBean.Item item = new MainDataBean.Item();
                        item.setTitle(a.select(".module-poster-item-info .module-poster-item-title").text());
                        item.setUrl(a.attr("href"));
                        item.setImg(a.getElementsByTag("img").attr("data-original"));
                        item.setEpisodes(a.select(".module-item-note").text());
                        if (isTopic)
                            item.setOpenClass(VodListActivity.class);
                        vodItems.add(item);
                    }
                    if (vodItems.size() > 0) {
                        contentBean.setItems(vodItems);
                        mainDataBeans.add(contentBean);
                    }
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
            Elements content = document.select(".main .content .module.module-info .module-main");
            if (content.size() == 0)
                return null;
            String title = content.select(".module-info-main .module-info-heading h1").text();
            String img = content.select(".module-info-poster .module-item-cover .module-item-pic img").attr("data-original");
            detailsDataBean.setTitle(title);
            //影视图片
            detailsDataBean.setImg(img);
            //影视地址
            detailsDataBean.setUrl(url);
            // tag
            Elements tags = document.select(".module-info-main .module-info-heading .module-info-tag .module-info-tag-link a");
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tags) {
                if (tag.text().isEmpty()) continue;
                tagTitles.add(tag.text().toUpperCase());
                tagUrls.add(insertBeforeThirdLastHyphen(tag.attr("href"), "replaceMe"));
            }
            Elements tags2 = content.select(".module-info-content .module-info-item .module-info-item-content a");
            for (Element tag : tags2) {
                if (tag.text().isEmpty()) continue;
                tagTitles.add(tag.text().toUpperCase());
                tagUrls.add(insertBeforeThirdLastHyphen(tag.attr("href"), "replaceMe"));
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);
            Elements data = content.select(".module-info-content .module-info-item");
            for (Element s : data) {
                if (s.text().contains("更新"))
                    detailsDataBean.setUpdateTime(s.text());
                else if (s.text().contains("集数") || s.text().contains("备注") || s.text().contains("片长"))
                    detailsDataBean.setInfo(s.text());
                else if (s.text().contains("豆瓣"))
                    detailsDataBean.setScore(s.text());
            }
            String introduction = content.select(".module-info-introduction-content p").text().replaceAll("\\s+", "");
            detailsDataBean.setIntroduction(introduction);
            // 获取所有播放列表
            Elements playTitleList = document.getElementById("y-playList").select(".module-tab-item");
            Elements ulList = document.select(".module-list.sort-list.tab-list .module-play-list .module-play-list-content");
            LogUtil.logInfo("playTitleList", playTitleList.html());
            if (playTitleList.size() > 0) {
                // 解析播放列表
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                for (int i=0,size=playTitleList.size(); i<size; i++) {
                    DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                    String playListName = playTitleList.get(i).select("span").text();
                    dramas.setListTitle(playListName);
                    List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
                    int index = 0;
                    for (Element a : ulList.get(i).select("a.module-play-list-link")) {
                        String name = a.select("span").text();
                        String watchUrl = a.attr("href");
                        dramasItems.add(new DetailsDataBean.DramasItem(index++, name, watchUrl, false));
                    }
                    dramas.setDramasItemList(dramasItems);
                    dramasList.add(dramas);
                }
                detailsDataBean.setDramasList(dramasList);
                // 解析剧集相关多季 该网点无
                // 解析推荐列表
                Elements recommendElements = document.select(".main .content .module .module-main .module-items.module-poster-items-base a"); //相关推荐
                if (recommendElements.size() > 0) {
                    List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
                    for (Element recommend : recommendElements) {
                        String recommendTitle = recommend.attr("title");
                        String recommendImg = recommend.getElementsByTag("img").attr("data-original");
                        String recommendUrl = recommend.attr("href");
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
            // 不知道播放页面为啥拿不到数据~
            /*Elements dataElement = document.select(".module-play-list-larger");
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
            // 通过详情界面获取播放列表
            String descUrl = document.select(".module-player-info .module-info-heading h1 a").attr("href");
            String responseData = OkHttpUtils.getInstance().performSyncRequest(getDefaultDomain() + descUrl);
            Document descHtml = Jsoup.parse(responseData);
            // 获取所有播放列表
            Elements playTitleList = descHtml.getElementById("y-playList").select(".module-tab-item");
            Elements ulList = descHtml.select(".module-list.sort-list.tab-list .module-play-list .module-play-list-content");
            if (playTitleList.size() > 0) {
                // 解析播放列表
                Elements playing = null;
                for (int i = 0, size = playTitleList.size(); i < size; i++) {
                    Elements aList = ulList.get(i).select("a.module-play-list-link");
                    for (Element a : aList) {
                        String watchUrl = a.attr("href");
                        // 因为dramaStr第一个一定是最后播放的地址，根据这个地址判断上次播放是那个源！
                        if (dramaStr.startsWith(watchUrl)) {
                            playing = aList;
                            break;
                        }
                    }
                }
                if (playing != null) {
                    int index = 0;
                    for (Element a : playing) {
                        String dramaTitle = a.select("span").text();
                        String dramaUrl = a.attr("href");
                        dramasItemList.add(new DetailsDataBean.DramasItem(index++, dramaTitle, dramaUrl, false));
                    }
                }
            }
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
     * @return {@link List< ClassificationDataBean >}
     */
    @Override
    public List<ClassificationDataBean> parserClassificationList(String source) {
        try {
            Document document = Jsoup.parse(source);
            List<ClassificationDataBean> classificationDataBeans = new ArrayList<>();
//            String pageTitle = document.getElementsByTag("title").text();
            // 当前地址是否包含 连续剧
            String[] seriesValues = {"2", "13", "15", "44", "45"};
            String nowVodId = "";
            Matcher matcher = Pattern.compile("/(\\d+)").matcher(document.getElementById("page").select("a").get(0).attr("href"));
            if (matcher.find())
                nowVodId = matcher.group(1);
            boolean isSeries = false;
            for (String value : seriesValues) {
                if (nowVodId.equals(value)) {
                    isSeries = true;
                    break;
                }
            }
            Elements columns = document.select(".module-class-items .module-class-item");
            int index = isSeries ? -1 : 0;

            for (Element column : columns) {
                index++;
                ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                String classificationTitle = column.select(".module-item-title").text();
                classificationDataBean.setClassificationTitle(classificationTitle);
                classificationDataBean.setMultipleChoices(false);

                // 福利分类只有排序，单独处理
                classificationDataBean.setIndex(nowVodId.equals("124") ? 5 : index);

                if (isSeries && classificationTitle.contains("地区")) {
                    index++;
                }

                List<ClassificationDataBean.Item> items = new ArrayList<>();
                Elements aList = column.select(".module-item-box a");
                for (Element a : aList) {
                    String title = a.text();
                    if (isSeries && index == 0) {
                        // 连续剧类型单独处理
                        String vodId = a.attr("href").replaceAll(".*/(\\d+)-.*", "$1");
                        items.add(new ClassificationDataBean.Item(title, vodId, nowVodId.equals(vodId)));
                    } else if (!title.contains("排序")) {
                        items.add(new ClassificationDataBean.Item(title, title.equals("全部") ? "" : title, title.equals("全部")));
                    }
                }
                classificationDataBean.setItemList(items);
                classificationDataBeans.add(classificationDataBean);
            }

            // 添加分类选项
            for (ClassificationDataBean bean : classificationDataBeans) {
                if (bean.getClassificationTitle().equals("排序")) {
                    bean.getItemList().add(new ClassificationDataBean.Item("全部", "", true));
                    bean.getItemList().add(new ClassificationDataBean.Item("时间排序", "time", false));
                    bean.getItemList().add(new ClassificationDataBean.Item("人气排序", "hits", false));
                    bean.getItemList().add(new ClassificationDataBean.Item("评分排序", "score", false));
                }
            }

            logInfo("分类列表信息", classificationDataBeans.toString());
            return classificationDataBeans;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parseClassificationList error", e.getMessage());
            return null;
        }
    }

    /**
     * 获取剧集列表集合接口 (分类)
     *
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean>  parserClassificationVodList(String source) {
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select(".module-items.module-poster-items-base a");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.select(".module-poster-item-title").text());
                    bean.setUrl(item.attr("href"));
                    bean.setImg(item.select("img").attr("data-original"));
                    bean.setEpisodesTag(item.select(".module-item-note").text());
                    bean.setTopLeftTag(item.select(".module-item-douban").text());
                    items.add(bean);
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
    public List<VodDataBean>  parserSearchVodList(String source) {
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select(".module-items.module-card-items .module-card-item.module-item a.module-card-item-poster");
            if (elements.size() > 0) {
                for (Element a : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(a.parent().getElementsByTag("strong").text());
                    bean.setUrl(a.attr("href"));
                    bean.setImg(a.select("img").attr("data-original"));
                    String tags = a.parent().select(".module-info-item-content").text();
                    if (!Utils.isNullOrEmpty(tags)) {
                        String[] tagArr = tags.split("/");
                        if (tagArr.length > 2) {
                            bean.setTopLeftTag(tagArr[0].replaceAll(" ", ""));
                            bean.setEpisodesTag(tagArr[1].replaceAll(" ", ""));
                        }
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
    public List<VodDataBean>  parserVodList(String source) {
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);

            Elements elements = document.select(".module-items a");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.select(".module-poster-item-title").text());
                    bean.setUrl(item.attr("href"));
                    bean.setImg(item.select("img").attr("data-original"));
                    bean.setEpisodesTag(item.select(".module-item-note").text());
                    bean.setTopLeftTag(item.select(".module-item-douban").text());
                    items.add(bean);
                }
                logInfo("视频列表数据", items.toString());
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserVodList error", e.getMessage());
        }
        List<VodDataBean>  vodDataBean = parserClassificationVodList(source);
        return Utils.isNullOrEmpty(vodDataBean) ? parserSearchVodList(source) : vodDataBean;
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
        return 7;
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
        /**
         * 固定格式
         * [0] 类型
         * [1] 剧情
         * [2] 地区
         * [3] 语言
         * [4] 年份
         * [5] 排序
         * [6] 分页
         */
        String lx = Utils.isNullOrEmpty(params[0]) ? "" : params[0];
        String jq = Utils.isNullOrEmpty(params[1]) ? "" : params[1];
        String dq = Utils.isNullOrEmpty(params[2]) ? "" : params[2];
        String yy = Utils.isNullOrEmpty(params[3]) ? "" : params[3];
        String nf = Utils.isNullOrEmpty(params[4]) ? "" : params[4];
        String px = Utils.isNullOrEmpty(params[5]) ? "" : params[5];
        String page = Utils.isNullOrEmpty(params[6]) ? "" : params[6];
        // 类型 - 地区 - 排序 - 剧情 - 语言 - 分页 - 年代 | 网站URL
        LogUtil.logInfo("getClassificationUrl", getDefaultDomain() + String.format(SourceEnum.FIVEMOVIE.getClassificationUrl(), lx, dq, px, jq, yy, page, nf));
        return getDefaultDomain() + String.format(SourceEnum.FIVEMOVIE.getClassificationUrl(), lx, dq, px, jq, yy, page, nf);
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
        return getDefaultDomain() + String.format(SourceEnum.FIVEMOVIE.getSearchUrl(), params[0], params[1]);
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
        LogUtil.logInfo("getVodListUrl", url);
        if (url.contains("topicdetail")) {
            // 专题视频
            return getDefaultDomain() + url;
        } else if (url.contains("replaceMe"))
            return getDefaultDomain() + url.replaceAll("replaceMe", String.valueOf(page));
        else // 默认
            return getDefaultDomain() + String.format(url, page);
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
        return null;
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
            List<DialogItemBean> result = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements scriptTags = document.select("script");
            for (Element script : scriptTags) {
                String scriptHtml = script.html();
                // 1.直接从网页源代码提取
                // 2.也可以调用接口请求 https://s5knz.com/voddisp/id/{影视ID}/sid/{线路ID}/nid/{集数}.html
                // 这里使用源代码的方式
                if (scriptHtml.contains("player_aaaa") && scriptHtml.endsWith("}")) {
                    logInfo("JavaScript", scriptHtml);
                    JSONObject jsonObject = extractJsonObject(scriptHtml);
                    if (jsonObject != null) {
                        String vodPlayUrl = getVodPlayUrl(jsonObject);
                        if (!Utils.isNullOrEmpty(vodPlayUrl))
                            result.add(new DialogItemBean(vodPlayUrl, vodPlayUrl.contains("m3u8") ? M3U8 : MP4));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("getPlayUrls error", e.getMessage());
        }
        return new ArrayList<>();
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
            Elements weekElements = document.select(".module-main.tab-list");
            for (int i = 0, size = WeekEnum.values().length; i<size; i++) {
                Elements aLi = weekElements.get(i).select("a");
                int week = WeekEnum.values()[i].getIndex();
                List<WeekDataBean.WeekItem> weekItems = new ArrayList<>();
                for (Element a : aLi) {
                    weekItems.add(new WeekDataBean.WeekItem(
                            a.attr("title"),
                            a.select(".module-item-cover .module-item-pic img").attr("data-original"),
                            a.attr("href"),
                            a.select(".module-item-cover .module-item-note").text(),
                            ""
                    ));
                }
                weekDataBeans.add(new WeekDataBean(week, weekItems));
            }
            logInfo("时间表数据", weekDataBeans.toString());
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
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select(".module-items.module-items.module-topic-items a");
            if (elements.size() > 0) {
                for (Element a : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setVodItemStyleType(VodItemStyleEnum.STYLE_16_9.getType());
                    bean.setTitle(a.select(".module-poster-item-info").text());
                    bean.setUrl(a.attr("href"));
                    bean.setImg(a.select("img").attr("data-original"));
                    items.add(bean);
                }
            }
            logInfo("专题列表", items.toString());
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserSearchVodList error", e.getMessage());
            return null;
        }
    }

    /**
     * 动漫专题视频列表接口
     *
     * @param source
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserTopticVodList(String source) {
        return parserClassificationVodList(source);
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
        LogUtil.logInfo("getTopticUrl", getDefaultDomain() + String.format(url, page));
        return getDefaultDomain() + String.format(url, page);
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
        return getDefaultDomain() + url;
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
        try {
            List<TextDataBean> textDataBeans = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select(".module-paper-item.module-item");
            if (elements.size() > 0) {
                for (Element element : elements) {
                    String topTitle = element.getElementsByTag("h3").text();
                    Elements aList = element.select(".module-paper-item-main a");
                    int index = 0;
                    List<TextDataBean.Item> itemList = new ArrayList<>();
                    for (Element a : aList) {
                        index += 1;
                        TextDataBean.Item rankItem = new TextDataBean.Item();
                        rankItem.setIndex(String.valueOf(index));
                        rankItem.setTitle(a.select("span.module-paper-item-infotitle").text());
                        rankItem.setUrl(a.attr("href"));
                        rankItem.setEpisodes(a.select(".module-paper-item-info p").text());
                        itemList.add(rankItem);
                    }
                    textDataBeans.add(new TextDataBean(topTitle, itemList));
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
     * 首页TAG枚举
     */
    @Getter
    @AllArgsConstructor
    public enum HomeTagEnum {
        WEEK("追番周表", "%s/label/week.html"),
        NETFLIX("Netflix", "/label/netflix/page/%s.html"),
        DY("电影", "1"),
        DSJ("电视剧", "2"),
        FL("福利", "124"),
        DM("动漫", "4"),
        ZYJL("综艺记录", "3"),
        JRGX("最近更新", "/label/new/page/%s.html"),
        ZT("专题列表", "/label/topic/page/%s.html"),
        PHB("排行榜", "/label/hot.html");
        private String name;
        private String content;
    }

    /**
     * 在倒数第三-前追加数据
     * @param input
     * @param textToInsert
     * @return
     */
    public static String insertBeforeThirdLastHyphen(String input, String textToInsert) {
        // 找到倒数第三个"-"的位置
        int thirdLastHyphenIndex = input.length();
        for (int i = input.length() - 1, count = 0; i >= 0; i--) {
            if (input.charAt(i) == '-') {
                count++;
                if (count == 3) {
                    thirdLastHyphenIndex = i;
                    break;
                }
            }
        }
        // 在找到的位置插入文本
        StringBuilder sb = new StringBuilder(input);
        sb.insert(thirdLastHyphenIndex, textToInsert);
        return sb.toString();
    }

    // -------------------------------- 获取播放地址方法开始 --------------------------------
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY = "81f834a7f68d4c52", IV = "zkz8scsGXttFVZBb";

    /**
     * 获取JavaScript中的JSON文本
     * @param html
     * @return
     */
    private JSONObject extractJsonObject(String html) {
        String jsonText = html.substring(html.indexOf("{"), html.lastIndexOf("}") + 1);
        return JSON.parseObject(jsonText);
    }

    /**
     * 获取播放地址
     * @param jsonObject
     * @return
     * @throws Exception
     */
    private String getVodPlayUrl(JSONObject jsonObject) throws Exception {
        String url = jsonObject.getString("url");
        url = url.replace("O0O0O", "=")
                .replace("o000o", "+")
                .replace("oo00o", "/");
        LogUtil.logInfo("urlReplace", url);
        url = decryptWithAES(url);
        LogUtil.logInfo("urlDecrypt", url);
        return url;
    }

    private static String decryptWithAES(String encryptedData) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec sKeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
        byte[] encryptedBytes = Base64.decodeBase64(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    @Override
    public DomainDataBean parserDomain(String source) {
        try {
            Document document = Jsoup.parse(source);
            List<DomainDataBean.Domain> domainList = new ArrayList<>();
            String dataInfo = document.getElementById("domainData").attr("data-info");
            String decodedString = new String(android.util.Base64.decode(dataInfo, android.util.Base64.DEFAULT));
            // {"site_main":"y5tkl.com","site_1":"b5mrx.com","site_2":"s5knz.com","site_3":"p5jvy.com"}
            JSONObject jsonObject = JSON.parseObject(decodedString);
            int index = 0;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                index += 1;
                String key = entry.getKey();
                String value = entry.getValue().toString();
                value = value.startsWith("http") ? value : "https://"+value;
                if (key.equals("site_main"))
                    domainList.add(new DomainDataBean.Domain("主用", value));
                else
                    domainList.add(new DomainDataBean.Domain("备用"+index, value));
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
}
