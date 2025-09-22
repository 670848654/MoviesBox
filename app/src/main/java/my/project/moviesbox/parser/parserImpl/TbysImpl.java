package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;
import static my.project.moviesbox.parser.config.MultiItemEnum.BANNER_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.TBYS;
import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;

import android.util.Base64;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.PlayerActivity;
import my.project.moviesbox.view.fragment.HomeFragment;
import okhttp3.FormBody;

/**
  * @包名: my.project.moviesbox.parser.impl
  * @类名: TbysImpl
  * @描述: 拖布影视站点解析实现
  * @作者: Li Z
  * @日期: 2024/1/23 16:39
  * @版本: 1.0
 */
public class TbysImpl implements ParserInterface {

    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.TBYS.getPostRequestMethod();
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
        return SourceEnum.TBYS.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#TBYS
     */
    @Override
    public int getSource() {
        return TBYS.index;
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
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", getDefaultDomain() + "/");
        headers.put("Sec-Ch-Ua", "\"Not(A:Brand\";v=\"99\", \"Microsoft Edge\";v=\"133\", \"Chromium\";v=\"133\"");
        headers.put("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 Edg/133.0.0.0");
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
        Document document = Jsoup.parse(source);
        String pageContent = document.select(".pagination").text();
        Matcher matcher = PAGE_PATTERN.matcher(pageContent);
        if (matcher.find()) {
            String totalPageString = matcher.group(1);
            try {
                return Integer.parseInt(totalPageString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
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
            // 该源没有头部TAG列表
            /*************************** 解析banner内容开始 ***************************/
            Elements bannerList = document.select(".swiper-wrapper > .swiper-slide");
            List<MainDataBean.Item> bannerItems = new ArrayList<>();
            MainDataBean bannerBean = new MainDataBean();
            bannerBean.setTitle("影视推荐");
            bannerBean.setHasMore(false);
            bannerBean.setDataType(BANNER_LIST.getType());
            bannerBean.setVodItemType(STYLE_16_9);
            for (Element element : bannerList) {
                MainDataBean.Item item = new MainDataBean.Item();
                Element a = element.getElementsByTag("a").get(0);
                // 名称
                item.setTitle(a.attr("title"));
                // 地址
                item.setUrl(a.attr("href"));
                // 图片
                item.setImg(getImageUrl(a.select("img").attr("src")));
                bannerItems.add(item);
            }
            bannerBean.setItems(bannerItems);
            mainDataBeans.add(bannerBean);
            /*************************** 解析banner内容结束 ***************************/
            /*************************** 解析list内容开始 ***************************/
            Elements vodList = document.select("div.vod-list");
            for (int i=0,size=vodList.size(); i<size; i++) {
                MainDataBean contentBean = new MainDataBean();
                List<MainDataBean.Item> vodItems = new ArrayList<>();
                contentBean.setDataType(ITEM_LIST.getType());
                switch (i) {
                    case 0:
                        contentBean.setTitle("热门电影");
                        contentBean.setHasMore(false);
                        break;
                    case 1:
                        contentBean.setTitle("电影");
                        contentBean.setHasMore(true);
                        contentBean.setMore(CLASSIFICATION_IDS[0]);
                        break;
                    case 2:
                        contentBean.setTitle("连续剧");
                        contentBean.setHasMore(true);
                        contentBean.setMore(CLASSIFICATION_IDS[1]);
                        break;
                    case 3:
                        contentBean.setTitle("综艺");
                        contentBean.setHasMore(true);
                        contentBean.setMore(CLASSIFICATION_IDS[2]);
                        break;
                    case 4:
                        contentBean.setTitle("动画");
                        contentBean.setHasMore(true);
                        contentBean.setMore(CLASSIFICATION_IDS[3]);
                        break;
                }
                Elements aList = vodList.get(i).select("a");
                for (Element a : aList) {
                    MainDataBean.Item item = new MainDataBean.Item();
                    item.setTitle(a.attr("title"));
                    item.setUrl(a.attr("href"));
                    item.setImg(getImageUrl(a.select("img").attr("data-src")));
                    item.setEpisodes(a.select("div.card-content > p.subtitle").text());
                    vodItems.add(item);
                }
                if (vodItems.size() > 0) {
                    contentBean.setItems(vodItems);
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
            String title = document.select(".column .box img").attr("alt");
            String img = getImageUrl(document.select(".column .box img").attr("src"));
            detailsDataBean.setTitle(title);
            //影视图片
            detailsDataBean.setImg(img);
            //影视地址
            detailsDataBean.setUrl(url);
            // tag
            Elements tags = document.select(".tags a");
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tags) {
                String tagTitle = tag.text();
                String tagUrl = tag.attr("href");
                tagTitles.add(tagTitle);
                tagUrls.add(tagUrl);
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);
            Elements infos = document.select(".column .is-horizontal");
            for (Element info : infos) {
                String infoText = replaceDescContent(info.text());
                if (info.text().contains("更新"))
                    detailsDataBean.setUpdateTime(infoText);
                else if (info.text().contains("导演"))
                    detailsDataBean.setScore(infoText);
                else if (info.text().contains("简介"))
                    detailsDataBean.setIntroduction(infoText);
            }
            // 获取所有播放列表
            Elements playTitleList = document.select(".play-source-box .tabs ul li");
            if (playTitleList.size() > 0) {
                // 解析播放列表
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                for (Element li : playTitleList) {
                    DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                    String listTitle = li.text();
                    dramas.setListTitle(listTitle);
                    String dataTab = li.attr("data-tab");
                    Elements playList = document.getElementsByAttributeValue("data-tab", dataTab).select("a");
                    List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
                    parserDramas(dramasItems, playList);
                    dramas.setDramasItemList(dramasItems);
                    dramasList.add(dramas);
                }
                detailsDataBean.setDramasList(dramasList);
                // 解析剧集相关多季 该网点无
                // 解析推荐列表
                Elements recommendElements = document.select(".is-one-third-mobile > a");
                if (recommendElements.size() > 0) {
                    List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
                    for (Element recommend : recommendElements) {
                        String recommendTitle = recommend.attr("title");
                        String recommendImg = getImageUrl(recommend.select("img").attr("data-src"));
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
            Elements playTitleList = document.select(".play-list-box .tabs ul li");
            if (playTitleList.size() > 0) {
                String dataTab = playTitleList.get(listSource).attr("data-tab");
                Elements playList = document.getElementsByAttributeValue("data-tab", dataTab).select("a");
                parserDramas(dramasItemList, playList);
            /*for (int i=0, size=playTitleList.size(); i<size; i++) {
                String dataTab = playTitleList.get(i).attr("data-tab");
                Elements playList = document.getElementsByAttributeValue("data-tab", dataTab).select("a");
                parserDramas(dramasItemList, playList);
            }*/
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
     * @return {@link List<ClassificationDataBean>}
     */
    @Override
    public List<ClassificationDataBean> parserClassificationList(String source) {
        try {
            Document document = Jsoup.parse(source);
            List<ClassificationDataBean> classificationDataBeans = new ArrayList<>();
            Elements columns = document.select(".section .container .library-box .column");
            int index = 0; // 0为VODID 跳过
            for (Element column : columns) {
                if (!column.hasClass("is-hidden")) {
                    index += 1;
                    ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                    String classificationTitle = column.select("span").text();
                    classificationDataBean.setClassificationTitle(classificationTitle);
                    classificationDataBean.setMultipleChoices(false);
                    classificationDataBean.setIndex(index);
                    Elements aList = column.select("a");
                    List<ClassificationDataBean.Item> items = new ArrayList<>();
                    for (Element a : aList) {
                        String title = a.text();
                        boolean isAll = title.equals("全部");
                        items.add(new ClassificationDataBean.Item(title, isAll ? "" : title, isAll));
                    }
                    if (classificationTitle.contains("年份")) {
                        // 判断是否有今年 没有则手动添加进去 方便查询
                        String nowYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                        if (!items.get(1).getTitle().equals(nowYear))
                            items.add(1, new ClassificationDataBean.Item(nowYear, nowYear, false));
                    }
                    classificationDataBean.setItemList(items);
                    classificationDataBeans.add(classificationDataBean);
                }
            }
            List<ClassificationDataBean.Item> items = new ArrayList<>();
            items.add(new ClassificationDataBean.Item("全部", "", true));
            items.add(new ClassificationDataBean.Item("按更新", "time", false));
            items.add(new ClassificationDataBean.Item("按人气", "hits", false));
            items.add(new ClassificationDataBean.Item("按推荐", "score", false));
            index += 1;
            ClassificationDataBean classificationDataBean = new ClassificationDataBean("排序", false, index, items);
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
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select(".vod-list .column a");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.attr("title"));
                    bean.setUrl(item.attr("href"));
                    bean.setImg(getImageUrl(item.select("img").attr("data-src")));
                    Elements tags = item.select(".vod-tagsinfo span");
                    if (!Utils.isNullOrEmpty(tags) && tags.size() > 0) {
                        for (Element tag : tags) {
                            if (tag.hasClass("is-hidden-mobile"))
                                bean.setEpisodesTag(tag.text());
                            else
                                bean.setTopLeftTag(tag.text());
                        }
                    }
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
    public List<VodDataBean> parserSearchVodList(String source) {
        try {
            List<VodDataBean> items = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select(".search-vod-list .column .vod-detail-box");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    Elements aList = item.select(".is-multiline").select("a");
                    for (Element a : aList) {
                        if (a.hasClass("title")) {
                            bean.setTitle(a.text());
                            bean.setUrl(a.attr("href"));
                            break;
                        }
                    }
                    bean.setImg(getImageUrl(item.select("img").attr("src")));
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
        return parserSearchVodList(source);
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
        return 8;
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
        // https://www.rainvi.com/index.php/vod/show/id/1/area/内地/by/score/class/爱情/lang/国语/letter/A/year/2023/page/1.html
        /**
         * params[0] id/1 -> 0
         * params[1] area/内地 -> 2
         * params[2] by/score -> 6
         * params[3] class/爱情 -> 1
         * params[4] lang/国语 -> 3
         * params[5] /letter/A -> 5
         * params[6] year/2023 -> 4
         * params[7] page/1 -> 7
         */
        return montageUrl(params[0], params[2], params[6], params[1], params[3], params[5], params[4], params[7]);
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
        // https://www.rainvi.com/index.php/vod/search/page/1/wd/%E5%B7%A8%E4%BA%BA.html
        // 固定格式：下标0为搜索参数 1为页码
        return getDefaultDomain() + String.format(SourceEnum.TBYS.getSearchUrl(), params[1], params[0]);
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
        // https://www.rainvi.com/index.php/vod/search/class/%E5%89%A7%E6%83%85,%E7%A7%91%E5%B9%BB,%E6%82%AC%E7%96%91,%E5%A5%87%E5%B9%BB,%E9%9F%A9%E5%9B%BD.html
        String modifiedUrl = prependTextBeforeDot(url, "/page/"+page);
        return getDefaultDomain() + modifiedUrl;
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
            Elements scriptElements = document.select("script");
            for (Element script : scriptElements) {
                String html = script.html();
                if (html.contains("player_aaaa")) {
                    logInfo("javaScript", html);
                    String jsonText = html.substring(html.indexOf("{"), html.lastIndexOf("}") + 1);
                    JSONObject jsonObject = JSON.parseObject(jsonText);
                    String encryptUrl = jsonObject.getString("url");
                    logInfo("播放地址加密字符串", encryptUrl);
                    switch (jsonObject.getInteger("encrypt")) {
                        case 2:
                            logInfo("encrypt为2 使用BASE64解码", "");
                            String decodedString = new String(Base64.decode(encryptUrl, Base64.DEFAULT));
                            logInfo("播放地址BASE64解码后", decodedString);
                            try {
                                decodedString = URLDecoder.decode(decodedString, StandardCharsets.UTF_8.toString());
                                logInfo("最终播放地址", decodedString);
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                            result.add(new DialogItemBean(decodedString, decodedString.contains("m3u8") ? M3U8 : MP4));
                            return result;
                        default:
                            result.add(new DialogItemBean(encryptUrl, encryptUrl.contains("m3u8") ? M3U8 : MP4));
                            return result;
                    }
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
     * @return {@link List<TextDataBean>}
     */
    @Override
    public List<TextDataBean> parserTextList(String source) {
        return null;
    }

    // -------------------------------- 该解析通用方法 --------------------------------
    /**
     * 获取分页正则
     */
    private static final Pattern PAGE_PATTERN = Pattern.compile("当前:\\d+/(\\d+)页");

    /**
     * 站点分类IDS数组,根据网站源码写死
     * 1：电影 2：连续剧 3：综艺 4：动画
     */
    private static final String[] CLASSIFICATION_IDS = new String[]{"1","2","3","4"};

    /**
     * 在.前面追加分页参数
     * @param originalUrl
     * @param textToPrepend
     * @return
     */
    public static String prependTextBeforeDot(String originalUrl, String textToPrepend) {
        int dotIndex = originalUrl.lastIndexOf(".");
        if (dotIndex != -1) {
            // 找到最后一个点号的位置，在其前面插入文本
            return originalUrl.substring(0, dotIndex) + textToPrepend + originalUrl.substring(dotIndex);
        } else {
            // 没有点号，直接在末尾追加文本
            return originalUrl + textToPrepend;
        }
    }

    /**
     * 解析播放列表
     * @param dramasItemList
     * @param playList
     */
    private void parserDramas(List<DetailsDataBean.DramasItem> dramasItemList, Elements playList) {
        int index = 0;
        for (Element a : playList) {
            String dramaTitle = a.text();
            String dramaUrl = a.attr("href");
            if (dramaUrl.contains("vod/play")) {
                dramasItemList.add(new DetailsDataBean.DramasItem(index++, dramaTitle, dramaUrl, false));
            }
        }
    }

    /**
     * 分类枚举
     */
    @Getter
    @AllArgsConstructor
    public enum ClassificationEnum {
        AREA("/area/%s"), // 地区
        BY("/by/%s"), // 排序
        CLASS("/class/%s"), // 类型
        ID("/id/%s"), // ID
        LANG("/lang/%s"), // 语言
        LETTER("/letter/%s"), // 字母
        YEAR("/year/%s"), // 年份
        PAGE("/page/%s"); // 分页

        private String content;
    }

    /**
     * 拼接分类url参数
     * @param idStr
     * @param areaStr
     * @param byStr
     * @param classStr
     * @param langStr
     * @param letterStr
     * @param yearStr
     * @param page
     * @return
     */
    private String montageUrl(String idStr, String areaStr, String byStr,
                              String classStr, String langStr, String letterStr,
                              String yearStr, String page) {
        StringBuffer stringBuffer = new StringBuffer();
        if (!Utils.isNullOrEmpty(idStr))
            stringBuffer.append(String.format(ClassificationEnum.ID.getContent(), idStr));
        if (!Utils.isNullOrEmpty(areaStr))
            stringBuffer.append(String.format(ClassificationEnum.AREA.getContent(), areaStr));
        if (!Utils.isNullOrEmpty(byStr))
            stringBuffer.append(String.format(ClassificationEnum.BY.getContent(), byStr));
        if (!Utils.isNullOrEmpty(classStr))
            stringBuffer.append(String.format(ClassificationEnum.CLASS.getContent(), classStr));
        if (!Utils.isNullOrEmpty(langStr))
            stringBuffer.append(String.format(ClassificationEnum.LANG.getContent(), langStr));
        if (!Utils.isNullOrEmpty(letterStr))
            stringBuffer.append(String.format(ClassificationEnum.LETTER.getContent(), letterStr));
        if (!Utils.isNullOrEmpty(yearStr))
            stringBuffer.append(String.format(ClassificationEnum.YEAR.getContent(), yearStr));
        if (!Utils.isNullOrEmpty(page))
            stringBuffer.append(String.format(ClassificationEnum.PAGE.getContent(), page));
        return getDefaultDomain() + String.format(SourceEnum.TBYS.getClassificationUrl(), stringBuffer);
    }

    /**
     * 获取图片地址
     * @param imgUrl
     * @return
     */
    private static String getImageUrl(String imgUrl) {
        if (imgUrl.startsWith("//"))
            return "https:" + imgUrl;
        else
            return imgUrl;
    }

    /**
     * 替换详情相关文本
     * @param text
     * @return
     */
    private static String replaceDescContent(String text) {
        return text.replaceFirst("：(\\s+)", "：");
    }
    // -------------------------------- 该解析通用方法 --------------------------------
}
