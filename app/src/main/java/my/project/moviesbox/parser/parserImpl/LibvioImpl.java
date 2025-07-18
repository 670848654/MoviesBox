package my.project.moviesbox.parser.parserImpl;

import static my.project.moviesbox.parser.LogUtil.logInfo;
import static my.project.moviesbox.parser.config.MultiItemEnum.ITEM_LIST;
import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.LIBVIO;
import static my.project.moviesbox.parser.config.VodTypeEnum.M3U8;
import static my.project.moviesbox.parser.config.VodTypeEnum.MP4;

import android.text.Html;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.strategy.danmu.DanmuStrategyFactory;
import my.project.moviesbox.utils.Utils;
import my.project.moviesbox.view.ClassificationVodListActivity;
import my.project.moviesbox.view.HomeFragment;
import my.project.moviesbox.view.PlayerActivity;
import okhttp3.FormBody;

/**
  * @包名: my.project.moviesbox.parser.parserImpl
  * @类名: LibvioImpl
  * @描述: libvio站点解析实现
  * @作者: Li Z
  * @日期: 2024/1/31 20:07
  * @版本: 1.0
 */
public class LibvioImpl implements ParserInterface {
    /**
     * 站点使用POST请求的类
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum#postRequestMethod
     */
    @Override
    public List<String> getPostMethodClassName() {
        return SourceEnum.LIBVIO.getPostRequestMethod();
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
        return SourceEnum.LIBVIO.getSourceName();
    }

