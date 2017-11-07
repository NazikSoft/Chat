package com.chat.api;

import com.chat.entity.Request;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by m on 22.09.2017.
 */

public interface FcmApi {

    @Headers({
            "Content-Type:application/json"
    })
    @POST("fcm/send")
    Call<Request> sendMsg(
            @Header("Authorization") String serverKey,
            @Body Request body);

}
