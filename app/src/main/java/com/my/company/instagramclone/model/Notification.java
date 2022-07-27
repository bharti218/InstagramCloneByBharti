package com.my.company.instagramclone.model;

public class Notification {
    private String publisherId;
    private String text;
    private String postId;
    private boolean isPost;

    public Notification() {
    }

    public Notification(String publisherId, String text, String postId, boolean isPost) {
        this.publisherId = publisherId;
        this.text = text;
        this.postId = postId;
        this.isPost = isPost;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }
}
