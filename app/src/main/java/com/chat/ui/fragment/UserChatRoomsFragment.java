package com.chat.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chat.R;
import com.chat.adapter.ChatRoomAdapter;
import com.chat.dao.net.UserDao;
import com.chat.entity.ChatRoom;
import com.chat.utils.ChatConst;
import com.chat.utils.ChatUtil;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nazar on 16.11.17.
 */

public class UserChatRoomsFragment extends Fragment {

    @BindView(R.id.recycler_view_user)
    RecyclerView recyclerView;
    private ChatRoomAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_chatrooms, container, false);
        ButterKnife.bind(this, view);
        initAdapter();
        return view;
    }


    private void initAdapter() {
        // create parser
        SnapshotParser<ChatRoom> parser = new SnapshotParser<ChatRoom>() {
            @Override
            public ChatRoom parseSnapshot(DataSnapshot dataSnapshot) {
                return dataSnapshot.getValue(ChatRoom.class);
            }
        };
        // get database reference for ChatRooms
        DatabaseReference chatDatabase = FirebaseDatabase.getInstance().getReference()
                .child(ChatConst.CHAT_DATABASE_PATH);

        // get Query of user ChatRoom
        Query chatRoomIdQuery = FirebaseDatabase.getInstance().getReference()
                .child(ChatConst.USER_DATABASE_PATH)
                .child(UserDao.getCurrentUserId())
                .child(ChatConst.COLUMN_CHAT_ROOMS)
                .orderByKey();

        // create options
        FirebaseRecyclerOptions<ChatRoom> options =
                new FirebaseRecyclerOptions.Builder<ChatRoom>()
                        .setIndexedQuery(chatRoomIdQuery, chatDatabase, parser)
                        .build();

        // init OnClickListener
        ChatRoomAdapter.OnChatClickListener listener = new ChatRoomAdapter.OnChatClickListener() {
            @Override
            public void onClick(ChatRoom chatRoom) {
                if (chatRoom == null) {
                    Toast.makeText(getActivity(), R.string.text_error, Toast.LENGTH_LONG).show();
                    return;
                }
                Fragment fragment = ChatFragment.newInstance(chatRoom.getId());
                ChatUtil.changeFragmentTo(getActivity(), fragment, "chatFragment");
            }
        };

        // init adapter
        adapter = new ChatRoomAdapter(getActivity(), options, listener);

        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        adapter.stopListening();
        super.onPause();
    }

}
