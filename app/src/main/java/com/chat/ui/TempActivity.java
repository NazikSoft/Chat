package com.chat.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.chat.R;
import com.chat.adapter.UserAdapter;
import com.chat.dao.net.ChatDao;
import com.chat.dao.net.UserDao;
import com.chat.entity.ChatRoom;
import com.chat.entity.User;
import com.chat.utils.ChatConst;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TempActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    // Firebase instance variables
    private UserAdapter mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ChatDao chatDao;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        ButterKnife.bind(this);

        initHandler();
        chatDao = new ChatDao(handler);

        // create adapter
        SnapshotParser<User> parser = new SnapshotParser<User>() {
            @Override
            public User parseSnapshot(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                return user;
            }
        };
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child(ChatConst.USER_DATABASE_PATH);
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(userRef, parser)
                        .build();
        mFirebaseAdapter = new UserAdapter(this, options, new UserAdapter.OnUserClickListener() {
            @Override
            public void oClick(User user) {
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setTitle("chat with " + user.getName());
                // set read count 0
                Map<String,Integer> readMessage = chatRoom.getUserReadMessageCount();
                readMessage.put(UserDao.getCurrentUserId(), 0);
                readMessage.put(user.getObjectId(), 0);
                // create ChatRoom in DB
                chatDao.createChatRoomWith(user, chatRoom);
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(this);

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(mFirebaseAdapter);
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ChatConst.HANDLER_RESULT_OK:
                        Intent intent = new Intent(TempActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case ChatConst.HANDLER_RESULT_ERR:
                        Toast.makeText(TempActivity.this, "Connection error", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }
}
