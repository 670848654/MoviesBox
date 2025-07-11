package my.project.moviesbox.model;

import static my.project.moviesbox.parser.config.SourceEnum.SourceIndexEnum.fromIndex;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import my.project.moviesbox.R;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.config.SourceEnum;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.utils.SharedPreferencesUtils;
import my.project.moviesbox.utils.Utils;
import okhttp3.Response;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2023/12/30 22:31
 */
public class BaseModel {
    protected static ParserInterface parserInterface;
    protected static String charsetName;
    protected static SourceEnum.SourceIndexEnum sourceEnum;

    static {
        parserInterface = ParserInterfaceFactory.getParserInterface();
        charsetName = Utils.isNullOrEmpty(parserInterface.setCharset()) ? "UTF-8" : parserInterface.setCharset();
        int interfaceSource = parserInterface.getSource();
        sourceEnum = fromIndex(interfaceSource);
    }
    /**
     * 解码方式
     * @param response
     * @return
     * @throws IOException
     */
    protected String getBody(Response response) throws IOException {
        return new String(response.body().bytes(), charsetName);
    }

    /**
     * 对中文编码
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    protected String encodeUrl(String url) throws UnsupportedEncodingException {
        String resultURL = "";
        for (int i = 0; i < url.length(); i++) {
            char charAt = url.charAt(i);
            if (isChineseChar(charAt)) {
                String encode = URLEncoder.encode(charAt+"","GB2312");
                resultURL+=encode;
            }else {
                resultURL+=charAt;
            }
        }
        return resultURL;
    }

    /**
     * 是否是中文
     * @param c
     * @return
     */
    private boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    /**
     * 获取访问地址
     * @param url
     * @return
     */
    protected String getHttpUrl(String url) {
        if (!url.startsWith("http"))
            url = ParserInterfaceFactory.getParserInterface().getDefaultDomain() + url;
        return url;
    }

    /**
     * 解析出错文本
     * @param response
     * @param source
     * @return
     */
    protected String parserErrorMsg(Response response, String source) {
        String title = Jsoup.parse(source).getElementsByTag("title").text();
        StringBuilder resultTitle = new StringBuilder();
        if (!Utils.isNullOrEmpty(title)) {
            if (title.toUpperCase().contains("JUST A MOMENT") || source.contains("cf_clearance")) {
                resultTitle.append(title).append("[站点疑似启用了Cloudflare]");
            } else {
                resultTitle.append("Title:").append(title);
            }
        } else if (source.contains("长亭雷池")) {
            resultTitle.append("[站点疑似启用了Cloudflare]");
        } else if (response.networkResponse() != null) {
            String msg = response.networkResponse().toString();
            if (msg.toUpperCase().contains("JUST A MOMENT") || msg.contains("cf_clearance")) {
                resultTitle.append("[站点疑似启用了Cloudflare]");
            }
        }

        String networkResponse = response.networkResponse() != null ? response.networkResponse().toString() : "";
        return String.format(Utils.getString(R.string.parsingErrorContent), networkResponse, title);
    }

    public void register() {
        if (SharedPreferencesUtils.getByPassCF() && !EventBus.getDefault().isRegistered(this)) {
            // 只有开启绕过CF验证才绑定EventBus
            LogUtil.logInfo(this.getClass().getName(), "EventBus register...");
            EventBus.getDefault().register(this);
        }
    }

    public void unregister() {
        if (SharedPreferencesUtils.getByPassCF() && EventBus.getDefault().isRegistered(this)) {
            // 只有开启绕过CF验证才解绑EventBus
            LogUtil.logInfo(this.getClass().getName(), "EventBus unregister...");
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * 文本长度超过500时截取
     * @param str
     * @return
     */
    protected static String truncateString(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() <= 500) {
            return str;
        } else {
            return str.substring(0, 500) + "...";
        }
    }
}
