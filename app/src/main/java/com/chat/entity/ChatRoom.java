package com.chat.entity;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nazar on 31.10.17.
 */

public class ChatRoom {
    private String id;
    private String title;
    private Message lastMessage;
    private List<Message> messages = new ArrayList<>();
    private HashMap<String,Integer> userReadMessageCount = new HashMap<String, Integer>();

    public ChatRoom() {
    }

    public ChatRoom(String id, String title, Message lastMessage, List<Message> messages) {
        this.id = id;
        this.title = title;
        this.lastMessage = lastMessage;
        this.messages = messages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

//    @Exclude
    public List<Message> getMessages() {
        return messages;
    }

//    @Exclude
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Map<String, Integer> getUserReadMessageCount() {
        return userReadMessageCount;
    }

    public void setUserReadMessageCount(HashMap<String, Integer> userReadMessageCount) {
        this.userReadMessageCount = userReadMessageCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoom that = (ChatRoom) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (lastMessage != null ? !lastMessage.equals(that.lastMessage) : that.lastMessage != null)
            return false;
        return messages != null ? messages.equals(that.messages) : that.messages == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (lastMessage != null ? lastMessage.hashCode() : 0);
        result = 31 * result + (messages != null ? messages.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", messages=" + messages +
                '}';
    }
}
