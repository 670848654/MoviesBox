package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.ItemStyleEnum.STYLE_16_9;
import static my.project.moviesbox.parser.config.MultiItemEnum.BANNER_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.MultiItemEnum.TAG_LIST;
import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.I_YINGHUA;
import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;

import androidx.annotation.LayoutRes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.contract.DanmuContract;
import my.project.moviesbox.model.DanmuModel;
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
import my.project.moviesbox.parser.config.VodItemStyleEnum;
import my.project.moviesbox.parser.config.WeekEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.HomeFragment;
import my.project.moviesbox.view.PlayerActivity;
import my.project.moviesbox.view.TextListActivity;
import my.project.moviesbox.view.TopticListActivity;
import my.project.moviesbox.view.VodListActivity;
import my.project.moviesbox.view.WeekActivity;
import okhttp3.FormBody;

/**
  * @包名: my.project.moviesbox.parser.impl
  * @类名: IYingHuaImpl
  * @描述: 樱花动漫站点解析实现
  * @作者: Li Z
  * @日期: 2024/1/23 16:39
  * @版本: 1.0
 */
public class IYingHuaImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.I_YINGHUA.getPostRequestMethod();
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
        return SourceEnum.I_YINGHUA.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#I_YINGHUA
     */
    @Override
    public int getSource() {
        return I_YINGHUA.index;
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
        Elements pages = document.select("div.pages");
        return pages.size() > 0 ? Integer.parseInt(document.getElementById("lastn").text()) : startPageNum();
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
            // tag内容解析
            mainDataBean = new MainDataBean();
            mainDataBean.setDataType(TAG_LIST.getType());
            List<MainDataBean.Tag> tags = new ArrayList<>();
            tags.add(new MainDataBean.Tag(HomeTagEnum.WEEK.name, HomeTagEnum.WEEK.content, WeekActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DMFL.name, HomeTagEnum.DMFL.content, ClassificationVodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DMDY.name, HomeTagEnum.DMDY.content, VodListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.DMZT.name, HomeTagEnum.DMZT.content, TopticListActivity.class));
            tags.add(new MainDataBean.Tag(HomeTagEnum.ZJGX.name, HomeTagEnum.ZJGX.content, TextListActivity.class));
            mainDataBean.setTags(tags);
            mainDataBeans.add(mainDataBean);
            // banner内容解析
            Elements bannerEle = document.select("div.hero-wrap > ul.heros > li");
            mainDataBean = new MainDataBean();
            mainDataBean.setTitle("动漫推荐");
            mainDataBean.setHasMore(false);
            mainDataBean.setDataType(BANNER_LIST.getType());
            mainDataBean.setVodItemType(STYLE_16_9);
            List<MainDataBean.Item> items = new ArrayList<>();
            for (Element element : bannerEle) {
                MainDataBean.Item item = new MainDataBean.Item();
                item.setTitle(element.select("a").attr("title"));
                item.setUrl(element.select("a").attr("href"));
                item.setImg(element.select("img").attr("src"));
                item.setEpisodes(element.getElementsByTag("em").text());
                items.add(item);
            }
            mainDataBean.setItems(items);
            mainDataBeans.add(mainDataBean);
            // 数据列表解析
            Elements titles = document.select("div.firs > div.dtit");
            Elements data = document.select("div.firs > div.img");
            for (int i=0,size=titles.size(); i<size; i++) {
                mainDataBean = new MainDataBean();
                mainDataBean.setTitle(titles.get(i).select("h2 > a").text());
                mainDataBean.setHasMore(i != 0);
                String moreUrl = titles.get(i).select("h2 > a").attr("href");
                mainDataBean.setMore(moreUrl);
                if (HomeTagEnum.DMDY.content.contains(moreUrl)) // 如果是电影 则应该返回视频列表视图
                    mainDataBean.setOpenMoreClass(VodListActivity.class);
                mainDataBean.setDataType(ITEM_LIST.getType());
                items = new ArrayList<>();
                Elements vodLi = data.get(i).select("ul > li");
                for (Element li : vodLi) {
                    Elements aList = li.select("a");
                    MainDataBean.Item item = new MainDataBean.Item();
                    item.setTitle(aList.get(1).text());
                    item.setUrl(aList.get(1).attr("href"));
                    item.setImg(aList.get(0).select("img").attr("src"));
                    item.setEpisodes(aList.size() == 3 ? aList.get(2).text() : "");
                    items.add(item);
                }
                if (items.size() > 0) {
                    mainDataBean.setItems(items);
                    mainDataBeans.add(mainDataBean);
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
            String title = document.select("h1").text();
            String img = document.select("div.thumb > img").attr("src");
            detailsDataBean.setTitle(title);
            //番剧图片
            detailsDataBean.setImg(img);
            //番剧地址
            detailsDataBean.setUrl(url);
            // tag
            Elements tagElements = new Elements();
            tagElements.addAll(document.select("div.sinfo > span").get(0).select("a"));
            tagElements.addAll(document.select("div.sinfo > span").get(1).select("a"));
            tagElements.addAll(document.select("div.sinfo > span").get(2).select("a"));
            tagElements.addAll(document.select("div.sinfo > span").get(4).select("a"));
            List<String> tagTitles = new ArrayList<>();
            List<String> tagUrls = new ArrayList<>();
            for (Element tag : tagElements) {
                tagTitles.add(tag.text().toUpperCase());
                tagUrls.add(tag.attr("href"));
            }
            detailsDataBean.setTagTitles(tagTitles);
            detailsDataBean.setTagUrls(tagUrls);
            if (document.select("div.sinfo > p").size() > 1)
                detailsDataBean.setUpdateTime(document.select("div.sinfo > p").get(1).text().isEmpty() ? "暂无更新" : document.select("div.sinfo > p").get(1).text());
            else
                detailsDataBean.setUpdateTime(document.select("div.sinfo > p").get(0).text().isEmpty() ? "暂无更新" : document.select("div.sinfo > p").get(0).text());
            detailsDataBean.setScore(document.select("div.score > em").text());
            detailsDataBean.setIntroduction(document.select("div.info").text());
            // 获取所有播放列表
            Elements playTitleList = document.select("div.play-pannel-box");
            // 解析播放列表
            List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
            DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
            dramas.setListTitle("默认播放列表");
            // 解析剧集列表
            Elements dramaElements = document.select("div.movurl > ul > li"); //剧集列表
            List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
            int index = 0;
            for (Element drama : dramaElements) {
                String name = drama.select("a").text();
                String watchUrl = drama.select("a").attr("href");
                dramasItems.add(new DetailsDataBean.DramasItem(index++, name, watchUrl, false));
            }
            dramas.setDramasItemList(dramasItems);
            dramasList.add(dramas);
            detailsDataBean.setDramasList(dramasList);
            // 解析剧集相关多季
            Elements multiElements = document.select("div.img > ul > li"); //多季
            List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
            if (multiElements.size() > 0) {
                for (Element multi : multiElements) {
                    String multiTitle = multi.select("p.tname > a").text();
                    String multiImg = multi.select("img").attr("src");
                    String multiUrl = multi.select("p.tname > a").attr("href");
                    recommendList.add(new DetailsDataBean.Recommend(multiTitle, multiImg, multiUrl));
                }
                detailsDataBean.setMultiList(recommendList);
            }
            // 解析推荐列表
            Elements recommendElements = document.select("div.pics > ul > li"); //相关推荐
            if (recommendElements.size() > 0) {
                recommendList = new ArrayList<>();
                for (Element recommend : recommendElements) {
                    String recommendTitle = recommend.select("h2 > a").text();
                    String recommendImg = recommend.select("img").attr("src");
                    String recommendUrl = recommend.select("h2 > a").attr("href");
                    recommendList.add(new DetailsDataBean.Recommend(recommendTitle, recommendImg, recommendUrl));
                }
                detailsDataBean.setRecommendList(recommendList);
            }
            logInfo("影视详情内容", detailsDataBean.toString());
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
            Elements dataElement = document.select("div.movurls > ul > li");
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
        return false;
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
            // OLD
            /*Elements titles = document.select("div.dtit");
            Elements items = document.select("div.link");
            List<ClassificationDataBean> classificationDataBeans = new ArrayList<>();
            if (titles.size() == items.size()) {
                for (int i=1,tagSize=titles.size(); i<tagSize; i++) {
                    ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                    classificationDataBean.setClassificationTitle(titles.get(i).text());
                    Elements itemElements = items.get(i).select("a");
                    List<ClassificationDataBean.Item> itemList = new ArrayList<>();
                    for (Element element : itemElements) {
                        itemList.add(new ClassificationDataBean.Item(element.text(), element.attr("href"), false));
                    }
                    classificationDataBean.setItemList(itemList);
                    classificationDataBeans.add(classificationDataBean);
                }
            }*/
            // NEW
            Elements labels = document.select("div.ters p label");
            List<ClassificationDataBean> classificationDataBeans = new ArrayList<>();
            if (labels.size() > 0) {
                // 遍历label标签
                for (Element label : labels) {
                    // 获取label标签后面的所有兄弟节点中的a标签
                    Elements siblings = label.parent().children();
                    ClassificationDataBean classificationDataBean = new ClassificationDataBean();
                    classificationDataBean.setClassificationTitle(label.text());
                    classificationDataBean.setIndex(0); // ！！注意！！因为不支持多条件查询，下标始终为0
                    List<ClassificationDataBean.Item> itemList = new ArrayList<>();
                    // 标记是否开始处理当前label对应的a标签
                    boolean startProcessing = false;

                    // 遍历兄弟节点，找到与当前label对应的a标签
                    for (Element sibling : siblings) {
                        if (sibling.tagName().equals("label")) {
                            // 如果遇到下一个label，停止处理
                            if (startProcessing) {
                                break;
                            }
                            // 如果遇到当前label，开始处理
                            if (sibling.equals(label)) {
                                startProcessing = true;
                            }
                        } else if (startProcessing && sibling.tagName().equals("a")) {
                            String href = sibling.attr("href");
                            String text = sibling.text();
                            itemList.add(new ClassificationDataBean.Item(text, href, false));
                        }
                    }
                    classificationDataBean.setItemList(itemList);
                    classificationDataBeans.add(classificationDataBean);
                }
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
            Elements elements = document.select("div.lpic > ul > li");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.select("h2").text());
                    bean.setUrl(item.select("h2 > a").attr("href"));
                    bean.setImg(item.select("img").attr("src"));
                    bean.setEpisodesTag(item.select("span font").text());
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
        return parserClassificationVodList(source);
    }

    /**
     * 拖布影视的详情TAG地址 [其他影视列表]
     *
     * @param source 网页源代码
     * @return {@link VodDataBean}
     */
    @Override
    public List<VodDataBean> parserVodList(String source) {
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.lpic > ul > li");
        if (elements.size() > 0)
            return parserClassificationVodList(source);
        try {
            List<VodDataBean> items = new ArrayList<>();
            document = Jsoup.parse(source);
            elements = document.select("div.imgs > ul > li");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.select("p > a").text());
                    bean.setUrl(item.select("p > a").attr("href"));
                    bean.setImg(item.select("img").attr("src"));
                    items.add(bean);
                }
                logInfo("番剧列表数据", items.toString());
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
        // http://www.iyinghua.io/japan/2.html
        // 固定格式：下标0为搜索参数 1为页码
        if (!params[1].equals("1")) // 如果不是第一页
            return getDefaultDomain() + params[0] + params[1]+".html";
        else
            return getDefaultDomain() + params[0];
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
        // http://www.iyinghua.io/search/%E9%AD%94%E6%B3%95/?page=2
        // 固定格式：下标0为搜索参数 1为页码
        return getDefaultDomain() + String.format(SourceEnum.I_YINGHUA.getSearchUrl(), params[0], params[1]);
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
        if (url.contains("movie"))
            // 动漫电影
            // http://www.iyinghua.io/movie/2.html
            return getDefaultDomain() + String.format(url, page != 1 ? page + ".html" : "");
        else
            // 视频列表
            return getDefaultDomain() + url + (page != 1 ? page + ".html" : "");
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
            List<DialogItemBean> urls = new ArrayList<>();
            Document document = Jsoup.parse(source);
            Elements elements = document.select("div.playbo > a");
            if (elements.size() > 0) {
                for (int i=0,size=elements.size(); i<size; i++) {
                    String url = getVideoUrl(elements.get(i).attr("onClick"));
                    urls.add(new DialogItemBean(url, url.contains("m3u8") ? M3U8 : MP4));
                }
            }
            return urls;
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
     * 设置时间表数据列表样式
     * @see WeekDataBean#ITEM_TYPE_0
     * @see WeekDataBean#ITEM_TYPE_1
     * @see WeekDataBean#ITEM_TYPE_2
     * @return {@link LayoutRes}
     * @return
     */
    @Override
    public int setWeekItemType() {
        return WeekDataBean.ITEM_TYPE_2;
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
            Elements weekElements = document.select("div.tlist > ul");
            if (weekElements.size() > 0) {
                for (int i = 0, size = WeekEnum.values().length; i<size; i++) {
                    Elements weekLi = weekElements.get(i).select("li");
                    int week = WeekEnum.values()[i].getIndex();
                    List<WeekDataBean.WeekItem> weekItems = new ArrayList<>();
                    for (Element li : weekLi) {
                        if (li.select("a").size() > 1) {
                            weekItems.add(new WeekDataBean.WeekItem(
                                    li.select("a").get(1).text(),
                                    "",
                                    li.select("a").get(1).attr("href"),
                                    li.select("a").get(0).text(),
                                    li.select("a").get(0).attr("href")
                            ));
                        } else {
                            weekItems.add(new WeekDataBean.WeekItem(
                                    li.select("a").get(1).text(),
                                    "",
                                    li.select("a").get(1).attr("href"),
                                    "",
                                    ""
                            ));
                        }

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
            Elements elements = document.select("div.dnews > ul > li");
            if (elements.size() > 0) {
                List<VodDataBean> items = new ArrayList<>();
                for (Element element : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setVodItemStyleType(VodItemStyleEnum.STYLE_16_9.getType());
                    bean.setTitle(element.select("p").text());
                    bean.setUrl(element.select("p > a").attr("href"));
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
        return parserVodList(source);
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
        // http://www.iyinghua.io/topic/2.html
        // 固定格式：下标0为搜索参数 1为页码
        if (page != 1) // 如果不是第一页
            return getDefaultDomain() + String.format(url, page) + ".html";
        else
            return getDefaultDomain() + String.format(url, "");
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
            Elements elements = document.select("div.topli > ul > li");
            List<TextDataBean.Item> items = new ArrayList<>();
            if (elements.size() > 0) {
                int index = 0;
                for (Element li : elements) {
                    index += 1;
                    Elements aList = li.select("a");
                    TextDataBean.Item item = new TextDataBean.Item();
//                    item.setIndex(li.select("i").text());
                    item.setIndex(String.valueOf(index));
                    Elements span = li.select("span > a");
                    if (span.size() > 0) {
                        item.setTitle(aList.get(1).text());
                        item.setUrl(aList.get(1).attr("href"));
                        item.setEpisodes(aList.get(2).text());
                    } else {
                        item.setTitle(aList.get(0).text());
                        item.setUrl(aList.get(0).attr("href"));
                        item.setEpisodes(aList.get(1).text());
                    }
                    item.setContent(li.select("em").text());
                    items.add(item);
                }
                textDataBeans.add(new TextDataBean("", items));
                logInfo("排行榜数据", textDataBeans.toString());
            }
            return textDataBeans;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserTextList error", e.getMessage());
        }
        return null;
    }

    // -------------------------------- 该解析通用方法 --------------------------------
    /**
     * 获取播放地址
     * @param url
     * @return
     */
    private static String getVideoUrl(String url) {
        if (!url.contains("$mp4"))
            return url.replaceAll("changeplay\\('", "").replaceAll("'\\);", "").replaceAll("\\$(.*)", "");
        else
            return url.replaceAll("\\$(.*)", "").replaceAll("changeplay\\('", "").replaceAll("'\\);", "");
    }

    /**
     * 首页TAG枚举
     */
    @Getter
    @AllArgsConstructor
    public enum HomeTagEnum {
        WEEK("番剧时间表", "%s"),
        DMFL("动漫分类", SourceEnum.I_YINGHUA.getClassificationUrl()),
        DMDY("动漫电影", "/movie/%s"),
        DMZT("动漫专题", "/topic/%s"),
        ZJGX("最新更新", "%s/new/");

        private String name;
        private String content;
    }
}
