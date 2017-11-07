package com.chat.ui.fragment;
/**
 * Created by Sergey on 03.03.2017.
 */

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chat.R;
import com.chat.adapter.ImageAdapter;
import com.chat.dao.net.ChatDao;
import com.chat.entity.Chat;
import com.chat.utils.ChatConst;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewFragment extends Fragment {

    private ViewPager viewPager;
    private ImageAdapter adapter;
    private int positionSelected;
    private String url;
    private View view;
    private boolean isDelete;
    private ImageAdapter.OnClickListener listener;
    private String compToken;
    private ChatDao dao;
    private String objectId;

    public static ImagePreviewFragment newInstance(String compToken, String url) {
        ImagePreviewFragment frg = new ImagePreviewFragment();
        frg.url = url;
        frg.compToken = compToken;
        return frg;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_image_preview, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.pagePreview);
        viewPager.setBackgroundColor(getResources().getColor(R.color.navigationBarColor));
        dao = new ChatDao(handler);
        dao.readAllByToken(compToken, objectId);
        return view;
    }

    private void createAdapter(List<String> listPath) {
        adapter = new ImageAdapter(getActivity(), listPath, listener, true, isDelete);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(positionSelected);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ChatConst.HANDLER_CHAT_LIST:
                    List<Chat> list = (List<Chat>) msg.obj;
                    objectId = list.get(list.size() - 1).getObjectId();
                    createAdapter(getListPath(list));
                    break;
            }
        }
    };

    private List<String> getListPath(List<Chat> list) {
        List<String> listPath = new ArrayList<>();
        int i = 0;
        for (Chat c : list) {
            if (c.getUrlFile() != null) {
                listPath.add(c.getUrlFile());
                if (c.getUrlFile().equals(url))
                    positionSelected = i;
                i++;
            }
        }
        Log.i("log_tag", "position: " + positionSelected);
        return listPath;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }
}
