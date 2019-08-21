package com.tryz.io.nrruboard;
import java.util.Date;

public class StoryPost {

    public String post_id;
    public String user_id;
    public String content;
    public Date timestamp;
    public String title;

    public StoryPost(){ }

    public StoryPost(String user_id, String content, Date timestamp,String title) {
        this.user_id = user_id;
        this.content = content;
        this.timestamp = timestamp;
        this.title = title;
    }
    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getTitle() {
        return title;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
