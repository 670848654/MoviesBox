package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;
import static my.project.moviesbox.parser.config.MultiItemEnum.BANNER_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.TAG_LIST;
import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
import my.project.moviesbox.view.TopticListActivity;
import my.project.moviesbox.view.VodListActivity;
import my.project.moviesbox.view.WeekActivity;
import my.project.moviesbox.view.base.BaseActivity;
import my.project.moviesbox.view.fragment.HomeFragment;
import okhttp3.FormBody;

/**
  * @包名: my.project.moviesbox.parser.impl
  * @类名: GiriGiriLoveImpl
  * @描述: ギリギリ愛站点解析实现
  * @作者: Li Z
  * @日期: 2024/1/23 16:38
  * @版本: 1.0
 */
public class GiriGiriLoveImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.GIRI_GIRI_LOVE.getPostRequestMethod();
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
        return SourceEnum.GIRI_GIRI_LOVE.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#GIRI_GIRI_LOVE
     */
    @Override
    public int getSource() {
        return SourceEnum.SourceIndexEnum.GIRI_GIRI_LOVE.index;
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

    @Override
    public HashMap<String, String> setPlayerHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accept", "*/*");
        headers.put("accept-encoding", "gzip, deflate, br, zstd");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("connection", "keep-alive");
        headers.put("host", "love.girigirilove.com");
        headers.put("origin", "https://m3u8.girigirilove.com");
        headers.put("sec-ch-ua", "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"Microsoft Edge\";v=\"138\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-site");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0");
        return headers;
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
        Document doc = Jsoup.parse(source);
        Element pageTip = doc.selectFirst("div.page-tip");

        if (pageTip != null) {
            String text = pageTip.text();  // 获取文本内容：共2678条数据,当前1/56页
            Pattern pattern = Pattern.compile("当前\\d+/(\\d+)页");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String totalPage = matcher.group(1);
                return Integer.parseInt(totalPage);
            } else {
                return startPageNum();
            }
        } else {
            return startPageNum();
        }
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
            tags.add(new MainDataBean.Tag(HomeTagEnum.RF.name, HomeTagEnum.RF.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.MF.name, HomeTagEnum.MF.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.JCB.name, HomeTagEnum.JCB.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZRFJ.name, HomeTagEnum.ZRFJ.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.BD.name, HomeTagEnum.BD.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.QT.name, HomeTagEnum.QT.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZT.name, HomeTagEnum.ZT.content, TopticListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZJGX.name, HomeTagEnum.ZJGX.content, VodListActivity.class));
            mainDataBean.setTags(tags);
            mainDataBeans.add(mainDataBean);
            // banner内容解析
            Elements bannerEle = document.select("div.slid-e-list > .slid-e-list-box");
            if (bannerEle.size() == 0)
                return null;
            mainDataBean = new MainDataBean();
            mainDataBean.setTitle("动漫推荐");
            mainDataBean.setHasMore(false);
            mainDataBean.setDataType(BANNER_LIST.getType());
            mainDataBean.setVodItemType(STYLE_16_9);
            List<MainDataBean.Item> bannerItems = new ArrayList<>();
            for (Element element : bannerEle) {
                MainDataBean.Item item = new MainDataBean.Item();
                item.setTitle(element.select("h3.slide-info-title").text());
                // 提取图片URL（从 style 属性中解析）
                Element imgDiv = element.selectFirst("div[style^=background-image]");
                String imageUrl;
                if (imgDiv != null) {
                    String style = imgDiv.attr("style");  // e.g. background-image: url(/upload/vod/xxx.jpg);
                    imageUrl = style.replaceAll(".*url\\(['\"]?(.*?)['\"]?\\).*", "$1");
                    item.setImg(getImg(imageUrl));
                } else {
                    // 2025年9月20日19:50:06 新版获取图片
                    Element el = element.selectFirst(".swiper-lazy.slid-e-bj");
                    if (el != null) {
                        imageUrl = el.attr("data-background");
                        item.setImg(getImg(imageUrl));
                    }
                }
                // 提取“影片详情”的 href 链接
                Element detailLink = element.selectFirst("a:contains(影片详情)");
                String detailHref = detailLink != null ? detailLink.attr("href") : null;
                item.setUrl(detailHref);
                item.setEpisodes(element.select(".slide-info").text());
                bannerItems.add(item);
            }
            mainDataBean.setItems(bannerItems);
            mainDataBeans.add(mainDataBean);
            // 日番、美番
            Elements boxs = document.select(".box-width.wow.fadeInUp");
            for (Element box : boxs) {
                String boxTitle = box.select("h4.title-h").text();
                if (Utils.isNullOrEmpty(boxTitle) || boxTitle.contains("本周推荐") || boxTitle.contains("每周推荐") || boxTitle.contains("周期表"))
                    continue;
                mainDataBean = new MainDataBean();
                mainDataBean.setTitle(boxTitle);
                mainDataBean.setHasMore(!boxTitle.contains("最近大家在看"));
                // 匹配 /show/ 后面的第一个数字（不管后面多少个 -）
                String moreNumber = box.select("div.title-right > a").attr("href").replaceAll(".*/show/(\\d+)-.*", "$1");
                mainDataBean.setMore(moreNumber);
                mainDataBean.setDataType(ITEM_LIST.getType());
                List<MainDataBean.Item> items = new ArrayList<>();
                Elements boxList = box.select(".public-list-box");
                for (Element item : boxList) {
                    MainDataBean.Item homeItemBean = new MainDataBean.Item();
                    String title = item.select("a.time-title").text();
                    String url = item.select("a.time-title").attr("href");
                    String img = getImg(item.select("img").attr("data-src"));
                    String episodes = item.select("span.public-list-prb").text();
                    String topLeftTag = item.select("span.public-prt").text();
                    homeItemBean.setTitle(title);
                    homeItemBean.setUrl(url);
                    homeItemBean.setImg(img);
                    homeItemBean.setEpisodes(episodes);
                    homeItemBean.setTopLeftTag(topLeftTag);
                    items.add(homeItemBean);
                }
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
            String title = document.select("h3.slide-info-title").text();
            String img = getImg(document.select("div.detail-pic > img").attr("data-src"));
            detailsDataBean.setTitle(title);
            //番剧图片
            detailsDataBean.setImg(img);
            //番剧地址
            detailsDataBean.setUrl(url);
            // tag
            Elements tags = document.select("div.slide-info > a");
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tags) {
                if (tag.text().isEmpty()) continue;
                tagTitles.add(tag.text());
                tagUrls.add(tag.attr("href"));
            }
            Elements tags2 = document.select("div.vod-tag > a");
            for (Element tag : tags2) {
                if (tag.text().isEmpty()) continue;
                tagTitles.add(tag.text());
                tagUrls.add(tag.text().replaceAll("#", ""));
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);

            /*Elements span = document.select("p.data span.text-muted");
            for (Element s : span) {
                if (s.text().contains("更新：")) {
                        detailsDataBean.setUpdateTime(s.parent().text());
                    break;
                }
            }*/
            String playScore = document.select("div.play-score").select(".text-site").text() + " " + document.select("div.play-score").select(".fraction").text();
            detailsDataBean.setScore(playScore);
            Element introductionElement = document.getElementById("height_limit");
            detailsDataBean.setIntroduction(Utils.isNullOrEmpty(introductionElement) ? "" : introductionElement.text());
            // 获取所有播放列表
            Elements playTitleList = document.select("div.anthology").select(".swiper-wrapper").select("a");
            playTitleList.select("span.badge").remove();
            Elements playList = document.select("div.anthology").select(".anthology-list-play");
            if (playTitleList.size() > 0) {
                // 解析播放列表
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                for (int i = 0; i < playTitleList.size(); i++) {
                    DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                    String playListName = playTitleList.get(i).text();
                    dramas.setListTitle(playListName);
                    List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
                    int index= 0;
                    for (Element drama : playList.get(i).select("li")) {
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
                // 解析剧集相关多季 该网点未知
                // 解析推荐列表
                Elements recommendElements = document.select("div.public-pic-b"); //相关推荐
                if (recommendElements.size() > 0) {
                    List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
                    for (Element item : recommendElements) {
                        String recommendTitle = item.select("a.time-title").text();
                        String recommendUrl = item.select("a.time-title").attr("href");
                        String recommendImg = getImg(item.select("img").attr("data-src"));
                        String recommendEpisodes = item.select("span.public-list-prb").text();
                        String topLeftTag = item.select("span.public-prt").text();
                        recommendList.add(new DetailsDataBean.Recommend(recommendTitle, recommendImg, recommendUrl, recommendEpisodes, topLeftTag));
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

    @Override
    public Class<? extends BaseActivity> detailTagOpenClass() {
        return VodListActivity.class;
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
            Elements playListElements = document.select(".anthology-list-play");
            // 获取所有播放列表
            List<DetailsDataBean.DramasItem> dramasItemList = new ArrayList<>();
            if (playListElements.size() > 0) {
                // 解析播放列表
                for (int i=0,size=playListElements.size(); i<size; i++) {
                    Elements playing = null;
                    Elements liList = playListElements.get(i).select("li");
                    for (Element drama : liList) {
                        String watchUrl = drama.select("a").attr("href");
                        // 因为dramaStr第一个一定是最后播放的地址，根据这个地址判断上次播放是那个源！
                        if (dramaStr.startsWith(watchUrl)) {
                            playing = liList;
                            break;
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
            }
            logInfo("播放列表信息", dramasItemList.toString());
            return dramasItemList;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserNowSourcesDramas", e.getMessage());
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
        try {
            Document document = Jsoup.parse(source);
            List<ClassificationDataBean> classificationDataBeans = new ArrayList<>();
            Elements navs = document.select(".ec-casc-list > .top20 > .nav-swiper");
            if (navs.size() > 0) {
                Element firstNav = navs.get(0);
                // 查看当前已选
                String selectedNav = firstNav.select("li.swiper-slide > a").text();
                // 删除前两个Nav
                navs.remove(0);
                navs.remove(0);
                /**
                 * 固定格式
                 * [0] 频道
                 * [1] 类型
                 * [2] 季度
                 * [3] 年份
                 * [4] 语言
                 * [5] 类别
                 * [6] 改编
                 * [7] 排序
                 * [8] 分页
                 */
                for (Element column : navs) {
                    String filterTitle = column.select(".filter-text").text();
                    ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                    classificationDataBean.setClassificationTitle(filterTitle);
                    if (filterTitle.contains("类型")|| filterTitle.contains("類型")) {
                        classificationDataBean.setIndex(1);
                    } else if (filterTitle.contains("季度")) {
                        classificationDataBean.setIndex(3);
                    } else if (filterTitle.contains("年份")) {
                        classificationDataBean.setIndex(3);
                    } else if (filterTitle.contains("语言")|| filterTitle.contains("語言")) {
                        classificationDataBean.setIndex(4);
                    } else if (filterTitle.contains("类别")|| filterTitle.contains("類别")) {
                        classificationDataBean.setIndex(5);
                    } else if (filterTitle.contains("改编")|| filterTitle.contains("改編")) {
                        classificationDataBean.setIndex(6);
                    }
                    Elements aElements = column.select("a");
                    List<ClassificationDataBean.Item> items = new ArrayList<>();
                    for (Element a : aElements) {
                        String liTitle = a.text();
                        if (liTitle.contains("注意"))
                            continue;
                        String href = filterTitle.contains("类别")|| filterTitle.contains("類别") ?  "/version/"+a.text()+"/" : a.text();
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
                items.add(new ClassificationDataBean.Item("最新", "time", true));
                items.add(new ClassificationDataBean.Item("最热", "hits", false));
                items.add(new ClassificationDataBean.Item("评分", "score", false));
                classificationDataBean.setItemList(items);
                classificationDataBeans.add(classificationDataBean);
                logInfo("分类列表信息", classificationDataBeans.toString());
                return classificationDataBeans;
            }
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
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select("div.public-pic-b");
            if (elements.size() > 0) {
                for (int i = 0, size = elements.size(); i < size; i++) {
                    VodDataBean item = new VodDataBean();
                    item.setTitle(elements.get(i).select("a.time-title").text());
                    item.setUrl(elements.get(i).select("a.time-title").attr("href"));
                    item.setImg(getImg(elements.get(i).select("img").attr("data-src")));
                    item.setEpisodesTag(elements.get(i).select("span.public-list-prb").text());
                    item.setTopLeftTag(elements.get(i).select("span.public-prt").text());
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
            Elements elements = document.select("div.search-list");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    String title = item.select("h3.slide-info-title").text();
                    bean.setTitle(title);
                    bean.setUrl(item.select("div.detail-info > a").attr("href"));
                    bean.setImg(getImg(item.select("img").attr("data-src")));
                    bean.setEpisodesTag(item.select("span.slide-info-remarks").text());
//                    bean.setTopLeftTag(item.select("span.public-prt").text());
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
            List<VodDataBean> items = new ArrayList<>();
            Elements elements = document.select("div.public-pic-b");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.select("a.time-title").text());
                    bean.setUrl(item.select("a.time-title").attr("href"));
                    bean.setImg(getImg(item.select("img").attr("data-src")));
                    bean.setEpisodesTag(item.select("span.public-list-prb").text());
                    bean.setTopLeftTag(item.select("span.public-prt").text());
                    items.add(bean);
                }
                logInfo("搜索列表数据", items.toString());
                return items;
            } else
                // 有可能是从详情界面TAG过来的，这里就需要走搜索页面解析方法
                return parserSearchVodList(source);
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
        /**
         * 固定格式
         * [0] 频道
         * [1] 类型
         * [2] 季度
         * [3] 年份
         * [4] 语言
         * [5] 类别
         * [6] 改编
         * [7] 排序
         * [8] 分页
         */
        String pd = Utils.isNullOrEmpty(params[0]) ? "" : params[0];
        String lx = Utils.isNullOrEmpty(params[1]) ? "" : params[1];
        String jd = Utils.isNullOrEmpty(params[2]) ? "" : params[2];
        String nf = Utils.isNullOrEmpty(params[3]) ? "" : params[3];
        String yy = Utils.isNullOrEmpty(params[4]) ? "" : params[4];
        String lb = Utils.isNullOrEmpty(params[5]) ? "" : params[5];
        String gb = Utils.isNullOrEmpty(params[6]) ? "" : params[6];
        String px = Utils.isNullOrEmpty(params[7]) ? "" : params[7];
        String page = Utils.isNullOrEmpty(params[8]) ? "" : params[8];
        // 频道.季度.排序.类型.语言.分页.改编.年份.类别
        LogUtil.logInfo("getClassificationUrl", getDefaultDomain() + String.format(SourceEnum.GIRI_GIRI_LOVE.getClassificationUrl(), pd, jd, px, lx, yy, page, gb, nf, lb));
        return getDefaultDomain() + String.format(SourceEnum.GIRI_GIRI_LOVE.getClassificationUrl(), pd, jd, px, lx, yy, page, gb, nf, lb);
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
        return getDefaultDomain() + String.format(SourceEnum.GIRI_GIRI_LOVE.getSearchUrl(), params[0], params[1]);
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
        if (url.contains("map")) {
            // 排行榜
            return getDefaultDomain() + url;
        } else if (url.contains("search")) {
            return getDefaultDomain() + insertBeforeThirdLastDash(url, String.valueOf(page));
        }
        return null;
    }

    public static String insertBeforeThirdLastDash(String url, String insertStr) {
        int count = 0;
        int index = url.length();
        List<Integer> dashPositions = new ArrayList<>();
        while (count < 3 && index > 0) {
            index = url.lastIndexOf('-', index - 1);
            if (index == -1) break;
            dashPositions.add(index);
            count++;
        }
        if (index == -1) {
            return url;  // 不足3个'-'，返回原串
        }
        return url.substring(0, index) + insertStr + url.substring(index);
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
        // 该站点使用的post 请求 参数为播放地址
        return SourceEnum.GIRI_GIRI_LOVE.getDanmuUrl() + "," + params[0];
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
                            url = new String(android.util.Base64.decode(encryptUrl, android.util.Base64.DEFAULT));
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
                    result.add(new DialogItemBean(url, url.contains("m3u8") ? M3U8 : MP4));
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
     * @return {@link List<WeekDataBean>}
     */
    @Override
    public List<WeekDataBean> parserWeekDataList(String source) {
        try {
            List<WeekDataBean> weekDataBeans = new ArrayList<>();
            Document document = Jsoup.parse(source);
            for (int i=0,size=WeekEnum.values().length; i<size; i++) {
                Elements weekLi = document.getElementById("week-module-"+(i+1)).select(".public-list-box");
                int week = WeekEnum.values()[i].getIndex();
                List<WeekDataBean.WeekItem> weekItems = new ArrayList<>();
                for (Element item : weekLi) {
                    String title = item.select("a.time-title").text();
                    String url = item.select("a.time-title").attr("href");
                    String img = getImg(item.select("img").attr("data-src"));
                    String episodes = item.select("span.public-list-prb").text();
                    String topLeftTag = item.select("span.public-prt").text();
                    weekItems.add(new WeekDataBean.WeekItem(
                            title,
                            img,
                            url,
                            episodes,
                            "",
                            topLeftTag
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
            Document document = Jsoup.parse(source);
            Elements elements = document.select("div.public-list-box");
            if (elements.size() > 0) {
                List<VodDataBean> items = new ArrayList<>();
                for (Element element : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setVodItemStyleType(VodItemStyleEnum.STYLE_16_9.getType());
                    bean.setTitle(element.select(".title-bottom").text());
                    bean.setUrl(element.select("a.public-list-exp").attr("href"));
                    bean.setImg(getImg(element.select("img").attr("data-src")));
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
        return parserSearchVodList(source);
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
        return null;
    }

    // -------------------------------- 该解析通用方法 --------------------------------
    /**
     * 处理图片
     * @param imgUrl
     * @return
     */
    private String getImg(String imgUrl) {
        return imgUrl.startsWith("http") ? imgUrl : getDefaultDomain() + imgUrl;
    }

    /**
     * 首页TAG枚举
     */
    @Getter
    @AllArgsConstructor
    public enum HomeTagEnum {
        WEEK("番剧时间表", "%s"),
        RF("日番", "2"),
        MF("美番", "3"),
        JCB("剧场版", "21"),
        ZRFJ("真人番剧", "20"),
        BD("BD副音轨", "24"),
        QT("演唱会&周边活动&其他", "26"),
        ZT("专题", "/topic"),
        ZJGX("最近更新", "/map")
        ;
        private String name;
        private String content;
    }

    @Override
    public DomainDataBean parserDomain(String source) {
        try {
            Document document = Jsoup.parse(source);
            List<DomainDataBean.Domain> domainList = new ArrayList<>();
            String dataInfo = document.select("a.uePcBigButton").attr("href");
            if (Utils.isNullOrEmpty(dataInfo))
                return new DomainDataBean().error("未能正确获取到最新域名数据，请自行通过发布页查看");
            else {
                domainList.add(new DomainDataBean.Domain("最新域名", dataInfo));
                return new DomainDataBean().success(domainList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new DomainDataBean().error("获取最新域名失败："+e.getMessage());
        }
    }
}
