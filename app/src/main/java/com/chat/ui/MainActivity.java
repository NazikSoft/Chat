package com.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.chat.R;
import com.chat.adapter.UserAdapter;
import com.chat.dao.net.ChatDao;
import com.chat.dao.net.UserDao;
//import com.chat.chatDao.local.ChatRealm;
import com.chat.entity.ChatRoom;
import com.chat.utils.ChatConst;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "log_main";//ChatConst.TAG;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private String objectId;

    private ChatDao chatDao;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_user);

        initHandler();
        chatDao = new ChatDao(handler);

        String userId = UserDao.getCurrentUserId();
        if (userId.equals("")) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        chatDao.getChatroomByUserId(userId);
    }


    private void createAdapter(List<ChatRoom> list) {
        adapter = new UserAdapter(list, handler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ChatConst.HANDLER_RESULT_OK:
                        List<ChatRoom> listOfChatRooms = new ArrayList<>();
                        listOfChatRooms = (List<ChatRoom>) msg.obj;
                        createAdapter(listOfChatRooms);
                        break;
                    case ChatConst.HANDLER_RESULT_ERR:
                        Toast.makeText(MainActivity.this, "Connection error", Toast.LENGTH_LONG).show();
                        break;

                        // click recycler view list item
                    case ChatConst.HANDLER_CLICK_RECYCLER_ITEM:
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                }
            }
        };
    }

//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case ChatConst.HANDLER_USERS_LIST:
//                    List<User> list = (List<User>) msg.obj;
//                    createAdapter(list);
//                    break;
//                case ChatConst.HANDLER_CLICK_RECYCLER_ITEM:
//                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("token", (String) msg.obj);
//                    startActivity(intent);
//                    break;
//                case ChatConst.HANDLER_CHAT_LIST:
//                    List<Chat> list1 = (List<Chat>) msg.obj;
//                    if (list1.size() > 0){
//                        adapter.getPostsCount(list1);
//                        objectId = list1.get(list1.size() - 1).getObjectId();
//                    }
//                    break;
//            }
//        }
//    };
//    @Override
//    protected void onResume() {
//        super.onResume();
//        chatDao.readAll();
//        chatDao.readAllByObjectId(objectId);
//    }
}
