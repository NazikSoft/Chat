package com.chat.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chat.R;
import com.chat.entity.Message;
import com.chat.utils.ChatConst;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nazar on 01.11.17.
 */

public class ChatRecyclerAdapter extends FirebaseRecyclerAdapter<Message, ChatRecyclerAdapter.ChatViewHolder> {

    private Context context;
    private OnImgMessageClickListener listener;

    public interface OnImgMessageClickListener {
        void oClick(String path);
    }

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ChatRecyclerAdapter(Context context, FirebaseRecyclerOptions<Message> options, OnImgMessageClickListener listener) {
        super(options);
        this.context = context;
        this.listener = listener;
    }


    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatRecyclerAdapter.ChatViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(final ChatViewHolder holder, int position, final Message message) {

        // setup message body (text or img)
        if (message.getText() != null) {
            // set text
            holder.messageText.setText(message.getText());
            holder.messageText.setVisibility(TextView.VISIBLE);
            holder.messageImg.setVisibility(ImageView.GONE);
        } else {
            // get img url
            String imageUrl = message.getImageUrl();

            // callback for img loader
            final Callback callback = new Callback() {
                @Override
                public void onSuccess() {
                    Log.i(ChatConst.TAG, "ChatAdapter onSuccess ");
                }

                @Override
                public void onError() {
                    Log.i(ChatConst.TAG, "ChatAdapter onError");
                }
            };

            // set img
            holder.messageImg.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    listener.oClick(message.getImageUrl());
                }
            });

            if (imageUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(imageUrl);
                storageReference.getDownloadUrl().addOnCompleteListener(
                        new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String downloadUrl = task.getResult().toString();
                                    Picasso.with(holder.messageImg.getContext())
                                            .load(downloadUrl)
//                                            .resize(150, 250)
//                                            .centerCrop()
                                            .placeholder(R.drawable.placeholder)
                                            .into(holder.messageImg, callback);
                                } else {
                                    Log.w(ChatConst.TAG, "Getting download url was not successful.",
                                            task.getException());
                                }
                            }
                        });
            } else {
                Picasso.with(holder.messageImg.getContext())
                        .load(message.getImageUrl())
//                        .resize(50, 50)
//                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .into(holder.messageImg, callback);
            }

            holder.messageImg.setVisibility(ImageView.VISIBLE);
            holder.messageText.setVisibility(TextView.GONE);
        }

        // setup message data
        holder.date.setText(ChatConst.sdf.format(message.getDate()));

        // setup user photo
        holder.messengerName.setText(message.getName());
        if (message.getPhotoUrl() == null || message.getPhotoUrl().equals("")) {
            holder.accountImg.setImageDrawable(ContextCompat.getDrawable(context,
                    R.mipmap.ic_launcher));
        } else {
            Picasso.with(context)
                    .load(message.getPhotoUrl())
                    .into(holder.accountImg);
        }
    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.messageTextView)
        TextView messageText;
        @BindView(R.id.messegerTextView)
        TextView messengerName;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.messageImageView)
        ImageView messageImg;
        @BindView(R.id.messengerImageView)
        CircleImageView accountImg;

        public ChatViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
