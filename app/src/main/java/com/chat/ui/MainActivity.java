package com.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.chat.R;
import com.chat.adapter.ChatRoomAdapter;
import com.chat.dao.net.UserDao;
//import com.chat.chatDao.local.ChatRealm;
import com.chat.entity.ChatRoom;
import com.chat.ui.fragment.ChatFragment;
import com.chat.ui.fragment.UserChatRoomsFragment;
import com.chat.utils.ChatConst;
import com.chat.utils.ChatUtil;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.containerFragment)
    FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        // if user not logged start LoginActivity
        String userId = UserDao.getCurrentUserId();
        if (userId.equals("")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        Intent intent = getIntent();
        if (intent.hasExtra(ChatConst.EXTRA_CHAT_ID)){
            Fragment fragment = ChatFragment.newInstance(intent.getStringExtra(ChatConst.EXTRA_CHAT_ID));
            ChatUtil.changeFragmentTo(this, fragment, "chatFragment");
        }else {
            ChatUtil.changeFragmentTo(this, new UserChatRoomsFragment(), "main");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getFragmentManager().getBackStackEntryCount()<1){
            finish();
        }
    }
}
