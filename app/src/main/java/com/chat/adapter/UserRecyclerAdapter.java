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
import com.chat.entity.User;
import com.chat.ui.TempActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

/**
 * Created by nazar on 01.11.17.
 */

public class UserRecyclerAdapter extends FirebaseRecyclerAdapter<User, UserRecyclerAdapter.UserViewHolder> {

    private Context context;
    private OnUserClickListener listener;

    public interface OnUserClickListener{
        void oClick(User user);
    }

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public UserRecyclerAdapter(Context context, FirebaseRecyclerOptions<User> options, OnUserClickListener listener) {
        super(options);
        this.context = context;
        this.listener = listener;
    }


    @Override
    public UserRecyclerAdapter.UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new UserRecyclerAdapter.UserViewHolder(inflater.inflate(R.layout.item_user_raw, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(final UserRecyclerAdapter.UserViewHolder viewHolder,
                                    int position,
                                    final User user) {
//                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        viewHolder.userName.setText(user.getName());
//        String imageUrl = user.getImgUrl();
//        if (imageUrl.startsWith("gs://")) {
//            StorageReference storageReference = FirebaseStorage.getInstance()
//                    .getReferenceFromUrl(imageUrl);
//            storageReference.getDownloadUrl().addOnCompleteListener(
//                    new OnCompleteListener<Uri>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Uri> task) {
//                            if (task.isSuccessful()) {
//                                String downloadUrl = task.getResult().toString();
//                                Glide.with(viewHolder.messageImageView.getContext())
//                                        .load(downloadUrl)
//                                        .into(viewHolder.messageImageView);
//                            } else {
//                                Log.w(TAG, "Getting download url was not successful.",
//                                        task.getException());
//                            }
//                        }
//                    });
//        } else {
//            Picasso.with(viewHolder.messageImageView.getContext())
//                    .load(user.get)
//                    .into(viewHolder.messageImageView);
//        }
        if (user.getImgUrl() == null) {
            viewHolder.userImage.setImageDrawable(ContextCompat.getDrawable(context,
                    R.mipmap.ic_launcher));
        } else {
            Picasso.with(context)
                    .load(user.getImgUrl())
                    .into(viewHolder.userImage);
        }
        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.oClick(user);
            }
        });
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView userName;
        ImageView userImage;

        public UserViewHolder(View v) {
            super(v);
            root = v;
            userName = (TextView) v.findViewById(R.id.textName);
            userImage = (ImageView) v.findViewById(R.id.userImg);
        }
    }
}