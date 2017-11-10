package com.chat.ui.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chat.ChatApp;
import com.chat.R;
import com.chat.adapter.ChatAdapter;
import com.chat.adapter.ChatRecyclerAdapter;
import com.chat.adapter.UserRecyclerAdapter;
import com.chat.api.Manager;
import com.chat.dao.net.ChatDao;
import com.chat.dao.net.FileUploadDao;
import com.chat.dao.net.UserDao;
import com.chat.entity.Chat;
import com.chat.entity.ChatRoom;
import com.chat.entity.Message;
import com.chat.entity.Request;
import com.chat.entity.TempConfig;
import com.chat.entity.User;
import com.chat.fcm.MyFirebaseMessagingService;
import com.chat.ui.ChatActivity;
import com.chat.utils.ChatConst;
import com.chat.utils.ChatUtil;
import com.chat.utils.PermissionUtil;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

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
    @BindView(R.id.sendButton)
    Button send;

    //    private ChatAdapter adapter;
    private ChatRecyclerAdapter adapter;
    private LinearLayoutManager mLinearLayoutManager;
    private Manager managerApi;
    private static String chatRoomId;
    private C
    private ChatDao chatDao;
    private Handler handler;


    private User currentUser;
    private User companionUser;
    private static Uri imageUri;
    private String objectId;
    private int heightDiff;
    private int indexPermission;
    private String tokenCompanion;
    private TempConfig temp;
    private OnClickListener onClickListener;

    public static ChatFragment newInstance(String chatRoomId, OnClickListener onClickListener) {
        ChatFragment frg = new ChatFragment();
        ChatFragment.chatRoomId = chatRoomId;
        frg.onClickListener = onClickListener;
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

        if (chatRoomId == null) {
//            tokenCompanion = intent.getStringExtra("token");
//            Log.i(TAG, "click user: " + tokenCompanion);
//            managerApi.getUserDao().findUserAll(tokenCompanion);
            chatRoomId = "";
            Toast.makeText(getActivity(), R.string.text_chat_load_error, Toast.LENGTH_LONG).show();
        }

        initAdapter();
        initEditText();
        initHandler();
        chatDao = new ChatDao(handler);
        keyboardSensor();
        return view;
    }

    private void initEditText() {
        textMsg.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ChatConst.DEFAULT_MSG_LENGTH_LIMIT)});
        textMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    send.setEnabled(true);
                } else {
                    send.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    private void initAdapter() {
        // create parser
        SnapshotParser<Message> parser = new SnapshotParser<Message>() {
            @Override
            public Message parseSnapshot(DataSnapshot dataSnapshot) {
                return dataSnapshot.getValue(Message.class);
            }
        };
        // get database reference for Message
        DatabaseReference messageReference = FirebaseDatabase.getInstance().getReference()
                .child(ChatConst.CHAT_DATABASE_PATH)
                .child(chatRoomId)
                .child(ChatConst.COLUMN_MESSAGES);
        // create options
        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(messageReference, parser)
                        .build();
        // init adapter
        adapter = new ChatRecyclerAdapter(getActivity(), options, new ChatRecyclerAdapter.OnImgMessageClickListener() {

            @Override
            public void oClick(String path) {
                temp.setFragmentPosition(1);
                onClickListener.onClick(path);
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
//        mLinearLayoutManager.setStackFromEnd(true);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
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

        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(adapter);

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
                prepareSendText(msg);
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

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
//        String path = null;

        if (requestCode == ChatConst.ACTION_SELECT_IMAGE) {
            imageUri = data.getData();
            prepareSendImg(imageUri);
        }
//        String[] filePathColumn = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getActivity().getContentResolver().query(imageUri, filePathColumn, null, null, null);
//        int columnIndex;
//        if (cursor != null) {
//            columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            cursor.moveToFirst();
//            path = (cursor.getString(columnIndex));
//            cursor.close();
//        }
//        if (path != null && path.length() > 0)
//            prepareSendImg(path);
//        Log.i(TAG, "file uri: " + path);
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
//                    case ChatConst.HANDLER_CHAT_LIST:
//                        List<Chat> list = (List<Chat>) msg.obj;
//                        if (list.size() > 0)
//                            objectId = list.get(list.size() - 1).getObjectId();
//                        else
//                            objectId = null;
//                        addChatToAdapter(list);
//                        break;
//                    case ChatConst.HANDLER_RESULT_COMPAMION_USER:
//                        companionUser = (User) msg.obj;
//                        temp.setCompanionToken(companionUser.getToken());
//                        ChatActivity.setTitle(getActivity(), companionUser.getName());
//                        break;
//                    case ChatConst.HANDLER_RESULT_CURRENT_USER:
//                        currentUser = (User) msg.obj;
//                        break;
//                    case ChatConst.HANDLER_RECEIVE_MSG:
//                        Chat chat = (Chat) msg.obj;
//                        Log.i(TAG, "readAll: " + chat);
//                        if (companionUser.getToken().equals(chat.getCompanionToken()))
//                            addChatToAdapter(chat);
//                        break;
                    case ChatConst.HANDLER_IMAGE_SAVE_OK:
                        sendFCM((Chat) msg.obj);
                        break;

                    case ChatConst.HANDLER_RESULT_OK:
                        // clear edit text field
                        textMsg.setText("");

                        sendFCM(chat);

                        break;
                }
            }
        };
    }

    private void prepareSendText(String msg) {
        if ((msg != null && msg.length() > 0)) {
            // create message entity
            Message message = new Message();
            message.setDate(new Date());
            message.setText(msg);
            // send message
            chatDao.sendMessage(chatRoomId, message);
        }
    }

    private void prepareSendImg(Uri uri) {
        if (uri != null) {
//            new FileUploadDao(handler).saveFile(chat);

            // upload img
            chatDao.uploadImage(chatRoomId, uri);
        }
    }

    private void sendFCM(Message message) {
        Request request = new Request();
        request.setTo(companionUser.getToken());
        request.getData().setChatRoomId(chatRoomId);
        request.getData().setMessage(ChatUtil.toJson(message));
        managerApi.send(request);
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
        MyFirebaseMessagingService.setHandler(null);
        chatDao.updateReadMessageCount(chatRoomId, adapter.getItemCount());
        adapter.stopListening();
        super.onPause();
//        adapter = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
        MyFirebaseMessagingService.setHandler(handler);
//        managerApi.getChatDao().readAllByToken(tokenCompanion, objectId);
    }
}
