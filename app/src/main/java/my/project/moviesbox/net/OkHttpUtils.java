package my.project.moviesbox.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import my.project.moviesbox.application.App;
import my.project.moviesbox.parser.LogUtil;
import my.project.moviesbox.parser.parserService.ParserInterface;
import my.project.moviesbox.parser.parserService.ParserInterfaceFactory;
import my.project.moviesbox.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private static ParserInterface parserInterface;

    static {
        parserInterface = ParserInterfaceFactory.getParserInterface();
    }

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
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(connectTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
                    .hostnameVerifier(SSLSocketClient.getHostnameVerifier());
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    /**
     * get请求
     * @param url
     * @param callback
     */
    public void doGet(String url, Callback callback) {
        LogUtil.logInfo("GET请求", url);
        Request request;
        Headers.Builder builder = new Headers.Builder();
        Map<String,String> headerMap = parserInterface.requestHeaders();
        if (headerMap != null && headerMap.size() > 0) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.add(key, value);
            }
        }
        Map<String,String> cookie = App.getCookies();
        if (!Utils.isNullOrEmpty(cookie)) {
            builder.add("Cookie", cookie.get("Cookie"));
        }
        request =new Request.Builder()
                .url(url)
                .headers(builder.build())
                .get()
                .build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(callback);
    }

    /**
     * get请求
     * @param url
     * @param headers
     * @param callback
     */
    public void doGet(String url, Headers headers, Callback callback) {
        LogUtil.logInfo("GET请求", url);
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
     * 同步请求
     * @param url
     * @return
     * @throws IOException
     */
    public static String performSyncRequest(String url) throws IOException {
        LogUtil.logInfo("GET请求", url);
        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .build();
        // 发起同步请求
        try (Response response = getOkHttpClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 返回响应数据
                return response.body().string();
            } else {
                throw new IOException("Request failed with code: " + response.code());
            }
        }
    }

    public static String performSyncRequestAndHeader(String url) throws IOException {
        LogUtil.logInfo("GET请求", url);
        Headers.Builder builder = new Headers.Builder();
        Map<String,String> headerMap = parserInterface.requestHeaders();
        if (headerMap != null && headerMap.size() > 0) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.add(key, value);
            }
        }
        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .headers(builder.build())
                .build();
        // 发起同步请求
        try (Response response = getOkHttpClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 返回响应数据
                return response.body().string();
            } else {
                throw new IOException("Request failed with code: " + response.code());
            }
        }
    }

    public static String performSyncRequestAndHeaderTest(String url) throws IOException {
        Headers.Builder builder = new Headers.Builder();
        builder.add("Accept", "*/*");
        builder.add("Connection", "keep-alive");
        builder.add("keep-alive", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.60");
        builder.add("Referer", "https://www.libvio.link");
        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .headers(builder.build())
                .build();
        // 发起同步请求
        try (Response response = getOkHttpClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                String contentType = response.header("Content-Type");
                Charset charset = Charset.forName("UTF-8"); // 默认使用 UTF-8

                if (contentType != null) {
                    MediaType mediaType = MediaType.parse(contentType);
                    if (mediaType != null) {
                        Charset responseCharset = mediaType.charset();
                        if (responseCharset != null) {
                            charset = responseCharset;
                        }
                    }
                }
                // 返回响应数据
                byte[] responseBodyBytes = response.body().bytes();
                return new String(responseBodyBytes, charset);
            } else {
                throw new IOException("Request failed with code: " + response.code());
            }
        }
    }

    /**
     * 同步请求
     * @param url
     * @return
     * @throws IOException
     */
    public static InputStream performSyncRequestIs(String url) throws IOException {
        LogUtil.logInfo("GET请求", url);
        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .build();
        // 发起同步请求
        try (Response response = getOkHttpClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应数据流
                try (InputStream inputStream = response.body().byteStream()) {
                    // 将数据读入 ByteArrayOutputStream
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                    byteArrayOutputStream.flush();

                    // 将 ByteArrayOutputStream 转为 ByteArrayInputStream 返回
                    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                }
            } else {
                throw new IOException("Request failed with code: " + response.code());
            }
        }
    }

    /**
     * post请求
     * @param url
     * @param body
     * @param callback
     */
    public void doPost(String url, FormBody body, Callback callback) {
        LogUtil.logInfo("POST请求", url);
        Headers.Builder builder = new Headers.Builder();
        Map<String,String> headerList = parserInterface.requestHeaders();
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

    /**
     * post请求
     * @param url
     * @param headers
     * @param body
     * @param callback
     */
    public void doPostDefault(String url, Headers headers, FormBody body, Callback callback) {
        LogUtil.logInfo("POST请求", url);
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(body)
                .build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(callback);
    }

    public Response doPostDefault(String url, Headers headers, FormBody body) throws IOException {
        LogUtil.logInfo("POST请求", url);
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(body)
                .build();
        Call call = getOkHttpClient().newCall(request);
        return call.execute();
    }

    /**
     * 发送同步 POST 请求
     *
     * @param url     请求的 URL
     * @param json    要发送的 JSON 字符串
     * @return        响应的字符串
     * @throws IOException  请求失败时抛出的异常
     */
    public String post(String url, String json) throws IOException {
        LogUtil.logInfo("POST请求", url);
        // 创建 RequestBody，指定媒体类型为 JSON
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // 执行请求并获取响应
        try (Response response = getOkHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 返回响应体的字符串
            return response.body().string();
        }
    }
}
