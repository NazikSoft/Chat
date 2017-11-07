package com.chat.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chat.R;
import com.chat.dao.net.UserDao;
import com.chat.entity.ChatRoom;
import com.chat.entity.Message;
import com.chat.entity.User;
import com.chat.utils.ChatConst;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by m on 15.09.2017.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<ChatRoom> chatRooms;
    private Handler handler;


    public UserAdapter(Context context, List<ChatRoom> list, Handler handler) {
        this.chatRooms = list;
        this.handler = handler;
        this.context = context;
    }

    public ChatRoom getItem(int position) {
        return chatRooms.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        holder.textCircle.setText(String.valueOf(chatRoom.getTitle().charAt(0)).toUpperCase());
        holder.textName.setText(chatRoom.getTitle());

        Message lastMessage = chatRoom.getLastMessage();
        if (lastMessage == null) {
            holder.textLastMessage.setText(context.getString(R.string.text_not_message_yet));
        } else {
            holder.textLastMessage.setText(lastMessage.getText());
        }

        int unreadMessageCount = getPostsCount(chatRoom);
        if (unreadMessageCount > 0) {
            holder.textCount.setVisibility(View.VISIBLE);
            holder.textCount.setText(unreadMessageCount + "");
        } else {
            holder.textCount.setVisibility(View.INVISIBLE);
            holder.textCount.setText("");
        }
    }

    public int getPostsCount(ChatRoom chatRoom) {
        int chatMessageCount = chatRoom.getMessages().size();
        Map<String, Integer> map = chatRoom.getUserReadMessageCount();
        int userReadMessage = map.get(UserDao.getCurrentUserId());
        return chatMessageCount - userReadMessage;
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ChatRoom chatRoom = getItem(getAdapterPosition());
            handler.obtainMessage(ChatConst.HANDLER_CLICK_RECYCLER_ITEM, chatRoom).sendToTarget();
        }
    }
}
