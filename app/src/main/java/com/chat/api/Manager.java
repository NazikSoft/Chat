package com.chat.api;

import android.os.Handler;
import android.util.Log;

import com.chat.dao.net.ChatDao;
import com.chat.dao.net.UserDao;
import com.chat.entity.Request;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by m on 20.09.2017.
 */

public class Manager {

    private static final String TAG = "log_tag";
    private static String apiKey = "key=AAAAOEg4f8Q:APA91bGZwWReIoORCrJuG_eGP1yck3vNbZzrqK6ItwuesCPclWgkklRdn4jtYmor7o3g94VIhfpKPl4gGVkiLUcKu2MqT-YXnRbaqpWFh1hwKKiM5QSLkL6TNtBg3mTRGkPO7aExMIuU";

    private final static String URL_FCM = "https://fcm.googleapis.com/";
    private Retrofit retrofit;
    private Handler handler;
    private ChatDao chatDao;
    private UserDao userDao;

    public Manager(Handler handler) {
        this.handler = handler;
        userDao = new UserDao(handler);
        chatDao=new ChatDao(handler);
    }

    private FcmApi getApi(String url) {
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(FcmApi.class);
    }

    public void send(Request request) {
        getApi(URL_FCM).sendMsg(apiKey, request).enqueue(new Callback<Request>() {
            @Override
            public void onResponse(Call<Request> call, Response<Request> response) {
                Log.i(TAG, "send code: " + response.code() + " body: " + response.body());
            }

            @Override
            public void onFailure(Call<Request> call, Throwable t) {
                Log.i(TAG, "send onFailure: " + t.getMessage());
            }
        });
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public ChatDao getChatDao() {
        return chatDao;
    }
}
