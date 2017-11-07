package com.chat;

import android.app.Application;

import com.chat.entity.TempConfig;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by m on 22.09.2017.
 */

public class ChatApp extends Application {
    private TempConfig temp;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public TempConfig getTemp() {
        if (temp == null)
            temp = new TempConfig();

        return temp;
    }
}
