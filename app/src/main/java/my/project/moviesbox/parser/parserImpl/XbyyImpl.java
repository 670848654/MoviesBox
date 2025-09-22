package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;
import static my.project.moviesbox.parser.config.MultiItemEnum.BANNER_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.TAG_LIST;
import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.XBYY;
import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.utils.VideoUtils;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.fragment.HomeFragment;
import okhttp3.FormBody;

/**
 * @author Li
 * @version 1.0
 * @description: 小宝影院站点解析实现
 * @date 2024/9/30 15:45
 */
public class XbyyImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.XBYY.getPostRequestMethod();
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
        return SourceEnum.XBYY.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#XBYY
     */
    @Override
    public int getSource() {
        return XBYY.index;
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
     * 播放时请求头部
     * 某些网站播放视频需要添加请求头部
     *
     * @return
     */
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

    /**
     * 站点影视列表获取分页总数
     *
     * @param source 网页源代码
     * @return 获取失败或只有一页时应返回 {@link #startPageNum}
     */
    @Override
    public int parserPageCount(String source) {
        int lastPageNumber = startPageNum();
        try {
            Document document = Jsoup.parse(source);
            Elements pageLinks = document.select("ul.myui-page li a");
            for (Element link : pageLinks) {
                if (link.text().equals("尾页")) {
                    String href = link.attr("href");
                    int startIndex = href.indexOf("page/") + 5;
                    int endIndex = href.indexOf("/", startIndex);
                    String value = endIndex != -1 ? href.substring(startIndex, endIndex) : href.substring(startIndex);
                    lastPageNumber = Integer.parseInt(value.replace(".html", ""));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastPageNumber;
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
            tags.add(new MainDataBean.Tag(HomeTagEnum.DM.name, HomeTagEnum.DM.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZY.name, HomeTagEnum.ZY.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.JLP.name, HomeTagEnum.JLP.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DJ.name, HomeTagEnum.DJ.content, ClassificationVodListActivity.class));
            tagBean.setTags(tags);
            mainDataBeans.add(tagBean);
            /*************************** 解析banner内容开始 ***************************/
            Elements bannerList = document.select(".carousel-inner .item.text-center a");
            List<MainDataBean.Item> bannerItems = new ArrayList<>();
            MainDataBean bannerBean = new MainDataBean();
            bannerBean.setDataType(BANNER_LIST.getType());
            bannerBean.setVodItemType(STYLE_16_9);
            for (Element a : bannerList) {
                MainDataBean.Item item = new MainDataBean.Item();
                // 名称
                item.setTitle(a.attr("title"));
                // 地址
                item.setUrl(a.attr("href"));
                // 图片
                item.setImg(getImgPath(a.select("img").attr("src")));
                bannerItems.add(item);
            }
            bannerBean.setItems(bannerItems);
            mainDataBeans.add(bannerBean);
            /*************************** 解析banner内容结束 ***************************/
            /*************************** 解析list内容开始 ***************************/
            Elements panels = document.select(".myui-panel.myui-panel-bg.hiddex-xs");
            if (panels.size() == 0)
                return null;
            for (Element panel : panels) {
                panel.remove();
            }
            Elements rowList = document.select(".myui-panel.myui-panel-bg");
            Elements headers = rowList.select(".col-lg-wide-75 .myui-panel__head");
            Elements vodItems = rowList.select(".col-lg-wide-75 .myui-vodlist");
            for (int i=0,size=headers.size(); i<size; i++) {
                Element header = headers.get(i);
                String headerTitle = header.select("h3").text();
                Elements moreElements = header.select("a.more");
                MainDataBean contentBean = new MainDataBean();
                contentBean.setDataType(ITEM_LIST.getType());
                contentBean.setTitle(headerTitle);
                if (moreElements.size() > 0) {
                    // 更多跳转地址
                    contentBean.setHasMore(true);
                    contentBean.setMore(moreElements.attr("href"));
                }
                Elements vods = vodItems.get(i).select("li .myui-vodlist__box");
                List<MainDataBean.Item> vodItemList = new ArrayList<>();
                for (Element vod : vods) {
                    MainDataBean.Item item = new MainDataBean.Item();
                    String title = vod.select("h4").text(); // 标题
                    Elements textRight = vod.select("a .pic-text.text-right");
                    item.setTitle(title);
                    item.setUrl(vod.select("a").attr("href"));
                    item.setImg(getImgPath(vod.select("a").attr("data-original")));
                    item.setEpisodes(textRight.text());
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
            String title = document.select(".myui-content__detail h1").text();
            detailsDataBean.setTitle(title);
            // 影视图片
            String img = document.select("a.myui-vodlist__thumb.picture img").attr("data-original");
            detailsDataBean.setImg(getImgPath(img));
            //影视地址
            detailsDataBean.setUrl(url);
            Elements infoElements = document.select("p.data.hidden-sm");
            String[] infoArr = infoElements.text().split("/");
            if (infoArr.length > 1) {
                detailsDataBean.setUpdateTime(infoArr[0]);
                detailsDataBean.setScore(infoArr[1]);
            } else
                detailsDataBean.setUpdateTime(infoArr[0]);
            Elements contentElements1 = document.select(".col-pd.text-collapse.content span.sketch.content");
            Elements contentElements2 = document.select(".col-pd.text-collapse.content span.data");
            detailsDataBean.setIntroduction(Utils.isNullOrEmpty(contentElements2) ? contentElements2.text() : contentElements1.text());
            Elements tagList = document.select("p.data a");
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tagList) {
                String tagTitle = tag.text();
                if (!tagTitle.isEmpty()) {
                    tagTitles.add(tag.text());
                    tagUrls.add(tag.attr("href"));
                }
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);
            // 解析播放列表
            Elements playListTitleElements = document.select(".nav.nav-tabs.active li a");
            Elements playListElements = document.select("ul.myui-content__list.scrollbar.sort-list");
            if (playListElements.size() > 0) {
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                for (int i=0,size=playListElements.size(); i<size; i++) {
                    Elements dramaElements = playListElements.get(i).select("li a");
                    DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                    dramas.setListTitle(playListTitleElements.get(i).text());
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
            Elements recommendElements = new Elements();
            recommendElements.add(document.getElementById("actor"));
            recommendElements.add(document.getElementById("year"));
            recommendElements.add(document.getElementById("type"));
            recommendElements.size();
            List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
            for (Element recommend : recommendElements.select("li .myui-vodlist__box")) {
                String recommendTitle = recommend.select("h4").text();
                String recommendImg = recommend.select("a").attr("data-original");
                String recommendUrl = recommend.select("a").attr("href");
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

            Elements dataElement = document.select(".tab-content .tab-pane");
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
                        String dramaTitle = element.text();
                        String dramaUrl = element.attr("href");
                        dramasItemList.add(new DetailsDataBean.DramasItem(index++, dramaTitle, dramaUrl, false));
                    }
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
            List<ClassificationDataBean> classificationDataBeans = new ArrayList<>();
            String title = document.getElementsByTag("title").text();
            HomeTagEnum tagEnum = HomeTagEnum.DY;
            if (title.contains(HomeTagEnum.DY.name))
                tagEnum = HomeTagEnum.DY;
            else if (title.contains(HomeTagEnum.DSJ.name))
                tagEnum = HomeTagEnum.DSJ;
            else if (title.contains(HomeTagEnum.DM.name))
                tagEnum = HomeTagEnum.DM;
            else if (title.contains(HomeTagEnum.ZY.name))
                tagEnum = HomeTagEnum.ZY;
            else if (title.contains(HomeTagEnum.JLP.name))
                tagEnum = HomeTagEnum.JLP;
            else if (title.contains(HomeTagEnum.DJ.name))
                tagEnum = HomeTagEnum.DJ;
            Elements columns = document.select(".myui-panel-bg2 .myui-panel-box .myui-panel_bd ul");
            int index = -1; // 约定 0：类型 1：分类 2：地区 3：年份 4：语言 5：排序 6：分页
            for (Element column : columns) {
                Element firstElement = column.select("li").first();
                ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                String classificationTitle = firstElement.text();
                classificationDataBean.setClassificationTitle(classificationTitle);
                index += 1;
                switch (tagEnum) {
                    case DY:
                        // 电影没有分类 因此当为地区时
                        if (classificationTitle.contains("地区"))
                            index = 2;
                        break;
                    case DSJ:
                    case DM:
                    case ZY:
                        // 电视剧/动漫/综艺没有地区 因此当为年份时+1
                        if (classificationTitle.contains("年份"))
                            index = 3;
                        break;
                    case JLP:
                        // 纪录片没有类型
                        if (classificationTitle.contains("地区"))
                            index = 2;
                        else if (classificationTitle.contains("年份"))
                            index = 3;
                        else if (classificationTitle.contains("语言"))
                            index = 4;
                        else if (classificationTitle.contains("排序"))
                            index = 5;
                        break;
                    case DJ:
                        if (classificationTitle.contains("地区"))
                            index = 2;
                        else if (classificationTitle.contains("排序"))
                            index = 5;
                        break;
                }
                classificationDataBean.setIndex(index);
                Elements liElements = column.select("li");
                List<ClassificationDataBean.Item> items = new ArrayList<>();
                for (Element li : liElements) {
                    String liTitle = li.text();
                    String liHref = li.select("a").attr("href");
                    boolean isAll = liTitle.equals("全部") || liTitle.equals("时间");
                    if (liHref.isEmpty())
                        continue;
                    switch (classificationTitle) {
                        case "类型":
                            items.add(new ClassificationDataBean.Item(liTitle, getClassificationContent(liHref, isAll, "id/") , isAll));
                            break;
                        case "分类":
                            items.add(new ClassificationDataBean.Item(liTitle, getClassificationContent(liHref, isAll, "class/") , isAll));
                            break;
                        case "地区":
                            items.add(new ClassificationDataBean.Item(liTitle, getClassificationContent(liHref, isAll, "area/") , isAll));
                            break;
                        case "年份":
                            items.add(new ClassificationDataBean.Item(liTitle, getClassificationContent(liHref, isAll, "year/") , isAll));
                            break;
                        case "语言":
                            items.add(new ClassificationDataBean.Item(liTitle, getClassificationContent(liHref, isAll, "lang/") , isAll));
                            break;
                        case "排序":
                            items.add(new ClassificationDataBean.Item(liTitle, getClassificationContent(liHref, isAll, "by/") , isAll));
                            break;
                    }
                }
                classificationDataBean.setItemList(items);
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

    private String getClassificationContent(String url, boolean isAll, String classification) {
        if (isAll && !classification.contains("id"))
            return "";
         int startIndex = url.indexOf(classification) + classification.length(); // 6是"class/"的长度
         int endIndex = url.indexOf("/", startIndex);
         String value = endIndex != -1 ? url.substring(startIndex, endIndex) : url.substring(startIndex);
         value = value.replaceAll(".html", "");
         return classification.contains("id") ? value : "/" + classification + value;
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

    /**
     * 是否能搜索
     *
     * @return
     */
    @Override
    public Class searchOpenClass() {
        return ParserInterface.super.searchOpenClass();
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
            Element searchUl = document.getElementById("searchList");
            if (Utils.isNullOrEmpty(searchUl))
                return null;
            Elements lis = searchUl.select("li");
            if (lis.size() > 0) {
                for (Element li : lis) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(li.select("h4").text());
                    bean.setUrl(li.select(".thumb a").attr("href"));
                    bean.setImg(getImgPath(li.select(".thumb a").attr("data-original")));
                    bean.setTopLeftTag(li.select(".pic-tag-top").text());
                    bean.setEpisodesTag(li.select(".text-right").text());
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
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);

            Elements elements = document.select(".myui-vodlist__box");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.select("h4").text());
                    bean.setUrl(item.select("a.myui-vodlist__thumb").attr("href"));
                    bean.setImg(getImgPath(item.select("a.myui-vodlist__thumb").attr("data-original")));
                    bean.setTopLeftTag(item.select(".pic-tag-top").text());
                    bean.setEpisodesTag(item.select(".text-right").text());
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
        String lx = params[0];
        if (lx.contains("id")) {
            lx = getClassificationContent(lx, false, "id/");
        }
        String fl = Utils.isNullOrEmpty(params[1]) ? "" : params[1];
        String dq = Utils.isNullOrEmpty(params[2]) ? "" : params[2];
        String nf = Utils.isNullOrEmpty(params[3]) ? "" : params[3];
        String yy = Utils.isNullOrEmpty(params[4]) ? "" : params[4];
        String px = Utils.isNullOrEmpty(params[5]) ? "" : params[5];
        String page = Utils.isNullOrEmpty(params[6]) ? "" : "/page/"+params[6];
        LogUtil.logInfo("getClassificationUrl", getDefaultDomain() + String.format(SourceEnum.XBYY.getClassificationUrl(), lx, fl+dq+nf+yy+px+page));
        return getDefaultDomain() + String.format(SourceEnum.XBYY.getClassificationUrl(), lx, fl+dq+nf+yy+px+page);
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
        return getDefaultDomain() + String.format(SourceEnum.XBYY.getSearchUrl(), params[1], params[0]);
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
        return addPageToUrl(url, page);
    }

    private String addPageToUrl(String url, int pageNumber) {
        url = url.startsWith("http") ? url : getDefaultDomain() + url;
        // 找到 ".html" 前的位置
        int index = url.lastIndexOf(".html");
        if (index == -1) {
            // 如果没有找到 ".html"，直接返回原始 URL
            return url;
        }

        // 在 ".html" 前插入 "/page/{pageNumber}"
        return url.substring(0, index) + "/page/" + pageNumber + ".html";
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
     * 播放地址是否需要解析
     * 默认为true 需要解析，有些网站可能不需要解析
     *
     * @return
     */
    @Override
    public boolean playUrlNeedParser() {
        return ParserInterface.super.playUrlNeedParser();
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
                    String url = jsonObject.getString("url");
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
     * 时间列表一行显示几个内容
     *
     * @param isPad      是否为平板
     * @param isPortrait 是否为竖屏
     * @return 返回不能为0！！！ 需自己实现 平板、手机横竖屏显示数量
     */
    @Override
    public int setWeekItemListItemSize(boolean isPad, boolean isPortrait) {
        return ParserInterface.super.setWeekItemListItemSize(isPad, isPortrait);
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
        DY("电影", "7"),
        DSJ("电视剧", "6"),
        DM("动漫", "5"),
        ZY("综艺", "3"),
        JLP("纪录片", "21"),
        DJ("短剧", "64");
        private String name;
        private String content;
    }

    private String getImgPath(String path) {
        return path.startsWith("http") ? path : getDefaultDomain() + path;
    }
}
