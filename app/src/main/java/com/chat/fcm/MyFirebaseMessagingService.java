package com.chat.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chat.R;

import com.chat.entity.Message;
import com.chat.ui.ChatActivity;
import com.chat.utils.ChatConst;
import com.chat.utils.ChatUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "log_fcm";
    private static Handler handler;
    private Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> map = remoteMessage.getData();
            String str = map.get("message");
            Message message = ChatUtil.fromJson(str, Message.class);
            String chatRoomId = map.get("chatRoomId");
            Log.i(TAG, "Message data payload: " + "\n" + message);
            if (handler == null)
                sendNotification(chatRoomId, message);
            else {
//                MediaPlayer thePlayer = MediaPlayer.create(getApplicationContext(), defaultSoundUri);
//                thePlayer.start();
//                handler.obtainMessage(ChatConst.HANDLER_RECEIVE_MSG, chatRoom).sendToTarget();
            }
        }
        if (remoteMessage.getNotification() != null) {
            Log.i(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void sendNotification(String chatId, Message message) {
        if (message == null) {
            return;
        }
        String text;
        if (message.getImageUrl() != null) {
            text = getString(R.string.text_get_image_message);
        } else {
            text = message.getText();
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatConst.EXTRA_CHAT_ID, chatId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ChatConst.NOTIFICATION_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(message.getName())
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    public static void setHandler(Handler h) {
        handler = h;
    }
}
