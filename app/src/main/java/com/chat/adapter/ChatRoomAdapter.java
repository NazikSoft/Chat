package com.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chat.R;
import com.chat.dao.net.UserDao;
import com.chat.entity.ChatRoom;
import com.chat.entity.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by m on 15.09.2017.
 */

public class ChatRoomAdapter extends FirebaseRecyclerAdapter<ChatRoom, ChatRoomAdapter.ViewHolder> {
    private Context context;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onClick(ChatRoom chatRoom);
    }


    public ChatRoomAdapter(Context context, FirebaseRecyclerOptions<ChatRoom> options, OnChatClickListener listener) {
        super(options);
        this.listener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ChatRoomAdapter.ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(ViewHolder holder, int position, final ChatRoom chatRoom) {
        if (chatRoom.getId() == null) {
            return;
        }
        holder.textCircle.setText(String.valueOf(chatRoom.getTitle().charAt(0)).toUpperCase());
        holder.textName.setText(chatRoom.getTitle());

        final Message lastMessage = chatRoom.getLastMessage();
        if (lastMessage == null) {
            holder.textLastMessage.setText(context.getString(R.string.text_not_message_yet));
        } else {
            if (lastMessage.getText() != null && !lastMessage.getText().equals("")) {
                holder.textLastMessage.setText(lastMessage.getText());
            } else {
                holder.textLastMessage.setText(context.getString(R.string.text_get_image_message));
            }
        }

        int unreadMessageCount = getPostsCount(chatRoom);
        if (unreadMessageCount > 0) {
            holder.textCount.setVisibility(View.VISIBLE);
            holder.textCount.setText(unreadMessageCount + "");
        } else {
            holder.textCount.setVisibility(View.INVISIBLE);
            holder.textCount.setText("");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(chatRoom);
            }
        });
    }

    private int getPostsCount(ChatRoom chatRoom) {
        int chatMessageCount = chatRoom.getMessages().size();
        Map<String, Integer> map = chatRoom.getUserReadMessageCount();
        int userReadMessage = map.get(UserDao.getCurrentUserId());
        return chatMessageCount - userReadMessage;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        @BindView(R.id.textCircle)
        TextView textCircle;
        @BindView(R.id.textName)
        TextView textName;
        @BindView(R.id.textLastMessage)
        TextView textLastMessage;
        @BindView(R.id.textCount)
        TextView textCount;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}
