package com.chat.dao.net;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chat.dao.ObjectDao;
import com.chat.entity.User;
import com.chat.utils.ChatConst;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


/**
 * Created by m on 22.09.2017.
 */

public class UserDao extends ObjectDao {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private String key;

    public UserDao(Handler handler) {
        super(handler);
        if (userRef == null) {
            database = FirebaseDatabase.getInstance();
            userRef = database.getReference(ChatConst.USER_DATABASE_PATH);
            mFirebaseAuth = FirebaseAuth.getInstance();
        }
    }

    // sign in with email and password
    public void signInWithEmail(final String email, String password) {
        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    User user = new User(email, FirebaseInstanceId.getInstance().getToken(), new Date().getTime());
                    save(user);
                } else {
                    error(ChatConst.HANDLER_RESULT_ERR);
                }
            }
        });
    }

    public boolean isUserLogged() {
        return mFirebaseAuth.getCurrentUser() != null;
    }

    private void save(User user) {
        if (user == null) {
            error(ChatConst.HANDLER_RESULT_ERR);
            return;
        }
        user.setObjectId(mFirebaseAuth.getCurrentUser().getUid());
        final String key = user.getObjectId();

        userRef.child(key).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    success(ChatConst.HANDLER_RESULT_OK, key);
                } else {
                    error(ChatConst.HANDLER_RESULT_ERR);
                }
            }
        });
    }

    public void update(User user) {
        if (user == null) {
            error(ChatConst.HANDLER_RESULT_ERR);
            return;
        }
        user.setLastUpdate(new Date().getTime());
        if (user.getObjectId() == null) return;

        user.setLastUpdate(new Date().getTime());
        userRef.child(user.getObjectId())
                .getRef().setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    success(ChatConst.HANDLER_RESULT_OK);
                }
            }
        });
    }

    public void readAll() {
        final String token = FirebaseInstanceId.getInstance().getToken();
        userRef.orderByChild(ChatConst.USER_DATABASE_PATH)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<User> list = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null && !user.getToken().equals(token))
                                list.add(user);
                        }
                        success(ChatConst.HANDLER_USERS_LIST, list);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        error(ChatConst.HANDLER_RESULT_ERR);
                    }
                });

    }

    public void findUserAll(final String token) {
        findUserByToken(token, false);
        findUserByToken(FirebaseInstanceId.getInstance().getToken(), true);
    }

    public void findUserByToken(final String token, final boolean currentUser) {
        Query query = userRef.orderByChild("token").equalTo(token);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    User user = s.getValue(User.class);
                    if (currentUser)
                        success(ChatConst.HANDLER_RESULT_CURRENT_USER, user);
                    else
                        success(ChatConst.HANDLER_RESULT_COMPAMION_USER, user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                error(ChatConst.HANDLER_RESULT_ERR);
            }
        });
    }

}
