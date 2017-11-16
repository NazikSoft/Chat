package com.chat;

import android.app.Application;

import com.chat.entity.TempConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
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
        Fresco.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
