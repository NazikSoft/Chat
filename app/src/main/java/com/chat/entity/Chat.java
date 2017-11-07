package com.chat.entity;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 21.09.2017.
 */

public class Chat {//extends RealmObject //extends BaseDaoEnabled
    @SerializedName("objectId")
    @Expose
//    @PrimaryKey
//    @Required
    private String objectId;

    @SerializedName("currentToken")
    @Expose
    private String currentToken;
    @SerializedName("companionToken")
    @Expose
    private String companionToken;
    @SerializedName("companionName")
    @Expose
    private String companionName;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("urlFile")
    @Expose
    private String urlFile;
    @SerializedName("changesCount")
    @Expose
    private String changesCount;
    @SerializedName("syncSend")
    @Expose
    private boolean syncSend;
    @SerializedName("isRead")
    @Expose
    private boolean isRead;
    @SerializedName("lastUpdate")
    @Expose
    private long lastUpdate;

    public Chat() {
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(String currentToken) {
        this.currentToken = currentToken;
    }

    public String getCompanionToken() {
        return companionToken;
    }

    public void setCompanionToken(String companionToken) {
        this.companionToken = companionToken;
    }

    public String getCompanionName() {
        return companionName;
    }

    public void setCompanionName(String companionName) {
        this.companionName = companionName;
    }

    public String getChangesCount() {
        return changesCount;
    }

    public void setChangesCount(String changesCount) {
        this.changesCount = changesCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public boolean isSyncSend() {
        return syncSend;
    }

    public void setSyncSend(boolean syncSend) {
        this.syncSend = syncSend;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean equalsTokens() {
        return equalsTokens(null);
    }

    public boolean equalsTokens(String... tokens) {
        String currentToken = FirebaseInstanceId.getInstance().getToken();
        if ((getCompanionToken().equals(currentToken) || getCurrentToken().equals(currentToken))) {
            if (tokens == null) {
                return true;
            } else {
                for (String t : tokens) {
                    if (getCompanionToken().equals(t) || getCurrentToken().equals(t))
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "objectId='" + objectId + '\'' +
                ", currentToken='" + currentToken + '\'' +
                ", companionToken='" + companionToken + '\'' +
                ", companionName='" + companionName + '\'' +
                ", message='" + message + '\'' +
                ", changesCount='" + changesCount + '\'' +
                ", syncSend=" + syncSend +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}