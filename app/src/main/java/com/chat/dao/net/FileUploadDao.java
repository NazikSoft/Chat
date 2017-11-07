package com.chat.dao.net;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chat.BuildConfig;
import com.chat.dao.ObjectDao;
import com.chat.entity.Chat;
import com.chat.utils.ChatConst;
import com.chat.utils.ImageUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileUploadDao extends ObjectDao {
    private static final String TAG = "log_UploadDao";
    private static final int PIXEL = 300;

    //    private List<String> paths;
    private Chat chat;
    private ChatDao chatDao;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public FileUploadDao(Handler handler) {
        super(handler);
        chatDao = new ChatDao(handler);
        storage = FirebaseStorage.getInstance();
        if (storageRef == null) {
            storageRef = storage.getReference();
        }
    }

    public void saveFile(Chat chat) {
        if (chat.getUrlFile() != null && chat.getUrlFile().length() > 0) {
            this.chat = chat;
            String path = chat.getUrlFile();
            Log.i(TAG, "saveFile: " + path);
            Uri uri = Uri.parse(path);
            Bitmap bitmap = ImageUtil.decodeSampledBitmapFromResource(path, PIXEL, PIXEL);
            if (bitmap == null)
                Log.i(TAG, "bitmap = null: ");
            saveBitmap(chat, bitmap, chat.getObjectId() + "-" + uri.getLastPathSegment());
        }

    }

    private void saveBitmap(final Chat chat, Bitmap bitmap, String nameImage) {
        if (bitmap == null) {
            if (chat.getUrlFile().contains("http"))
                success(ChatConst.HANDLER_IMAGE_SAVE_OK, chat);
            return;
        }
        final String path = "image-" + nameImage;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.child(path).putBytes(data);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "onProgress " + (int) progress);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final String path = taskSnapshot.getMetadata().getDownloadUrl().toString();
                chat.setUrlFile(path);
                chatDao.updateByMap(chat.getObjectId(), new HashMap<String, Object>() {{
                    put("urlFile", path);
                }});
                chat.setUrlFile(path);
                success(ChatConst.HANDLER_IMAGE_SAVE_OK, chat);

            }
        });
    }

}