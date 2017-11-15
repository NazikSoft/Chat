package com.chat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.chat.ChatApp;
import com.chat.R;
import com.chat.entity.TempConfig;
import com.chat.ui.fragment.ChatFragment;
import com.chat.ui.fragment.ImagePreviewFragment;
import com.chat.utils.ChatConst;
import com.chat.utils.ChatUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView textTitle;
    private static final String TAG = ChatConst.TAG;
    private String urlImage;
//    private TempConfig temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        createMenu();
        toFragment();
    }

    public static void setTitle(Activity activity, String title) {
        ((TextView) activity.findViewById(R.id.toolbar_title)).setText(title);
    }

    public static String getTitle(Activity activity) {
        return ((TextView) activity.findViewById(R.id.toolbar_title)).getText().toString();
    }

    public void createMenu() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setContentInsetsAbsolute(160, 160);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toFragment() {
        String chatRoomId = getIntent().getStringExtra(ChatConst.EXTRA_CHAT_ID);
        Fragment fragment = ChatFragment.newInstance(chatRoomId);
        ChatUtil.changeFragmentTo(this,fragment,"main");
//        Fragment fragment = null;
//        switch (temp.getFragmentPosition()) {
//            case 0:
//                String chatRoomId = getIntent().getStringExtra(ChatConst.EXTRA_CHAT_ID);
//                fragment = ChatFragment.newInstance(chatRoomId, listener);
//                break;
//            case 1:
//                fragment = ImagePreviewFragment.newInstance(temp.getCompanionToken(), urlImage);
//                break;
//        }
//        ChatUtil.changeFragmentTo(this, fragment, "main");
}

//    private ChatFragment.OnClickListener listener = new ChatFragment.OnClickListener() {
//        @Override
//        public void onClick(String url) {
//            urlImage = url;
//            toFragment();
//        }
//    };

//    @Override
//    public void onBackPressed() {
//        if (temp.getFragmentPosition() > 0) {
//            temp.setFragmentPosition(temp.getFragmentPosition() - 1);
//            toFragment();
//        } else {
//            finish();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        temp = ((ChatApp) getApplication()).getTemp();
//        toFragment();
//    }
}
