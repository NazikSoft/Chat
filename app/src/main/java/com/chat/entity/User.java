package com.chat.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 22.09.2017.
 */

public class User {

    private String objectId;
    private String name;
    private String token;
    private int countNewPost;
    private long lastUpdate;
    private List<String> chats = new ArrayList<>();

    public User() {
    }

    public User(String name, String token, long lastUpdate) {
        this.name = name;
        this.token = token;
        this.lastUpdate = lastUpdate;
    }

    public User(String name, String password) {
        this.name = name;
    }

    public List<String> getChats() {
        return chats;
    }

    public void setChats(List<String> chats) {
        this.chats = chats;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public int getCountNewPost() {
        return countNewPost;
    }

    public void setCountNewPost(int countNewPost) {
        this.countNewPost = countNewPost;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "User{" +
                "objectId='" + objectId + '\'' +
                ", name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", countNewPost=" + countNewPost +
                ", lastUpdate=" + lastUpdate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (countNewPost != user.countNewPost) return false;
        if (lastUpdate != user.lastUpdate) return false;
        if (objectId != null ? !objectId.equals(user.objectId) : user.objectId != null)
            return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        return token != null ? token.equals(user.token) : user.token == null;
    }

    @Override
    public int hashCode() {
        int result = objectId != null ? objectId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + countNewPost;
        result = 31 * result + (int) (lastUpdate ^ (lastUpdate >>> 32));
        return result;
    }
}
