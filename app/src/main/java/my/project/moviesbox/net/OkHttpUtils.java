package my.project.moviesbox.net;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import my.project.moviesbox.parser.config.ParserInterfaceFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
  * @包名: my.project.moviesbox.net
  * @类名: OkHttpUtils
  * @描述: OkHttp工具类
  * @作者: Li Z
  * @日期: 2024/2/3 19:49
  * @版本: 1.0
 */
public class OkHttpUtils {
    private static OkHttpUtils okHttpUtils = null;
    private static OkHttpClient okHttpClient = null;
    private final static int connectTimeout = 10;
    private final static int readTimeout = 20;
    private final static HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private OkHttpUtils() {
    }

    public static OkHttpUtils getInstance() {
        if (okHttpUtils == null) {
            synchronized (OkHttpUtils.class) {
                if(okHttpUtils == null){
                    okHttpUtils = new OkHttpUtils();
                }
            }
        }
        return okHttpUtils;
    }

    /**
     * 单例模式
     * 封装okhttp
     * synchronized同步方法
     * @return
     */
    private static synchronized OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
                    .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                    /*.cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                            cookieStore.put(httpUrl.host(), list);
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                            List<Cookie> cookies = cookieStore.get(httpUrl.host());
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    })*/
                    .build();
        }
        return okHttpClient;
    }

    /**
     * get请求
     * @param url
     * @param callback
     */
    public void doGet(String url, Callback callback) {
        Request request;
        Headers.Builder builder = new Headers.Builder();
        Map<String,String> headerMap = ParserInterfaceFactory.getParserInterface().requestHeaders();
        if (headerMap != null && headerMap.size() > 0) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.add(key, value);
            }
        }
        request =new Request.Builder()
                .url(url)
                .headers(builder.build())
                .get()
                .build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(callback);
    }

    public void doGet(String url, Headers headers, Callback callback) {
        Request request;
        request =new Request.Builder()
                .url(url)
                .headers(headers)
                .get()
                .build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(callback);
    }

    /**
     * post请求
     * @param url
     * @param body
     * @param callback
     */
    public void doPost(String url, FormBody body, Callback callback) {
        Headers.Builder builder = new Headers.Builder();
        Map<String,String> headerList = ParserInterfaceFactory.getParserInterface().requestHeaders();
        if (headerList != null && headerList.size() > 0) {
            for (Map.Entry<String, String> entry : headerList.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.add(key, value);
            }
            builder.add(":path", url.replaceAll(ParserInterfaceFactory.getParserInterface().getDefaultDomain(), ""));
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(builder.build())
                .post(body)
                .build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(callback);
    }

    public void doPostDefault(String url, Headers headers, FormBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(body)
                .build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(callback);
    }
}
