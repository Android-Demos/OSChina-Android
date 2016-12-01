package net.oschina.app.improve.bean.simple;

import java.io.Serializable;

/**
 * Created by JuQiu
 * on 16/6/16.
 * 相关推荐实体
 */
public class About implements Serializable {
    private long id;
    private String title;
    private String content;
    private int type;
    private String href;
    private int viewCount;
    private int commentCount;
    private int transmitCount;
    private String[] images;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTransmitCount() {
        return transmitCount;
    }

    public void setTransmitCount(int transmitCount) {
        this.transmitCount = transmitCount;
    }

    public boolean check() {
        return id > 0 && type >= 0;
    }
}