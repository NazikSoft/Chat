package com.chat.dao.net;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chat.dao.ObjectDao;
import com.chat.entity.Chat;
import com.chat.entity.ChatRoom;
import com.chat.entity.Message;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chat.utils.ChatConst.LOADING_IMAGE_URL;

/**
 * Created by m on 23.09.2017.
 */

public class ChatDao extends ObjectDao {

    private DatabaseReference chatRef;
    private DatabaseReference userRef;
    private UserDao userDao;
    // temp data
    private Map<String, Object> tempMap;
    private int counter;

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
                            success(ChatConst.HANDLER_RESULT_OK, new ArrayList<ChatRoom>());
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
        counter = 0;
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
                    counter++;
                    if (!dataSnapshot.exists()) {
//                        success(ChatConst.HANDLER_RESULT_OK, new ArrayList<ChatRoom>());
                        return;
                    }

                    // put data to temp Map
                    ChatRoom chatRoom = dataSnapshot.getChildren().iterator().next().getValue(ChatRoom.class);
                    if (chatRoom == null) {
                        chatRoom = new ChatRoom();
                    }
                    tempMap.put(chatRoom.getId(), chatRoom);

                    // check is this call last
                    if (counter == chatroomIds.size()) {
                        List<ChatRoom> result = new ArrayList<>();
                        for (String id : chatroomIds) {
                            ChatRoom chat = (ChatRoom) tempMap.get(id);
                            if (chat != null) {
                                result.add((ChatRoom) tempMap.get(id));
                            }
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

    public void sendMessage(String chatRoomId, Message message) {
        DatabaseReference messageRef = chatRef.child(chatRoomId).child(ChatConst.COLUMN_MESSAGES);
        String messageId = messageRef.push().getKey();
        message.setId(messageId);
        String name = UserDao.getCurrentUserName();
        if (name == null || name.equals("")) {
            name = UserDao.getCurrentUserEmail();
        }
        message.setName(name);
        message.setPhotoUrl(UserDao.getCurrentUserPhoto());
        messageRef.child(messageId).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public void uploadImage(String chatRoomId, final Uri uri) {
        Log.d(ChatConst.TAG, "Uri: " + uri.toString());
        DatabaseReference messageRef = chatRef.child(chatRoomId).child(ChatConst.COLUMN_MESSAGES);

        String messageId = messageRef.push().getKey();
        Date date = new Date();
        String name = UserDao.getCurrentUserName();
        if (name == null || name.equals("")) {
            name = UserDao.getCurrentUserEmail();
        }
        String imageUrl = ChatConst.LOADING_IMAGE_URL;
        String photoUrl = UserDao.getCurrentUserPhoto();
        // create message entity
        Message message = new Message(messageId, null, name, photoUrl, imageUrl, date);
        // save message in db
        messageRef.child(messageId).setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // save img in cloud storage
                    String key = databaseReference.getKey();
                    StorageReference storageReference =
                            FirebaseStorage.getInstance()
                                    .getReference(UserDao.getCurrentUserId())
                                    .child(key)
                                    .child(uri.getLastPathSegment());
                    putImageInStorage(storageReference, uri, key);
                } else {
                    Log.w(ChatConst.TAG, "Unable to write message to database.", databaseError.toException());
                    error(ChatConst.HANDLER_RESULT_ERR);
                }
            }
        });
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult().getDownloadUrl();
                    if (downloadUrl == null){
                        error(ChatConst.HANDLER_RESULT_ERR);
                        return;
                    }
                    String img = downloadUrl.toString();

                    // update image data in database
                    FirebaseDatabase.getInstance().getReference()
                            .child(key)
                            .setValue(img).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                success(ChatConst.HANDLER_IMAGE_SAVE_OK);
                            }else {
                                error(ChatConst.HANDLER_RESULT_ERR);
                            }
                        }
                    });
                } else {
                    Log.w(ChatConst.TAG, "Image upload task was not successful.", task.getException());
                }
            }
        });
    }

    public void updateReadMessageCount(String chatRoomId, int messageCount) {
        chatRef.child(chatRoomId)
                .child(ChatConst.COLUMN_USER_READ_MESSAGE_COUNT)
                .child(UserDao.getCurrentUserId())
                .setValue(messageCount);
    }
}
