package com.chat.ui.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chat.ChatApp;
import com.chat.R;
import com.chat.adapter.ChatAdapter;
import com.chat.api.Manager;
import com.chat.dao.net.FileUploadDao;
import com.chat.entity.Chat;
import com.chat.entity.Request;
import com.chat.entity.TempConfig;
import com.chat.entity.User;
import com.chat.fcm.MyFirebaseMessagingService;
import com.chat.ui.ChatActivity;
import com.chat.utils.ChatConst;
import com.chat.utils.ChatUtil;
import com.chat.utils.PermissionUtil;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ChatFragment extends Fragment {
    private static final String TAG = ChatConst.TAG;
    @BindView(R.id.textMsg)
    EditText textMsg;
    @BindView(R.id.recycler_view_chat)
    RecyclerView recyclerView;
    @BindView(R.id.addresses_confirm_root_view)
    RelativeLayout rootView;

    private ChatAdapter adapter;
    private Manager managerApi;
    private User currentUser;
    private User companionUser;
    private static Uri imageUri;
    private String objectId;
    private int heightDiff;
    private int indexPermission;
    private String tokenCompanion;
    private Intent intent;
    private TempConfig temp;
    private OnClickListener onClickListener;

    public static ChatFragment newInstance(Intent intent, OnClickListener onClickListener) {
        ChatFragment frg = new ChatFragment();
        frg.onClickListener = onClickListener;
        frg.intent = intent;
        return frg;
    }

    public interface OnClickListener {
        void onClick(String path);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        temp = ((ChatApp) getActivity().getApplication()).getTemp();
        managerApi = new Manager(handler);
        if (intent != null) {
            tokenCompanion = intent.getStringExtra("token");
            Log.i(TAG, "click user: " + tokenCompanion);
            managerApi.getUserDao().findUserAll(tokenCompanion);
        }
        keyboardSensor();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void keyboardSensor() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff2 = rootView.getRootView().getHeight() - rootView.getHeight();
                if (heightDiff != heightDiff2) {
                    if (adapter != null)
                        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
                }
                heightDiff = heightDiff2;
            }
        });
    }

    @OnClick({R.id.sendButton, R.id.imageGallery, R.id.imageCamera})
    public void submit(View view) {
        switch (view.getId()) {
            case R.id.sendButton:
                String msg = textMsg.getText().toString();
                prepareSend(msg, null);
                break;
            case R.id.imageGallery:
                indexPermission = 0;
                if (PermissionUtil.checkPermission(ChatFragment.this, PermissionUtil.PERMISSIONS[indexPermission]))
                    startAction();
                break;
            case R.id.imageCamera:
                indexPermission = 1;
                if (PermissionUtil.checkPermission(ChatFragment.this, PermissionUtil.PERMISSIONS[indexPermission]))
                    startAction();
                break;
        }
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, ChatConst.ACTION_SELECT_IMAGE);
    }

    private void openCamera() {
        ContentValues value = new ContentValues();
        value.put(MediaStore.Images.Media.TITLE, "IMG");
        value.put(MediaStore.Images.Media.DESCRIPTION, "Camera");
        imageUri = getActivity().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, ChatConst.ACTION_IMAGE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        String path = null;
        if (requestCode == ChatConst.ACTION_SELECT_IMAGE) {
            imageUri = data.getData();
        }
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(imageUri, filePathColumn, null, null, null);
        int columnIndex;
        if (cursor != null) {
            columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            cursor.moveToFirst();
            path = (cursor.getString(columnIndex));
            cursor.close();
        }
        if (path != null && path.length() > 0)
            prepareSend("", path);
        Log.i(TAG, "file uri: " + path);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ChatConst.HANDLER_CHAT_LIST:
                    List<Chat> list = (List<Chat>) msg.obj;
                    if (list.size() > 0)
                        objectId = list.get(list.size() - 1).getObjectId();
                    else
                        objectId=null;
                    addChatToAdapter(list);
                    break;
                case ChatConst.HANDLER_RESULT_COMPAMION_USER:
                    companionUser = (User) msg.obj;
                    temp.setCompanionToken(companionUser.getToken());
                    ChatActivity.setTitle(getActivity(), companionUser.getName());
                    break;
                case ChatConst.HANDLER_RESULT_CURRENT_USER:
                    currentUser = (User) msg.obj;
                    break;
                case ChatConst.HANDLER_RECEIVE_MSG:
                    Chat chat = (Chat) msg.obj;
                    Log.i(TAG, "readAll: " + chat);
                    if (companionUser.getToken().equals(chat.getCompanionToken()))
                        addChatToAdapter(chat);
                    break;
                case ChatConst.HANDLER_IMAGE_SAVE_OK:
                    sendFCM((Chat) msg.obj);
                    break;
                case ChatConst.RECYCLER_LIST_CLICKED:
                    temp.setFragmentPosition(1);
                    onClickListener.onClick((String)msg.obj );
                    break;
            }
        }
    };

    private void addChatToAdapter(final Chat chat) {
        addChatToAdapter(new ArrayList<Chat>() {{
            add(chat);
        }});
    }

    private void addChatToAdapter(List<Chat> list) {
        if (adapter != null) {
            adapter.addAllToAdapter(list);
        } else {
            createAdapter(list);
        }
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
    }

    private void createAdapter(List<Chat> list) {
        adapter = new ChatAdapter(getActivity(), list, handler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void prepareSend(String msg, String path) {
        if ((msg != null && msg.length() > 0) || (path != null && path.length() > 0)) {
            Chat chat = new Chat();
            chat.setMessage(msg);
            chat.setCurrentToken(companionUser.getToken());
            chat.setCompanionToken(currentUser.getToken());
            chat.setCompanionName(currentUser.getName());
            chat.setUrlFile(path);
            chat.setLastUpdate(new Date().getTime());
            managerApi.getChatDao().save(chat);

            if (path != null && path.length() > 0) {
                new FileUploadDao(handler).saveFile(chat);
            } else {
                sendFCM(chat);
                addChatToAdapter(chat);
            }
        }
    }

    private void sendFCM(Chat chat) {
        Request request = new Request();
        request.setTo(companionUser.getToken());
        request.getData().setMessage(ChatUtil.toJson(chat));
        managerApi.send(request);
        textMsg.setText(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE
                && PermissionUtil.isPermissionGranted(getActivity(), permissions[0])) {
            startAction();
        } else {
            if (PermissionUtil.checkShouldShowRequestPermission(this, permissions[0])) {
                Toast.makeText(getActivity(), "Permission deferred", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startAction() {

        switch (indexPermission) {
            case 0:
                openGallery();
                break;
            case 1:
                openCamera();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MyFirebaseMessagingService.setHandler(null);
        adapter = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        MyFirebaseMessagingService.setHandler(handler);
        managerApi.getChatDao().readAllByToken(tokenCompanion, objectId);
    }
}
