package com.chat.adapter;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chat.R;
import com.chat.entity.Chat;
import com.chat.entity.User;
import com.chat.utils.ChatConst;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by m on 15.09.2017.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> userList;
    private Handler handler;


    public UserAdapter(List<User> list, Handler handler) {
        this.userList = list;
        this.handler = handler;
    }

    public User getItem(int position) {
        return userList.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.textCircle.setText(String.valueOf(user.getName().charAt(0)).toUpperCase());
        holder.textName.setText(user.getName());
//        holder.textLastMessage.setText(user.getLastMessage);
        if (user.getCountNewPost() != 0){
            holder.textCount.setVisibility(View.VISIBLE);
            holder.textCount.setText(user.getCountNewPost() + "");
        }
    }

    public void setPostsCount(List<Chat> postsCount) {
        for (Chat c : postsCount) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getToken().equals(c.getCompanionToken()) && !c.isRead()){
                    userList.get(i).setCountNewPost(userList.get(i).getCountNewPost() + 1);
                    Log.i("log_tag", "setPostsCount: "+c.getObjectId());
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userList.size();
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
            User user = getItem(getAdapterPosition());
            handler.obtainMessage(ChatConst.HANDLER_USER_OBJ, user.getToken()).sendToTarget();
        }
    }
}
