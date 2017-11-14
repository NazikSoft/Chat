package com.chat.fcm;

import android.os.Handler;
import android.util.Log;

import com.chat.dao.net.UserDao;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;



public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG ="log_tag " ;
    private UserDao userDao= new UserDao(new Handler());

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
        userDao.updateUserToken(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        //save to my server db
    }
}
