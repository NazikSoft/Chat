package com.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.chat.R;
import com.chat.adapter.UserAdapter;
import com.chat.dao.net.ChatDao;
import com.chat.dao.net.UserDao;
//import com.chat.userDao.local.ChatRealm;
import com.chat.entity.Chat;
import com.chat.entity.User;
import com.chat.utils.ChatConst;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "log_main";//ChatConst.TAG;
    private ChatDao chatDao;
    private UserDao userDao;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private String objectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_user);
        userDao = new UserDao(handler);
        chatDao = new ChatDao(handler);
    }


    private void createAdapter(List<User> list) {
        adapter = new UserAdapter(list, handler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ChatConst.HANDLER_USERS_LIST:
                    List<User> list = (List<User>) msg.obj;
                    createAdapter(list);
                    break;
                case ChatConst.HANDLER_USER_OBJ:
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("token", (String) msg.obj);
                    startActivity(intent);
                    break;
                case ChatConst.HANDLER_CHAT_LIST:
                    List<Chat> list1 = (List<Chat>) msg.obj;
                    if (list1.size() > 0){
                        adapter.setPostsCount(list1);
                        objectId = list1.get(list1.size() - 1).getObjectId();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        userDao.readAll();
        chatDao.readAllByObjectId(objectId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
