package com.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.test.mock.MockApplication;
import android.widget.Toast;

import com.chat.R;
import com.chat.adapter.ChatRecyclerAdapter;
import com.chat.adapter.UserAdapter;
import com.chat.adapter.UserAdapter2;
import com.chat.dao.net.ChatDao;
import com.chat.dao.net.UserDao;
//import com.chat.chatDao.local.ChatRealm;
import com.chat.entity.ChatRoom;
import com.chat.utils.ChatConst;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter2 adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_user);

        String userId = UserDao.getCurrentUserId();
        if (userId.equals("")) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        initAdapter();
    }

    private void initAdapter() {
        // create parser
        SnapshotParser<ChatRoom> parser = new SnapshotParser<ChatRoom>() {
            @Override
            public ChatRoom parseSnapshot(DataSnapshot dataSnapshot) {
                return dataSnapshot.getValue(ChatRoom.class);
            }
        };
        // get database reference for ChatRooms
        DatabaseReference chatDatabase = FirebaseDatabase.getInstance().getReference()
                .child(ChatConst.CHAT_DATABASE_PATH);

        // get Query of user ChatRoom
        Query chatRoomIdQuery = FirebaseDatabase.getInstance().getReference()
                .child(ChatConst.USER_DATABASE_PATH)
                .child(UserDao.getCurrentUserId())
                .child(ChatConst.COLUMN_CHAT_ROOMS)
                .orderByKey();

        // create options
        FirebaseRecyclerOptions<ChatRoom> options =
                new FirebaseRecyclerOptions.Builder<ChatRoom>()
                        .setIndexedQuery(chatRoomIdQuery, chatDatabase, parser)
                        .build();

        // init OnClickListener
        UserAdapter2.OnChatClickListener listener = new UserAdapter2.OnChatClickListener() {
            @Override
            public void onClick(ChatRoom chatRoom) {
                if (chatRoom == null) {
                    Toast.makeText(MainActivity.this, R.string.text_error, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ChatConst.EXTRA_CHAT_ID, chatRoom.getId());
                startActivity(intent);
            }
        };

        // init adapter
        adapter = new UserAdapter2(this, options, listener);

        final LinearLayoutManager manager = new LinearLayoutManager(this);
//        mLinearLayoutManager.setStackFromEnd(true);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }


//    private void initHandler() {
//        handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                switch (msg.what) {
//                    case ChatConst.HANDLER_RESULT_OK:
//                        List<ChatRoom> listOfChatRooms = new ArrayList<>();
//                        listOfChatRooms = (List<ChatRoom>) msg.obj;
//
//                        if (listOfChatRooms.size() == 0) {
//                            Toast.makeText(MainActivity.this, "Список чатов пуст", Toast.LENGTH_LONG).show();
//                        }
//                        break;
//                    case ChatConst.HANDLER_RESULT_ERR:
//                        Toast.makeText(MainActivity.this, "Connection error", Toast.LENGTH_LONG).show();
//                        break;
//
//                    // click recycler view list item
//                    case ChatConst.HANDLER_CLICK_RECYCLER_ITEM:
//                        ChatRoom chatRoom = (ChatRoom) msg.obj;
//                        if (chatRoom == null) {
//                            Toast.makeText(MainActivity.this, R.string.text_error, Toast.LENGTH_LONG).show();
//                        }
//                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra(ChatConst.EXTRA_CHAT_ID, chatRoom.getId());
//                        startActivity(intent);
//                        break;
//                }
//            }
//        };
//    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startListening();
    }

    @Override
    protected void onPause() {
        adapter.stopListening();
        super.onPause();
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
