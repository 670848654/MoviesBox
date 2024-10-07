package my.project.moviesbox.parser.bean.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

import lombok.Getter;

/**
 * @author Li
 * @version 1.0
 * @description: 注释
 * @date 2024/7/22 9:32
 */
@Root(name = "rss", strict = false)
@Getter
public class SilisiliRssFeed {

    @Element(name = "channel")
    private Channel channel;

    @Root(name = "channel", strict = false)
    @Getter
    public static class Channel {
        @Element(name = "title")
        private String title;

        @Element(name = "description")
        private String description;

        @Element(name = "link")
        private String link;

        @Element(name = "language")
        private String language;

        @Element(name = "docs")
        private String docs;

        @Element(name = "generator")
        private String generator;

        @Element(name = "image")
        private Image image;

        @ElementList(inline = true, entry = "item")
        private List<Item> items;
    }

    @Root(name = "image", strict = false)
    @Getter
    public static class Image {
        @Element(name = "url")
        private String url;
    }

    @Root(name = "item", strict = false)
    @Getter
    public static class Item {
        @Element(name = "title")
        private String title;

        @Element(name = "link")
        private String link;

        @Element(name = "description", required = false)
        private String description;

        @Element(name = "pubDate", required = false)
        private String pubDate;
    }
}
