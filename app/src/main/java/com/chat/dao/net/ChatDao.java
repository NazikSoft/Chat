package com.chat.dao.net;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chat.dao.ObjectDao;
import com.chat.entity.Chat;
import com.chat.entity.ChatRoom;
import com.chat.entity.User;
import com.chat.utils.ChatConst;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by m on 23.09.2017.
 */

public class ChatDao extends ObjectDao {

    private DatabaseReference chatRef;
    private DatabaseReference userRef;
    private UserDao userDao;
    // temp data
    private Map<String, Object> tempMap;

    public ChatDao(Handler handler) {
        super(handler);
        if (chatRef == null) {
            chatRef = FirebaseDatabase.getInstance().getReference(ChatConst.CHAT_DATABASE_PATH);
            chatRef.keepSynced(true);
        }
        userRef = FirebaseDatabase.getInstance().getReference(ChatConst.USER_DATABASE_PATH);
        userDao = new UserDao(handler);
    }

    public void save(Chat chat) {
        if (chat == null) {
            error(ChatConst.HANDLER_RESULT_ERR);
            return;
        }
        final String objectId = chatRef.push().getKey();

        chat.setObjectId(objectId);
        chatRef.child(objectId).setValue(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    success(ChatConst.HANDLER_RESULT_OK, objectId);
                } else {
                    error(ChatConst.HANDLER_RESULT_ERR);
                }
            }
        });
    }

    public void readAllByObjectId(final String objectId) {
        int limit = 100;
        Query query;
        if (objectId != null) {
            query = chatRef.orderByKey().startAt(objectId);
        } else {
            query = chatRef.orderByChild(ChatConst.CHAT_DATABASE_PATH);
        }
        query.limitToLast(limit);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Chat> chats = new ArrayList<>();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    Chat chat = s.getValue(Chat.class);
                    if (chat.equalsTokens() && (objectId == null || !chat.getObjectId().equals(objectId))) {
                        chats.add(chat);
                        Log.i(ChatConst.TAG + "1", "chatDao filter chat: " + chat.toString());
                    }
                }
                success(ChatConst.HANDLER_CHAT_LIST, chats);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(ChatConst.TAG, "onCancelled: ");
                error(ChatConst.HANDLER_RESULT_ERR);
            }
        });
    }

    public void readAllByToken(final String companionToken, final String objectId) {
        int limit = 100;
        final List<Chat> list = new ArrayList<>();
        Query query;
        if (objectId != null) {
            query = chatRef.orderByKey().endAt(objectId);
        } else {
            query = chatRef.orderByChild(ChatConst.CHAT_DATABASE_PATH);
        }
        query.limitToLast(limit);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    Chat chat = s.getValue(Chat.class);
                    if (chat.equalsTokens(companionToken)) {//&& (objectId == null || !chat.getObjectId().equals(objectId))
                        list.add(chat);
//                        Log.i(ChatConst.TAG,"readAllByToken: "+chat.toString());
                    }

                }
                success(ChatConst.HANDLER_CHAT_LIST, list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                error(ChatConst.HANDLER_RESULT_ERR);
            }
        });
    }

    public void updateByMap(String objectId, Map<String, Object> update) {
        chatRef.child(objectId).updateChildren(update);
    }

    public void updateByObject(final Chat chat) {
        if (chat.getObjectId() == null) return;

//        chat.setLastUpdate(new Date().getTime());
        chatRef.child(chat.getObjectId())
                .getRef().setValue(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i("log_chatDao", "updateByObject ok");
                    success(ChatConst.HANDLER_RESULT_OK, chat);
                }
            }
        });
    }

    public void createChatRoomWith(User user, ChatRoom chatRoom) {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updateData = new HashMap<>();

        String chatRoomId = chatRef.push().getKey();

        // add update to current user account
        String path1 = ChatConst.USER_DATABASE_PATH + "/"
                + UserDao.getCurrentUserId() + "/"
                + ChatConst.COLUMN_CHAT_ROOMS + "/"
                + chatRoomId;
        updateData.put(path1, "");

        // add update to second user account
        String path2 = ChatConst.USER_DATABASE_PATH + "/"
                + user.getObjectId() + "/"
                + ChatConst.COLUMN_CHAT_ROOMS + "/"
                + chatRoomId;
        updateData.put(path2, "");

        // add chat room to chat reference
        chatRoom.setId(chatRoomId);
        updateData.put(chatRef.getKey() + "/" + chatRoomId, chatRoom);

        // make update
        root.updateChildren(updateData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    success(ChatConst.HANDLER_RESULT_OK);
                } else {
                    error(ChatConst.HANDLER_RESULT_ERR);
                }

            }
        });


    }

    // return list of ChatRoom by user id
    public void getChatroomByUserId(String userId) {
        userRef.child(userId)
                .child(ChatConst.COLUMN_CHAT_ROOMS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            error(ChatConst.HANDLER_RESULT_ERR);
                            return;
                        }

                        List<String> result = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String chatroomId = snapshot.getKey();
                            if (chatroomId != null && !chatroomId.equals("")) {
                                result.add(chatroomId);
                            }
                        }
                        getChatroomListByIds(result);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        error(ChatConst.HANDLER_RESULT_ERR);
                    }
                });
    }

    // return list of ChatRoom by list of ChatRoom's ids
    public void getChatroomListByIds(final List<String> chatroomIds) {
        if (chatroomIds == null || chatroomIds.size() == 0) {
            success(ChatConst.HANDLER_RESULT_OK, new ArrayList<ChatRoom>());
            return;
        }
        tempMap = new HashMap<>();
        for (String chatroomId : chatroomIds) {
            chatRef.orderByKey()
                    .equalTo(chatroomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // is data exist
                    if (!dataSnapshot.exists()) {
                        success(ChatConst.HANDLER_RESULT_OK, new ArrayList<ChatRoom>());
                        return;
                    }

                    // put data to temp Map
                    ChatRoom chatRoom = dataSnapshot.getChildren().iterator().next().getValue(ChatRoom.class);
                    if (chatRoom == null) {
                        chatRoom = new ChatRoom();
                    }
                    tempMap.put(chatRoom.getId(), chatRoom);

                    // check is this call last
                    if (tempMap.size() == chatroomIds.size()) {
                        List<ChatRoom> result = new ArrayList<>();
                        for (String id : chatroomIds) {
                            result.add((ChatRoom) tempMap.get(id));
                        }
                        success(ChatConst.HANDLER_RESULT_OK, result);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    error(ChatConst.HANDLER_RESULT_ERR);
                }
            });
        }

    }
}
