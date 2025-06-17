package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;
import static my.project.moviesbox.parser.config.MultiItemEnum.BANNER_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.TAG_LIST;
import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;

import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.model.DanmuModel;
import my.project.moviesbox.net.OkHttpUtils;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.bean.ClassificationDataBean;
import my.project.moviesbox.parser.bean.DetailsDataBean;
import my.project.moviesbox.parser.bean.DialogItemBean;
import my.project.moviesbox.parser.bean.MainDataBean;
import my.project.moviesbox.parser.bean.TextDataBean;
import my.project.moviesbox.parser.bean.VodDataBean;
import my.project.moviesbox.parser.bean.WeekDataBean;
import my.project.moviesbox.parser.config.ItemStyleEnum;
import my.project.moviesbox.parser.config.MultiItemEnum;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.sourceCustomView.VerifySearchActivity;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoUtils;
import my.project.moviesbox.view.BaseActivity;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.HomeFragment;
import my.project.moviesbox.view.PlayerActivity;
import okhttp3.FormBody;

/**
 * @author Li
 * @version 1.0
 * @description: 纽约影视站点解析实现
 * @date 2024/10/10 14:19
 */
public class NyyyImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.NYYY.getPostRequestMethod();
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
        return SourceEnum.NYYY.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#index
     */
    @Override
    public int getSource() {
        return SourceEnum.SourceIndexEnum.NYYY.index;
    }

    /**
     * 站点请求头部
     * <p>某些网站访问需要添加请求头部</p>
     *
     * @return
     */
    @Override
    public Map<String, String> requestHeaders() {
        return null;
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

    @Override
    public HashMap<String, String> setPlayerHeaders() {
        return ParserInterface.super.setPlayerHeaders();
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

    @Override
    public int parserPageCount(String source) {
        try {
            Document document = Jsoup.parse(source);
            Elements pageLinks = document.select(".page-info a");
            if (Utils.isNullOrEmpty(pageLinks))
                return startPageNum();
            Element lastLink = pageLinks.last();
            String href = lastLink.attr("href");
            String regex = "-(\\d+)-";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return startPageNum();
        } catch (Exception e) {
            e.printStackTrace();
            return startPageNum();
        }
    }

    /**
     * APP首页内容解析接口
     * <p>{@link MultiItemEnum}: 列表ITEM样式</p>
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
            MainDataBean tagBean = new MainDataBean();
            tagBean.setDataType(TAG_LIST.getType());
            List<MainDataBean.Tag> tags = new ArrayList<>();
            tags.add(new MainDataBean.Tag(HomeTagEnum.DY.name, HomeTagEnum.DY.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DSJ.name, HomeTagEnum.DSJ.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZY.name, HomeTagEnum.ZY.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DM.name, HomeTagEnum.DM.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.JJTY.name, HomeTagEnum.JJTY.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DJ.name, HomeTagEnum.DJ.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.LL.name, HomeTagEnum.LL.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.JLP.name, HomeTagEnum.JLP.content, ClassificationVodListActivity.class));
            tagBean.setTags(tags);
            mainDataBeans.add(tagBean);
            /*************************** 解析banner内容开始 ***************************/
            List<MainDataBean.Item> bannerItems = new ArrayList<>();
            MainDataBean bannerBean = new MainDataBean();
            bannerBean.setDataType(BANNER_LIST.getType());
            bannerBean.setVodItemType(STYLE_16_9);
            Elements slides = document.select(".slide-time-bj.swiper-slide");
            for (Element slide : slides) {
                Elements aElements = slide.select("a");
                if (aElements.size() > 0) {
                    MainDataBean.Item item = new MainDataBean.Item();
                    // 名称
                    item.setTitle(aElements.select(".slide-info-title").text());
                    Elements remarks = aElements.select(".slide-info-remarks");
                    item.setEpisodes(remarks.size() > 1 ? remarks.get(1).text() : remarks.get(0).text());
                    // 地址
                    item.setUrl(aElements.attr("href"));
                    // 图片
                    String imageUrl = extractImageUrl(aElements.select(".slide-time-img3").attr("style"));
                    item.setImg(getImgPath(imageUrl));
                    bannerItems.add(item);
                }
            }
            bannerBean.setItems(bannerItems);
            mainDataBeans.add(bannerBean);
            /*************************** 解析banner内容结束 ***************************/
            /*************************** 解析list内容开始 ***************************/
            Elements boxElements = document.select(".box-width.wow.fadeInUp");
            if (boxElements.size() == 0)
                return null;
            for (Element box : boxElements) {
                MainDataBean contentBean = new MainDataBean();
                contentBean.setDataType(ITEM_LIST.getType());
                contentBean.setTitle(box.select("h4.title-h").text());
                Elements aElements = box.select(".title-right a");
                String moreUrl = aElements.attr("href");
                if (!Utils.isNullOrEmpty(moreUrl)) {
                    // 更多跳转地址
                    contentBean.setHasMore(true);
                    contentBean.setMore(extractNumber(moreUrl));
                }
                Elements vods = box.select("a.public-list-exp");
                List<MainDataBean.Item> vodItemList = new ArrayList<>();
                for (Element vod : vods) {
                    MainDataBean.Item item = new MainDataBean.Item();
                    String title = vod.attr("title"); // 标题
                    String updateInfo = vod.select("span.public-prt").text();
                    item.setTitle(title);
                    item.setUrl(vod.attr("href"));
                    item.setImg(getImgPath(vod.select("img").attr("data-src")));
                    item.setEpisodes(updateInfo);
                    vodItemList.add(item);
                }
                contentBean.setItems(vodItemList);
                mainDataBeans.add(contentBean);
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

    private static String extractImageUrl(String style) {
        String prefix = "url('";
        int start = style.indexOf(prefix);
        if (start != -1) {
            start += prefix.length();
            int end = style.indexOf("')", start);
            if (end != -1) {
                return style.substring(start, end);
            }
        }
        return "";
    }

    private static String extractNumber(String url) {
        // 使用正则表达式匹配 / 和 - 之间的数字
        Pattern pattern = Pattern.compile("/vodshow/(\\d+)-");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1); // 提取的数字
        }
        return "";
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
            String title = document.select(".detail-info h3.slide-info-title").text();
            detailsDataBean.setTitle(title);
            // 影视图片
            String img = document.select(".detail-pic img").attr("data-src");
            detailsDataBean.setImg(getImgPath(img));
            //影视地址
            detailsDataBean.setUrl(url);
            Elements infoElements = document.select(".slide-info.hide");
            for (Element info : infoElements) {
                if (info.text().contains("上映"))
                    detailsDataBean.setInfo(info.text());
                else if (info.text().contains("更新"))
                    detailsDataBean.setUpdateTime(info.text());
            }
            detailsDataBean.setScore(document.select(".score-title .text-site").text() + " " + document.select(".fraction").text());
            detailsDataBean.setIntroduction(document.getElementById("height_limit").text());
            Elements tagList = infoElements.select("a");
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tagList) {
                String tagTitle = tag.text();
                if (!tagTitle.isEmpty()) {
                    String href = tag.attr("href");
                    String regex = "-(.*)-";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(href);
                    if (matcher.find()) {
                        tagTitles.add(tag.text());
                        tagUrls.add(matcher.group(1).replaceAll("-", ""));
                    }
                }
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);
            // 解析播放列表
            Elements playListTitleElements = document.select(".anthology-tab .swiper-wrapper a");
            Elements playListElements = document.select("ul.anthology-list-play");
            if (playListElements.size() > 0) {
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                for (int i=0,size=playListElements.size(); i<size; i++) {
                    Elements dramaElements = playListElements.get(i).select("li a");
                    DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                    dramas.setListTitle(playListTitleElements.get(i).ownText());
                    List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
                    int index = 0;
                    for (Element drama : dramaElements) {
                        dramasItems.add(new DetailsDataBean.DramasItem(index++, drama.text(), drama.attr("href"), false));
                    }
                    dramas.setDramasItemList(dramasItems);
                    dramasList.add(dramas);
                    detailsDataBean.setDramasList(dramasList);
                }
            }
            // 解析推荐列表
            Elements recommendElements = document.select(".box-width.wow.fadeInUp").select("a.public-list-exp");
            List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
            for (Element recommend : recommendElements) {
                String recommendTitle = recommend.attr("title");
                String recommendImg = getImgPath(recommend.select("img").attr("data-src"));
                String recommendUrl = recommend.attr("href");
                recommendList.add(new DetailsDataBean.Recommend(recommendTitle, getImgPath(recommendImg), recommendUrl));
            }
            detailsDataBean.setRecommendList(recommendList);
            logInfo("详情信息", detailsDataBean.toString());
            return detailsDataBean;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserDetails error", e.getMessage());
        }
        return null;
    }

    @Override
    public Class<? extends BaseActivity> detailTagOpenClass() {
        return VerifySearchActivity.class;
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
            // 获取所有播放列表
            Elements playListElements = document.select("ul.anthology-list-play");
            List<DetailsDataBean.DramasItem> dramasItemList = new ArrayList<>();
            if (playListElements.size() > 0) {
                // 解析播放列表
                Elements playing = null;
                for (int i=0,size=playListElements.size(); i<size; i++) {
                    Elements playAList = playListElements.get(i).select("a");
                    for (Element drama : playAList) {
                        String watchUrl = drama.select("a").attr("href");
                        // 因为dramaStr第一个一定是最后播放的地址，根据这个地址判断上次播放是那个源！
                        if (dramaStr.startsWith(watchUrl)) {
                            playing = playAList;
                            break;
                        }
                    }
                }
                if (playing != null) {
                    int index = 0;
                    for (Element element : playing) {
                        String dramaTitle = element.text();
                        String dramaUrl = element.select("a").attr("href");
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
            String title = document.getElementsByTag("title").text();
            HomeTagEnum tagEnum = HomeTagEnum.DY;
            if (title.contains(HomeTagEnum.DY.name))
                tagEnum = HomeTagEnum.DY;
            else if (title.contains(HomeTagEnum.DSJ.name))
                tagEnum = HomeTagEnum.DSJ;
            else if (title.contains(HomeTagEnum.ZY.name))
                tagEnum = HomeTagEnum.ZY;
            else if (title.contains(HomeTagEnum.DM.name))
                tagEnum = HomeTagEnum.DM;
            else if (title.contains(HomeTagEnum.DJ.name))
                tagEnum = HomeTagEnum.DJ;
            else if (title.contains(HomeTagEnum.JJTY.name))
                tagEnum = HomeTagEnum.JJTY;
            else if (title.contains(HomeTagEnum.LL.name))
                tagEnum = HomeTagEnum.LL;
            else if (title.contains(HomeTagEnum.JLP.name))
                tagEnum = HomeTagEnum.JLP;
            Elements columns = document.select(".nav-swiper");
            int index = 0; // 约定 1:分类 2:类型 3:地区 4:年份 5:语言 6:字母 7:排序
            for (Element column : columns) {
                String filterTitle = column.select(".filter-text").text();
                if (filterTitle.contains("频道"))
                    continue;
                ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                classificationDataBean.setClassificationTitle(filterTitle);
                index += 1;
                switch (tagEnum) {
                    case DY:
                    case DSJ:
                    case ZY:
                    case DM:
                        // 拥有全部不处理
                    case DJ:
                        // 短剧
                        if (filterTitle.contains("类型"))
                            index = 2;
                        else if (filterTitle.contains("字母"))
                            index = 6;
                        break;
                    case JJTY:
                        // 体育竞技
                        if (filterTitle.contains("字母"))
                            index = 6;
                        break;
                    case LL:
                        // 伦理
                        if (filterTitle.contains("地区"))
                            index = 3;
                        else if (filterTitle.contains("年份"))
                            index = 4;
                        else if (filterTitle.contains("语言"))
                            index = 5;
                        else if (filterTitle.contains("字母"))
                            index = 6;
                        break;
                    case JLP:
                        // 纪录片
                        if (filterTitle.contains("类型"))
                            index = 2;
                        else if (filterTitle.contains("地区"))
                            index = 3;
                        else if (filterTitle.contains("字母"))
                            index = 6;
                        break;
                }
                classificationDataBean.setIndex(index);
                Elements liElements = column.select("a");
                List<ClassificationDataBean.Item> items = new ArrayList<>();
                for (Element li : liElements) {
                    String liTitle = li.text();
                    String href = liTitle;
                    if (filterTitle.contains("分类")) {
                        String url = li.attr("href");
                        String regex = "/(\\d+)-";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(url);
                        if (matcher.find()) {
                            href = matcher.group(1);
                        }
                    }
                    boolean isAll = liTitle.equals("全部");
                    items.add(new ClassificationDataBean.Item(liTitle, href.replace("全部", ""), isAll));
                }
                classificationDataBean.setItemList(items);
                classificationDataBeans.add(classificationDataBean);
            }
            ClassificationDataBean classificationDataBean = new ClassificationDataBean();
            classificationDataBean.setClassificationTitle("排序");
            classificationDataBean.setIndex(7);
            List<ClassificationDataBean.Item> items = new ArrayList<>();
            items.add(new ClassificationDataBean.Item("按最新", "time", true));
            items.add(new ClassificationDataBean.Item("按最热", "hits", false));
            items.add(new ClassificationDataBean.Item("按评分", "score", false));
            classificationDataBean.setItemList(items);
            classificationDataBeans.add(classificationDataBean);
            logInfo("分类列表信息", classificationDataBeans.toString());
            return classificationDataBeans;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserClassificationList error", e.getMessage());
        }
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
        return parserVodList(source);
    }

    @Override
    public Class<? extends BaseActivity> searchOpenClass() {
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
            boolean hasVerify = document.text().contains("请输入验证码");
            if (!hasVerify) {
                Elements videoList = document.select(".search-box");
                if (videoList.size() == 0)
                    return vodDataBeans;
                for (Element video : videoList) {
                    VodDataBean item = new VodDataBean();
                    String videoName = video.select(".thumb-txt").text(); // 标题
//                String dateTime = video.select("p.text-muted").text(); // r日期
//                Elements ribbonTop = video.select(".ribbon-top"); // 评分
                    item.setTitle(videoName);
                    item.setUrl(video.select("a.public-list-exp").attr("href"));
                    item.setImg(getImgPath(video.select("a.public-list-exp img").attr("data-src")));
                    item.setEpisodesTag(video.select("a.public-list-exp .public-list-prb").text());
                    item.setTopLeftTag(video.select(".thumb-director").text());
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
     * 详情TAG点击跳转视频列表 [其他影视列表]
     *
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserVodList(String source) {
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements vods = document.select("a.public-list-exp");
            if (vods.size() > 0) {
                for (Element vod : vods) {
                    VodDataBean bean = new VodDataBean();
                    String title = vod.attr("title"); // 标题
                    String updateInfo = vod.select("span.public-prt").text();
                    bean.setTitle(title);
                    bean.setUrl(vod.attr("href"));
                    bean.setImg(getImgPath(vod.select("img").attr("data-src")));
                    bean.setEpisodesTag(updateInfo);
                    items.add(bean);
                }
                logInfo("视频列表数据", items.toString());
            }
            return items;
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
        return 9;
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
        // params : 0、1:分类 2:类型 3:地区 4:年份 5:语言 6:字母 7:排序 8:分页
        // URL 格式为 1: 影视类型 2:地区 3:排序 4:类型 5:语言 6:字母 7:分页 8:年份
        String fl = Utils.isNullOrEmpty(params[1]) ? params[0] : params[1];
        String dq = Utils.isNullOrEmpty(params[3]) ? "" : params[3];
        String px = Utils.isNullOrEmpty(params[7]) ? "" : params[7];
        String lx = Utils.isNullOrEmpty(params[2]) ? "" : params[2];
        String yy = Utils.isNullOrEmpty(params[5]) ? "" : params[5];
        String zm = Utils.isNullOrEmpty(params[6]) ? "" : params[6];
        String fy = params[8];
        String nf = Utils.isNullOrEmpty(params[4]) ? "" : params[4];
        return getDefaultDomain() + String.format(SourceEnum.NYYY.getClassificationUrl(), fl, dq, px, lx, yy, zm, fy, nf);
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
        String url = getDefaultDomain() + String.format(SourceEnum.NYYY.getSearchUrl(), params[0], params[1]);
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
        try {
            String url = params[0];
            if (!url.startsWith("http"))
                url = getDefaultDomain() + url;
            String source = OkHttpUtils.getInstance().performSyncRequestAndHeader(url);
            // 提取ID
            Document doc = Jsoup.parse(source);
            Element element = doc.getElementById("blibliId");
            String id = element.attr("data-id");
            LogUtil.logInfo("影视id", id);
            if (Utils.isNullOrEmpty(id))
                return null;
            return getDefaultDomain() + String.format(SourceEnum.NYYY.getDanmuUrl(), id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 弹幕接口返回是否为JSON
     * <p>注：弹幕只有两种格式 XML/JsonObject</p>
     * <p>JSON弹幕需自行实现弹幕解析{@link DanmuModel#getDanmu}</p>
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
        try {
            List<DialogItemBean> result = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements scriptElements = document.select("script");
            for (Element script : scriptElements) {
                String html = script.html();
                if (html.contains("player_aaaa")) {
                    logInfo("javaScript", html);
                    String jsonText = html.substring(html.indexOf("{"), html.lastIndexOf("}") + 1);
                    JSONObject jsonObject = JSON.parseObject(jsonText);
                    String encryptUrl = jsonObject.getString("url").replace("\\/", "/");;
                    logInfo("播放地址加密字符串", encryptUrl);
                    String url = "";
                    switch (jsonObject.getInteger("encrypt")) {
                        case 1:
                            logInfo("encrypt为1 直接URL解码", "");
                            url = URLDecoder.decode(encryptUrl, StandardCharsets.UTF_8.toString());
                            logInfo("播放地址URL解码后 - >", url);
                            url = url.replaceAll("%", "\\\\");
                            logInfo("unicode转中文后", url);
                            url = unicodeDecode(url);
                            logInfo("最终播放地址", url);
//                            result.add(new DialogItemBean(url, url.contains("m3u8") ? M3U8 : MP4));
                            break;
                        case 2:
                            logInfo("encrypt为2 使用BASE64解码", "");
                            url = new String(Base64.decode(encryptUrl, Base64.DEFAULT));
                            logInfo("BASE64解码后", url);
                            try {
                                url = url.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                                logInfo("替换特殊字符后", url);
                                url = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
                                logInfo("播放地址URL解码后", url);
                                url = url.replaceAll("%", "\\\\");
                                logInfo("unicode转中文后", url);
                                url = unicodeDecode(url);
                                logInfo("最终播放地址", url);
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        default:
                            return null;
                    }
                    boolean isM3U8 = url.contains("m3u8");
                    if (isM3U8) {
                        if (isDownload)
                            result.add(new DialogItemBean(url, M3U8));
                        else {
                            // 尝试过滤里面的广告
                            String localPath = saveLocalM3U8Path(url);
                            if (!Utils.isNullOrEmpty(localPath)) {
                                boolean hasAd = VideoUtils.removeLocalM3U8Ad(localPath);
                                result.add(new DialogItemBean(hasAd ? "file:/" + localPath : url, M3U8));
                            }
                            else
                                result.add(new DialogItemBean(url, M3U8));
                        }
                    }
                    else
                        result.add(new DialogItemBean(url, MP4));
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("getPlayUrl error", e.getMessage());
        }
        return null;
    }

    /**
     * unicode转中文
     * @param string
     * @return
     */
    public static String unicodeDecode(String string) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            string = string.replace(matcher.group(1), ch + "");
        }
        return string;
    }

    private String saveLocalM3U8Path(String url) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // 建立 HTTP 连接
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Referer", getDefaultDomain() + "/");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setDoInput(true);
            // 检查HTTP响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // 读取M3U8文件内容
            StringBuilder m3u8Content = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                m3u8Content.append(line).append("\n"); // 拼接每行内容并换行
            }

            // 将M3U8内容转为byte[]，用于保存
            byte[] processedData = m3u8Content.toString().getBytes(StandardCharsets.UTF_8);
            return Utils.writeToFile(processedData, (UUID.randomUUID().toString()) + ".m3u8");
        }  catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            // 关闭连接和流
            // 关闭连接和流
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
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

    /**
     * 首页TAG枚举
     */
    @Getter
    @AllArgsConstructor
    public enum HomeTagEnum {
        DY("电影", "1"),
        DSJ("电视剧", "2"),
        ZY("综艺", "3"),
        DM("动漫", "4"),
        JJTY("竞技体育", "5"),
        DJ("短剧", "50"),
        LL("伦理", "8"),
        JLP("纪录片", "29")
        ;

        private String name;
        private String content;
    }

    private String getImgPath(String path) {
        if (Utils.isNullOrEmpty(path))
            return "";
        return path.startsWith("http") ? path : getDefaultDomain() + path;
    }
}
