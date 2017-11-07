package com.chat.dao;

import android.os.Handler;
import android.os.Message;

import java.util.List;

public abstract class ObjectDao {

    private Handler handler;

    public ObjectDao(Handler handler) {
        this.handler = handler;
    }

    public <K> void success(int whatForHandler, List<K> listData) {
        if (handler != null) {
            Message msg = handler.obtainMessage(whatForHandler, listData);
            handler.sendMessage(msg);
        }
    }

    public void success(int whatForHandler, Object key) {
        if (handler != null) {
            Message msg = handler.obtainMessage(whatForHandler, key);
            handler.sendMessage(msg);
        }
    }

    public void success(int whatForHandler) {
        if (handler != null)
            handler.sendEmptyMessage(whatForHandler);
    }

    public void error(int whatForHandler) {
        handler.sendEmptyMessage(whatForHandler);
    }

    public void deleteHandler() {
        this.handler = null;
    }
}
