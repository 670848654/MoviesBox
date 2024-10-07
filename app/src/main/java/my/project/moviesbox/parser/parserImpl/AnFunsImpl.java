package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;
import static my.project.moviesbox.parser.config.MultiItemEnum.BANNER_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.TAG_LIST;
import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.ANFUNS;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import my.project.moviesbox.contract.DanmuContract;
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
import my.project.moviesbox.parser.config.WeekEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.HomeFragment;
import my.project.moviesbox.view.PlayerActivity;
import my.project.moviesbox.view.TextListActivity;
import my.project.moviesbox.view.WeekActivity;
import okhttp3.FormBody;

/**
  * @包名: my.project.moviesbox.parser.parserImpl
  * @类名: AnFunsImpl
  * @描述: AnFuns站点解析实现
  * @作者: Li Z
  * @日期: 2024/1/26 13:37
  * @版本: 1.0
 */
public class AnFunsImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.ANFUNS.getPostRequestMethod();
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
        return SourceEnum.ANFUNS.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#ANFUNS
     */
    @Override
    public int getSource() {
        return ANFUNS.index;
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
        headers.put("Sec-Ch-Ua", "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"");
        headers.put("Referer", getDefaultDomain());
        headers.put("Sec-Ch-Ua-Mobile", "?0");
        headers.put("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "none");
        headers.put("Upgrade-Insecure-Requests", "1");
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
        Map<String, String> headers = new HashMap<>();
        headers.put("Pragma", "no-cache");
        headers.put("Referer", getDefaultDomain() + "/");
        headers.put("Sec-Ch-Ua", "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"");
        headers.put("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.put("Sec-Fetch-Dest", "image");
        headers.put("Sec-Fetch-Mode", "no-cors");
        headers.put("Sec-Fetch-Site", "cross-site");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.60");
        return headers;
    }

    @Override
    public HashMap<String, String> setPlayerHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accept", "*/*");
        headers.put("accept-encoding", "gzip, deflate, br, zstd");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("origin", getDefaultDomain());
        headers.put("priority", "u=1, i");
        headers.put("sec-ch-ua", "\"Not)A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"127\", \"Chromium\";v=\"127\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "cross-site");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0");
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
        /*Elements pageContent = document.select(".hl-page-total");
        if (pageContent.size() > 0) {
            String regex = "/\\s*(\\d+)页";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(pageContent.text());
            if (matcher.find()) {
                String extractedNumber = matcher.group(1);
                LogUtil.logInfo("总页码", extractedNumber);
                return Integer.valueOf(extractedNumber);
            } else {
                return startPageNum();
            }
        }*/
        Element paginationElement = document.select("ul.hl-page-wrap").first();
        if (paginationElement != null) {
            // 找到包含总页码数的li元素
            Element totalPageElement = paginationElement.select("li.hl-page-tips a").first();
            if (totalPageElement != null) {
                String text = totalPageElement.text();
                String[] parts = text.split("/");
                if (parts.length == 2) {
                    String totalPagesStr = parts[1].trim();
                    LogUtil.logInfo("总页码", totalPagesStr);
                    return Integer.parseInt(totalPagesStr);
                } else
                    LogUtil.logInfo("无法解析总页数", "");
            } else
                LogUtil.logInfo("找不到包含总页码数的元素", "");
        } else
            LogUtil.logInfo("找不到分页元素", "");
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
            // TAG
            mainDataBean = new MainDataBean();
            mainDataBean.setDataType(TAG_LIST.getType());
            List<MainDataBean.Tag> tags = new ArrayList<>();
            tags.add(new MainDataBean.Tag(HomeTagEnum.WEEK.name, HomeTagEnum.WEEK.content, WeekActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.XFJF.name, HomeTagEnum.XFJF.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.LGWX.name, HomeTagEnum.LGWX.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DMZT.name, HomeTagEnum.DMZT.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.OMDM.name, HomeTagEnum.OMDM.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZJGX.name, HomeTagEnum.ZJGX.content, TextListActivity.class));
            mainDataBean.setTags(tags);
            mainDataBeans.add(mainDataBean);
            // 轮播
            Elements bannerElements = document.getElementById("conch-banner").select("ul.swiper-wrapper > li > a");
            mainDataBean = new MainDataBean();
            mainDataBean.setDataType(BANNER_LIST.getType());
            mainDataBean.setVodItemType(STYLE_16_9);
            List<MainDataBean.Item> items = new ArrayList<>();
            for (Element banner : bannerElements) {
                MainDataBean.Item item = new MainDataBean.Item();
                item.setTitle(banner.attr("title"));
                item.setUrl(banner.attr("href"));
                item.setImg(banner.attr("data-original"));
                item.setEpisodes(banner.select("div.hl-br-sub").text());
                items.add(item);
            }
            mainDataBean.setItems(items);
            mainDataBeans.add(mainDataBean);
            // 热播推荐
            Element conchContent = document.getElementById("conch-content");
            Elements listTitle = conchContent.select("div.conch-ctwrap div.container div.hl-row-box div.hl-rb-vod div.hl-rb-head");
            if (listTitle.size() > 0) {
                for (int i=0,size=listTitle.size(); i<size; i++) {
                    if (!listTitle.get(i).parent().hasClass("hl-week-item")) {
                        String title = listTitle.get(i).select("h2.hl-rb-title").text();
                        if (title.contains("排行榜"))
                            continue;
                        Elements moreA = listTitle.get(i).select("a.hl-rb-more");
                        mainDataBean = new MainDataBean();
                        mainDataBean.setTitle(title);
                        mainDataBean.setDataType(ITEM_LIST.getType());
                        if (moreA.size() > 0) {
                            String moreUrl = moreA.attr("href");
                            mainDataBean.setHasMore(true);
                            if (moreUrl.contains("/map")) {
                                mainDataBean.setOpenMoreClass(TextListActivity.class);
                                mainDataBean.setMore("%s"+moreUrl);
                            }
                            else if (moreUrl.contains("/type/")) {
                                String regex = "([0-9]+)";
                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(moreUrl);
                                if (matcher.find())
                                    mainDataBean.setMore(matcher.group());
                            }
                        } else
                            mainDataBean.setHasMore(false);
                        Elements aList = listTitle.get(i).parent().select("div.row div.hl-list-wrap ul li a.hl-lazy");
                        items = new ArrayList<>();
                        for (Element a : aList) {
                            MainDataBean.Item homeItemBean = new MainDataBean.Item();
                            homeItemBean.setTitle(a.attr("title"));
                            homeItemBean.setUrl(a.attr("href"));
                            homeItemBean.setImg(a.attr("data-original"));
                            homeItemBean.setEpisodes(a.select("span.remarks").text());
                            items.add(homeItemBean);
                        }
                        if (items.size() > 0) {
                            mainDataBean.setItems(items);
                            mainDataBeans.add(mainDataBean);
                        }
                    }
                }
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
            Element detailElement = document.getElementById("conch-content");
            String title = detailElement.select(".conch-ctwrap-auto .hl-has-item .container .hl-row-box .hl-dc-pic span").attr("title");
            String img = detailElement.select(".conch-ctwrap-auto .hl-has-item .container .hl-row-box .hl-dc-pic span").attr("data-original");
            detailsDataBean.setTitle(title);
            //番剧图片
            detailsDataBean.setImg(img);
            //番剧地址
            detailsDataBean.setUrl(url);
            // tag
            Elements tags = detailElement.select(".conch-ctwrap-auto .hl-has-item .container .hl-row-box .hl-detail-content .hl-dc-content .hl-vod-data .hl-full-box ul.clearfix li a");
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tags) {
                tagTitles.add(tag.text().toUpperCase());
                tagUrls.add(tag.attr("href").replaceAll("\\.html", "/page/replaceThis.html"));
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);

            Elements emList = detailElement.select(".conch-ctwrap-auto .hl-has-item .container .hl-row-box .hl-detail-content .hl-dc-content .hl-vod-data .hl-full-box ul.clearfix li em");
            for (Element em : emList) {
                if (em.text().contains("状态")) {
                    detailsDataBean.setUpdateTime(em.parent().text());
                } else if (em.text().contains("上映")) {
                    detailsDataBean.setScore(em.parent().text());
                }
            }
            Elements desc = detailElement.select(".conch-ctwrap .container .hl-row-box .hl-rb-content .hl-content-wrap .hl-content-text em");
            detailsDataBean.setIntroduction(desc.text());
            // 获取所有播放列表
            Element playElement = detailElement.getElementById("playlist");
            if (playElement != null) {
                // 解析播放列表
                Elements playTitleList = playElement.select(".hl-plays-wrap a");
                Elements playList = playElement.select(".hl-tabs-box");
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                for (int i=0,size=playTitleList.size(); i<size; i++) {
                    DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                    String playListName = playTitleList.get(i).attr("alt");
                    dramas.setListTitle(playListName);
                    List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
                    Elements playAList =playList.get(i).select("a");
                    int index = 0;
                    for (Element drama : playAList) {
                        if (drama.select("a").select("i").size() > 0)
                            continue;
                        index += 1;
                        String name = drama.select("a").text();
                        String watchUrl = drama.select("a").attr("href");
                        dramasItems.add(new DetailsDataBean.DramasItem(index, name, watchUrl, false));
                    }
                    dramas.setDramasItemList(dramasItems);
                    dramasList.add(dramas);
                }
                detailsDataBean.setDramasList(dramasList);
                // 解析剧集相关多季 该网点无
                // 解析推荐列表
                Elements recommendElements = detailElement.select(".hl-change-list a.hl-lazy"); //相关推荐
                if (recommendElements.size() > 0) {
                    List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
                    for (Element a : recommendElements) {
                        String recommendTitle = a.attr("title");
                        String recommendImg = a.attr("data-original");
                        String recommendUrl = a.attr("href");
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
            Element detailElement = document.getElementById("conch-content");
            // 获取所有播放列表
            Element playElement = detailElement.getElementById("playlist");
            Elements playTitleList = playElement.select(".hl-plays-wrap a");
            Elements playList = playElement.select(".hl-tabs-box");
            List<DetailsDataBean.DramasItem> dramasItemList = new ArrayList<>();
            if (playTitleList.size() > 0) {
                // 解析播放列表
                Elements playing = null;
                for (int i=0,size=playTitleList.size(); i<size; i++) {
                    Elements playAList = playList.get(i).select("a");
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
                        if (element.select("a").select("i").size() > 0)
                            continue;
                        index += 1;
                        String dramaTitle = element.text();
                        String dramaUrl = element.select("a").attr("href");
                        dramasItemList.add(new DetailsDataBean.DramasItem(index, dramaTitle, dramaUrl, false));
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
     * @return {@link List<ClassificationDataBean>}
     */
    @Override
    public List<ClassificationDataBean> parserClassificationList(String source) {
        try {
            Document document = Jsoup.parse(source);
            List<ClassificationDataBean> classificationDataBeans = new ArrayList<>();
            Elements columns = document.select(".hl-filter-wrap.hl-navswiper");
            int index = 0; // 0为ID 跳过
            for (Element column : columns) {
                if (!column.hasClass("hl-filter-opt")) {
                    index += 1;
                    ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                    classificationDataBean.setClassificationTitle(column.select(".hl-filter-item span").text());
                    classificationDataBean.setMultipleChoices(false);
                    classificationDataBean.setIndex(index);
                    Elements aList = column.select("ul li a");
                    List<ClassificationDataBean.Item> items = new ArrayList<>();
                    for (Element a : aList) {
                        String title = a.text();
                        items.add(new ClassificationDataBean.Item(title, title.equals("全部") ? "" : title, title.equals("全部")));
                    }
                    classificationDataBean.setItemList(items);
                    classificationDataBeans.add(classificationDataBean);
                }
            }
            if (classificationDataBeans.size() > 0) {
                // 如果存在分类检索数据才添加
                List<ClassificationDataBean.Item> items = new ArrayList<>();
                items.add(new ClassificationDataBean.Item("全部", "", true));
                items.add(new ClassificationDataBean.Item("按最新", "time", false));
                items.add(new ClassificationDataBean.Item("按最热", "hits", false));
                items.add(new ClassificationDataBean.Item("按评分", "score", false));
                index += 1;
                ClassificationDataBean classificationDataBean = new ClassificationDataBean("排序", false, index, items);
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
            /*Elements elements = document.select(".hl-list-item a.hl-lazy");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean.Item bean = new VodDataBean.Item();
                    bean.setTitle(item.attr("title"));
                    bean.setUrl(item.attr("href"));
                    bean.setImg(item.attr("data-original"));
                    bean.setTopLeftTag(item.select("span.state").text());
                    bean.setEpisodesTag(item.select("span.remarks").text());
                    items.add(bean);
                }
                vodDataBean.setItemList(items);
                logInfo("分类列表数据", vodDataBean.toString());
            }*/
            Elements elements = document.select(".hl-vod-list li a.hl-lazy");
            if (elements.size() > 0) {
                for (Element a : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(a.attr("title"));
                    bean.setUrl(a.attr("href"));
                    bean.setImg(a.attr("data-original"));
                    bean.setTopLeftTag(a.select(".hl-pic-tag").text());
                    bean.setEpisodesTag(a.select(".hl-pic-text").text());
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
            Elements elements = document.select(".hl-list-item a.hl-lazy");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.attr("title"));
                    bean.setUrl(item.attr("href"));
                    bean.setImg(item.attr("data-original"));
                    bean.setTopLeftTag(item.select("span.state").text());
                    bean.setEpisodesTag(item.select("span.remarks").text());
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
        return parserClassificationVodList(source);
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
        // https://www.anfuns.cc/show/3-%E6%90%9E%E7%AC%91-B-2024/area/%E6%97%A5%E6%9C%AC/by/hits/lang/%E6%97%A5%E8%AF%AD/page/1.html
        /**
         * params[0] id -> 0
         * params[1] type -> 1
         * params[2] area -> 4
         * params[3] year -> 3
         * params[4] lang -> 6
         * params[5] latter -> 2
         * params[6] by -> 5
         * params[7] page -> 7
         */
        return montageUrl(params[0], params[1], params[5], params[3], params[2], params[6], params[4], params[7]);
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
        // https://www.anfuns.cc/search/page/1/wd/%E9%AC%BC%E7%81%AD%E4%B9%8B%E5%88%83.html
        // 固定格式：下标0为搜索参数 1为页码
        LogUtil.logInfo("getSearchUrl", getDefaultDomain() + String.format(SourceEnum.ANFUNS.getSearchUrl(), params[1], params[0]));
        return getDefaultDomain() + String.format(SourceEnum.ANFUNS.getSearchUrl(), params[1], params[0]);
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
        if (url.contains("/search/")) {
            return getDefaultDomain() + url.replaceAll("replaceThis", String.valueOf(page));
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
        // 该站点查询弹幕只需要id和下标
        return getDefaultDomain() + String.format(SourceEnum.ANFUNS.getDanmuUrl(), params[0], params[1]);
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
    public List<DialogItemBean> getPlayUrl(String source) {
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
                            logInfo("BASE64解码后", decodedString);
                            try {
                                decodedString = decodedString.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                                logInfo("替换特殊字符后", decodedString);
                                decodedString = URLDecoder.decode(decodedString, StandardCharsets.UTF_8.toString());
                                logInfo("播放地址URL解码后 - >", decodedString);
                                decodedString = decodedString.replaceAll("%", "\\\\");
                                logInfo("unicode转中文后", decodedString);
                                decodedString = unicodeDecode(decodedString);
                                logInfo("最终播放地址", decodedString);
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                            result.add(new DialogItemBean(decodedString, decodedString.contains("m3u8") ? M3U8 : MP4));
                            return result;
                        case 3:
                            logInfo("encrypt为3 通过API获取", "");
                            String api = getDefaultDomain() + "/vapi/AIRA/art.php?url=%s&next=&vid=%s&title=%s&nid=%s&uid=guest&name=guest&group=guest";
                            String url = encryptUrl;
                            String vid = jsonObject.getString("id");
                            String title = URLEncoder.encode(jsonObject.getJSONObject("vod_data").getString("vod_name"), "UTF-8");
                            int nid = jsonObject.getInteger("nid");
                            String parseApi = String.format(api, url, vid, title, nid);
                            logInfo("parserApi", parseApi);
                            String apiResult = OkHttpUtils.performSyncRequestAndHeader(parseApi);
                            logInfo("apiResult", apiResult);
                            String regex = "url:\\s*'([^']+)'";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(apiResult);
                            // 查找匹配的内容
                            if (matcher.find()) {
                                String videoUrl = matcher.group(1);
                                logInfo("播放地址", videoUrl);
                                result.add(new DialogItemBean(videoUrl, videoUrl.contains("m3u8") ? M3U8 : MP4));
                                return result;
                            }
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
        try {
            List<WeekDataBean> weekDataBeans = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements weekElements = document.select(".hl-vod-list.hl-fadeIn.swiper-wrapper");
            if (weekElements.size() > 0) {
                for (int i = 0, size = WeekEnum.values().length; i<size; i++) {
                    Elements aList = weekElements.get(i).select("li a.hl-lazy");
                    int week = WeekEnum.values()[i].getIndex();
                    List<WeekDataBean.WeekItem> weekItems = new ArrayList<>();
                    for (Element a : aList) {
                        weekItems.add(new WeekDataBean.WeekItem(
                                a.attr("title"),
                                a.attr("data-original"),
                                a.attr("href"),
                                a.select("span.remarks").text(),
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
            Elements aList = document.select("ul.hl-rank-list li a");
            if (aList.size() > 0) {
                List<TextDataBean.Item> itemList = new ArrayList<>();
                int index = 0;
                for (Element a : aList) {
                    index += 1;
                    TextDataBean.Item rankItem = new TextDataBean.Item();
                    rankItem.setIndex(String.valueOf(index));
                    rankItem.setTitle(a.select(".hl-item-div1 .hl-item-content").text().replaceAll(" ", ""));
                    rankItem.setUrl(a.attr("href"));
                    rankItem.setEpisodes(a.select(".hl-item-div2").text());
                    Elements updateTime = a.select(".hl-item-div5 span");
                    rankItem.setContent(updateTime.get(0).text() + " " + updateTime.get(1).text());
                    itemList.add(rankItem);
                }
                textDataBeans.add(new TextDataBean("", itemList));
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
     * 首页TAG枚举
     */
    @Getter
    @AllArgsConstructor
    public enum HomeTagEnum {
        WEEK("番剧时间表", "%s"),
        XFJF("新番旧番", "1"),
        LGWX("蓝光无修", "2"),
        DMZT("动漫剧场", "3"),
        OMDM("欧美动漫", "4"),
        ZJGX("最近更新", "%s/map.html");

        private String name;
        private String content;
    }

    /**
     * 分类枚举
     */
    @Getter
    @AllArgsConstructor
    public enum ClassificationEnum {
        // /show/3-搞笑-B-2024/area/日本/by/hits/lang/日语/page/1.html
        ID("%s-%s-%s-%s"), // type 值
        TYPE("%s"), // 类型
        LETTER("%s"), // 字母
        YEAR("%s"), // 年份
        AREA("/area/%s"), // 地区
        BY("/by/%s"), // 排序
        LANG("/lang/%s"), // 语言
        PAGE("/page/%s"); // 分页

        private String content;
    }

    /**
     * 拼接分类url参数
     * @param id
     * @param type
     * @param letter
     * @param year
     * @param area
     * @param by
     * @param lang
     * @param page
     * @return
     */
    private String montageUrl(@NonNull String id, String type, String letter,
                              String year, String area, String by,
                              String lang, String page) {
        StringBuffer stringBuffer = new StringBuffer();
        /*if (Utils.isNullOrEmpty(type)) {
            type = "";
        }
        if (Utils.isNullOrEmpty(letter)) {
            letter = "";
        }
        if (Utils.isNullOrEmpty(year)) {
            year = "";
        }
        stringBuffer.append(String.format(ClassificationEnum.ID.getContent(), id, type, letter, year));
        if (!Utils.isNullOrEmpty(area))
            stringBuffer.append(String.format(ClassificationEnum.AREA.getContent(), area));
        if (!Utils.isNullOrEmpty(by))
            stringBuffer.append(String.format(ClassificationEnum.BY.getContent(), by));
        if (!Utils.isNullOrEmpty(lang))
            stringBuffer.append(String.format(ClassificationEnum.LANG.getContent(), lang));
        if (!Utils.isNullOrEmpty(page))
            stringBuffer.append(String.format(ClassificationEnum.PAGE.getContent(), page));*/
        stringBuffer.append(String.format(ClassificationEnum.TYPE.getContent(), id+"-"+page));
        LogUtil.logInfo("class url", getDefaultDomain() + String.format(SourceEnum.ANFUNS.getClassificationUrl(), stringBuffer));
        return getDefaultDomain() + String.format(SourceEnum.ANFUNS.getClassificationUrl(), stringBuffer);
    }
}
