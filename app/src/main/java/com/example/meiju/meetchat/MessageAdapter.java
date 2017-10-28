package com.example.meiju.meetchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages> mMessagesList;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Messages> messagesList) {
        mMessagesList = messagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {

        mFirebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        Messages c = mMessagesList.get(position);

        String fromUser = c.getFrom();
        String messageType = c.getType();

        if(fromUser.equals(currentUserId)) {
            holder.messageText.setTextColor(R.color.colorAccent);
        } else {
            holder.messageText.setTextColor(R.color.colorPrimary);
        }

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUser);

        if(messageType.equals("text")) {
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        } else {
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(holder.messageImage.getContext()).load(c.getMessage()).into(holder.messageImage);
        }

    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }
}
