package app.matolaypal.com.rssreader;

/**
 * Represent a simple feed model (without setter/getter, because unnecessary now)
 */
class RssFeedModel {

    String title;
    String link;
    String description;

    RssFeedModel(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }
}