    /**
     * 站点标识
     * <p>请在站点枚举类中配置</p>
     *
     * @return
     * @see SourceEnum.SourceIndexEnum#LIBVIO
     */
    @Override
    public int getSource() {
        return LIBVIO.index;
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
        headers.put("Referer", getDefaultDomain() + "/");
//        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        headers.put("Origin", getDefaultDomain());
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
        headers.put("accept-encoding", "identity;q=1, *;q=0");
        headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.put("priority", "i");
        headers.put("range", "bytes=0-");
        headers.put("referer", getDefaultDomain());
        headers.put("sec-ch-ua", "\"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("sec-fetch-dest", "video");
        headers.put("sec-fetch-mode", "no-cors");
        headers.put("sec-fetch-site", "cross-site");
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
        Document document = Jsoup.parse(source);
        Elements pageContent = document.select(".stui-page__item li a");
        if (pageContent.size() > 0) {
            Element lastElement = pageContent.get(pageContent.size()-1);
            String regex = "-(\\d+)-";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(lastElement.attr("href"));
            if (matcher.find()) {
                String number = matcher.group(1);
                System.out.println("Number: " + number);
                return Integer.parseInt(number);
            } else {
                System.out.println("Number not found in the link.");
                return startPageNum();
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
            MainDataBean mainDataBean;
            // 今日热门
            Elements vodList = document.select("div.stui-pannel__bd ul.stui-vodlist");
            for (int i=0,size=vodList.size(); i<size; i++) {
                Elements aList = vodList.get(i).select("a.lazyload");
                mainDataBean = new MainDataBean();
                mainDataBean.setDataType(ITEM_LIST.getType());
                mainDataBean.setHasMore(true);
                List<MainDataBean.Item> items = new ArrayList<>();
                switch (i) {
                    case 0:
                        mainDataBean.setTitle("热门推荐");
                        mainDataBean.setHasMore(false);
                        break;
                    case 1:
                        mainDataBean.setTitle("电影");
                        mainDataBean.setMore("1");
                        break;
                    case 2:
                        mainDataBean.setTitle("电视剧");
                        mainDataBean.setMore("2");
                        break;
                    case 3:
                        mainDataBean.setTitle("动漫");
                        mainDataBean.setMore("4");
                        break;
                    case 4:
                        mainDataBean.setTitle("综艺");
                        mainDataBean.setMore("3");
                        break;
                    case 5:
                        mainDataBean.setTitle("即将上映");
                        mainDataBean.setMore("27");
                        break;
                }
                for (Element a : aList) {
                    MainDataBean.Item homeItemBean = new MainDataBean.Item();
                    String title = a.attr("title");
                    String url = a.attr("href");
                    String img = a.attr("data-original");
                    String episodes = a.select("span.text-right").text();
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
            Elements content = document.select(".stui-pannel__bd .stui-content");
            if (content.size() == 0)
                return null;
            String title = content.select(".stui-content__detail h1").text();
            String img = content.select(".stui-content__thumb a img").attr("data-original");
            detailsDataBean.setTitle(title);
            //影视图片
            detailsDataBean.setImg(img);
            //影视地址
            detailsDataBean.setUrl(url);
            // 无TAG
            Elements data = content.select(".stui-content__detail p.data");
            for (Element s : data) {
                if (s.text().contains("主演"))
                    detailsDataBean.setInfo(s.text());
                else if (s.text().contains("更新"))
                    detailsDataBean.setUpdateTime(s.text());
                else if (s.text().contains("评分"))
                    detailsDataBean.setScore(s.text());
            }
            // 简介
            detailsDataBean.setIntroduction(content.select(".stui-content__detail p.desc .detail-content").text());
            // 获取所有播放列表
            Elements playList = document.select(".stui-pannel__bd .stui-vodlist__head");
            if (playList.size() > 0) {
                List<DetailsDataBean.Dramas> dramasList = new ArrayList<>();
                for (Element play : playList) {
                    if (play.select("h3").text().contains("播放")) {
                        // 解析播放列表
                        DetailsDataBean.Dramas dramas = new DetailsDataBean.Dramas();
                        String playListName = play.select("h3").text();
                        dramas.setListTitle(playListName);
                        List<DetailsDataBean.DramasItem> dramasItems = new ArrayList<>();
                        int index = 0;
                        for (Element element : play.select("ul li a")) {
                            String name = element.select("a").text();
                            String watchUrl = element.select("a").attr("href");
//                    Log.e("dramaStr - > " , dramaStr + "- > " + watchUrl);
                            dramasItems.add(new DetailsDataBean.DramasItem(index++, name, watchUrl, false));
                        }
                        dramas.setDramasItemList(dramasItems);
                        dramasList.add(dramas);
                        detailsDataBean.setDramasList(dramasList);
                    }
                }
                // 解析剧集相关多季 该网点无
                // 解析推荐列表
                Elements recommendElements = document.select(".stui-pannel__bd ul.stui-vodlist.clearfix li a.lazyload"); //相关推荐
                if (recommendElements.size() > 0) {
                    List<DetailsDataBean.Recommend> recommendList = new ArrayList<>();
                    for (Element recommend : recommendElements) {
                        String recommendTitle = recommend.attr("title");
                        String recommendImg = recommend.attr("data-original");
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
            Elements titleElements = document.select("ul.play-tab li a");
            Elements ulElements = document.select(".play-content .stui-play__list");
            // 获取所有播放列表
            List<DetailsDataBean.DramasItem> dramasItemList = new ArrayList<>();
            if (titleElements.size() > 0) {
                // 解析播放列表
                for (int i=0,size=titleElements.size(); i<size; i++) {
                    if (titleElements.get(i).text().contains("播放")) {
                        Elements playing = null;
                        Elements liList = ulElements.get(i).select("li");
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
        return false;
    }

    /**
     * 获取剧集分类列表接口 (分类)
     *
     * @param source
     * @return {@link List< ClassificationDataBean >}
     */
    @Override
    public List<ClassificationDataBean> parserClassificationList(String source) {
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
            Elements elements = document.select(".stui-pannel .stui-pannel__bd ul li a.lazyload");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.attr("title"));
                    bean.setUrl(item.attr("href"));
                    bean.setImg(item.attr("data-original"));
                    bean.setEpisodesTag(item.select(".text-right").text());
                    bean.setTopLeftTag(item.select(".pic-tag-top").text());
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
            Elements elements = document.select("ul.stui-vodlist.clearfix li a.lazyload");
            if (elements.size() > 0) {
                for (Element item : elements) {
                    VodDataBean bean = new VodDataBean();
                    bean.setTitle(item.attr("title"));
                    bean.setUrl(item.attr("href"));
                    bean.setImg(item.attr("data-original"));
                    bean.setEpisodesTag(item.select(".text-right").text());
                    bean.setTopLeftTag(item.select(".pic-tag-top").text());
                    items.add(bean);
                }
                logInfo("搜索列表数据", items.toString());
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("parserClassificationVodList error", e.getMessage());
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
        return getDefaultDomain() + String.format(SourceEnum.LIBVIO.getClassificationUrl(), params[0], params[1]);
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
        return getDefaultDomain() + String.format(SourceEnum.LIBVIO.getSearchUrl(), params[0], params[1]);
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
        return null;
    }

    /**
     * 弹幕接口返回是否为JSON
     * <p>注：弹幕只有两种格式 XML/JsonObject</p>
     * <p>JSON弹幕需自行实现弹幕解析{@link DanmuStrategyFactory#getStrategy}</p>
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
            Element playerScript = null;
            for (Element script : scriptElements) {
                String html = script.html();
                if (html.contains("player_aaaa")) {
                    playerScript = script;
                    break;
                }
            }
            if (playerScript == null)
                return null;
            else {
                String script = playerScript.html();
                logInfo("javaScript", script);
                String jsonText = script.substring(script.indexOf("{"), script.lastIndexOf("}") + 1);
                JSONObject jsonObject = JSON.parseObject(jsonText);
                String url = jsonObject.getString("url");
                String nextUrl = jsonObject.getString("link_next");
                String id = jsonObject.getString("id");
                String nid = jsonObject.getString("nid");
                String jsName = jsonObject.getString("from");
                // 调用接口获取真实播放地址
                // 先获取接口
                logInfo("getParserApiUrl", getDefaultDomain() + "/static/player/"+jsName+".js");
//                Document urlJS = Jsoup.connect(getDefaultDomain() + "/static/player/"+jsName+".js").ignoreContentType(true).headers(requestHeaders()).get();
                String responseData = OkHttpUtils.getInstance().performSyncRequest(getDefaultDomain() + "/static/player/"+jsName+".js");
                Document urlJS = Jsoup.parse(responseData);
                String parserApi = "";
                String regex = "src=\"(.*?)\"";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(urlJS.html());
                if (matcher.find()) {
                    parserApi = matcher.group(1);
                    logInfo("parserApi", parserApi);
                    parserApi = Html.fromHtml(parserApi).toString();
                    parserApi = parserApi
                            .replaceAll("\'\\+MacPlayer.PlayUrl\\+\'", url)
                            .replaceAll("\'\\+MacPlayer.PlayLinkNext\\+\'", nextUrl)
                            .replaceAll("\'\\+MacPlayer.Id\\+\'", id)
                            .replaceAll("\'\\+MacPlayer.Nid\\+\'", nid);
                } else {
                    logInfo("getPlayUrl error", "Base URL not found in the JavaScript code.");
                    return null;
                }
                parserApi = parserApi.startsWith("http") ? parserApi : getDefaultDomain()+parserApi;
                logInfo("parserUrl", parserApi);
                responseData = OkHttpUtils.getInstance().performSyncRequestAndHeader(parserApi);
                LogUtil.logInfo("responseData", responseData);
                Document doc = Jsoup.parse(responseData);
                if(doc != null) {
                    String html = doc.select("script").html();
                    DialogItemBean dialogItemBean = extractDialogItemBean(html,
                            "var urls\\s*=\\s*'([^']+)';",
                            "var vid\\s*=\\s*'([^']+)';");
                    if (dialogItemBean != null) {
                        result.add(dialogItemBean);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logInfo("getPlayUrl error", e.getMessage());
        }
        return null;
    }


    private DialogItemBean extractDialogItemBean(String html, String... regexes) {
        for (String regex : regexes) {
            DialogItemBean bean = regexPlayUrl(regex, html);
            if (bean != null) {
                return bean;
            }
        }
        return null;
    }

    /**
     * 获取播放地址
     * @param regex
     * @param content
     * @return
     */
    private DialogItemBean regexPlayUrl(String regex, String content) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String playUrl = matcher.group(1);
            LogUtil.logInfo("视频播放地址", playUrl);
            return new DialogItemBean(playUrl, playUrl.contains("m3u8") ? M3U8 : MP4);
        }
        LogUtil.logInfo("regex not found", regex);
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

    @Override
    public DomainDataBean parserDomain(String source) {
        try {
            Document document = Jsoup.parse(source);
            Elements aElements = document.select("ul li a");
            List<DomainDataBean.Domain> domainList = new ArrayList<>();
            for (Element a : aElements) {
                String title = a.text();
                String href = a.attr("href");
                if (!Utils.isNullOrEmpty(href)) {
                    domainList.add(new DomainDataBean.Domain(title, href));
                }
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
