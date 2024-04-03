package my.project.moviesbox.config;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import my.project.moviesbox.bean.SourceBean;

/**
  * @包名: my.project.moviesbox.config
  * @类名: OpenSourceEnum
  * @描述: 开源相关枚举
  * @作者: Li Z
  * @日期: 2024/1/24 13:39
  * @版本: 1.0
 */
@Getter
@AllArgsConstructor
public enum OpenSourceEnum {
    JSOUP("jsoup", "jhy", "jsoup: Java HTML Parser, with best of DOM, CSS, and jquery", "https://github.com/jhy/jsoup"),
    CONSCRYPT("conscrypt", "google", "Conscrypt is a Java Security Provider that implements parts of the Java Cryptography Extension and Java Secure Socket Extension.", "https://github.com/google/conscrypt"),
    MATERIAL_COMPONENTS_ANDROID("material-components-android", "material-components", "Modular and customizable Material Design UI components for Android", "https://github.com/material-components/material-components-android"),
    BASE_RECYCLER_VIEW("BaseRecyclerViewAdapterHelper", "CymChad", "BRVAH:Powerful and flexible RecyclerAdapter", "https://github.com/CymChad/BaseRecyclerViewAdapterHelper"),
    GLIDE("Glide", "bumptech", "An image loading and caching library for Android focused on smooth scrolling", "https://github.com/bumptech/glide"),
    GLIDE_TRANSFORMATIONS("glide-transformations", "wasabeef", "An Android transformation library providing a variety of image transformations for Glide.", "https://github.com/wasabeef/glide-transformations"),
    PERMISSION_X("PermissionX", "guolindev", "An open source Android library that makes handling runtime permissions extremely easy.", "https://github.com/guolindev/PermissionX"),
    JZ_VIDEO("JZVideo", "Jzvd", "高度自定义的安卓视频框架 MediaPlayer exoplayer ijkplayer ffmpeg", "https://github.com/Jzvd/JZVideo"),
    EXO_PLAYER("ExoPlayer", "google", "An extensible media player for Android", "https://github.com/google/ExoPlayer"),
    IJK_PLAYER("Ijkplayer", "bilibili", "Android/iOS video player based on FFmpeg n3.4, with MediaCodec, VideoToolbox support.", "https://github.com/bilibili/ijkplayer"),
    BUTTERKNIFE("butterknife", "JakeWharton", "Bind Android views and callbacks to fields and methods.", "https://github.com/JakeWharton/butterknife"),
    OKHTTP("okhttp", "square", "An HTTP+HTTP/2 client for Android and Java applications.", "https://github.com/square/okhttp"),
    ANDROID_UPNP_DEMO("AndroidUPnPDemo", "zaneCC", "android 投屏", "https://github.com/zaneCC/AndroidUPnPDemo"),
    CLING("cling", "4thline", "UPnP/DLNA library for Java and Android", "https://github.com/4thline/cling"),
    EXPANDABLE_TEXT_VIEW("ExpandableTextView", "MZCretin", "实现类似微博内容，@用户，链接高亮，@用户和链接可点击跳转，可展开和收回的TextView", "https://github.com/MZCretin/ExpandableTextView"),
    FASTJSON("fastjson", "alibaba", "A fast JSON parser/generator for Java.", "https://github.com/alibaba/fastjson"),
    EVENTBUS("EventBus", "greenrobot", "Event bus for Android and Java that simplifies communication between Activities, Fragments, Threads, Services, etc. Less code, better quality.", "https://github.com/greenrobot/EventBus"),
    ARIA("Aria", "AriaLyy", "下载可以很简单，aria.laoyuyu.me/aria_doc/", "https://github.com/AriaLyy/Aria"),
    NANOHTTPD("nanohttpd", "NanoHttpd", "Tiny, easily embeddable HTTP server in Java.", "https://github.com/NanoHttpd/nanohttpd"),
    DANMAKU_FLAME_MASTER("DanmakuFlameMaster", "bilibili", "Android开源弹幕引擎·烈焰弹幕使 ～", "https://github.com/bilibili/DanmakuFlameMaster"),
    MOBILE_FFMPEG("mobile-ffmpeg", "tanersener", "FFmpeg for Android, iOS and tvOS. Not maintained anymore. Superseded by FFmpegKit.", "https://github.com/tanersener/mobile-ffmpeg");

    private String title;
    private String author;
    private String introduction;
    private String url;

    /**
     * @方法名称: getSourceList
     * @方法描述: 封装实体列表
     * @日期: 2024/1/24 13:42
     * @作者: Li Z
     *
     * @返回:
     */
    public static List<SourceBean> getSourceList() {
        List<SourceBean> sourceBeans = new ArrayList<>();
        for (OpenSourceEnum openSourceEnum : OpenSourceEnum.values()) {
            sourceBeans.add(new SourceBean(openSourceEnum.getTitle(), openSourceEnum.getAuthor(), openSourceEnum.getIntroduction(), openSourceEnum.getUrl()));
        }
        return sourceBeans;
    }
}
