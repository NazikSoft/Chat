package com.chat.adapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chat.R;
import com.chat.entity.User;
import com.chat.utils.ChatСonstants;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by m on 15.09.2017.
 */

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.ViewHolder> {
    private List<User> list;
    private Handler handler;

    public AdapterUser(List<User> list, Handler handler) {
        this.list = list;
        this.handler = handler;

    }

    public User getItem(int position) {
        return list.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User users = list.get(position);
        holder.text.setText(users.getName().charAt(0));
        holder.text2.setText( users.getName());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View itemView;
        @Bind(R.id.text)
        TextView text;
        @Bind(R.id.text2)
        TextView text2;
        @Bind(R.id.text3)
        TextView text3;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            User user = getItem(getPosition());
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(user.getObjectId()));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            itemView.getContext().startActivity(browserIntent);
            handler.obtainMessage(ChatСonstants.HANDLER_USER_OBJ, user).sendToTarget();

        }
    }
}
