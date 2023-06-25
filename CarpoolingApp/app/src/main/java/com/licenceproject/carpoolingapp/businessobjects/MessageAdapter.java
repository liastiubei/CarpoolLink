package com.licenceproject.carpoolingapp.businessobjects;// MessageAdapter.java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Message;

import java.util.List;

/**
 * Adapter for the implementation of the Message List
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    //List of messages
    private List<Message> messageList;

    //Constructor
    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    // Binds the data to the views in the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.textViewMessage.setText(message.getText());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("messages").whereEqualTo("id", message.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                db.collection("users").whereEqualTo("username", queryDocumentSnapshots.getDocuments().get(0).getString("senderId")).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocuments) {
                        holder.textViewName.setText(queryDocuments.getDocuments().get(0).getString("firstname"));
                    }
                });
            }
        });

    }

    // Returns the number of items in the RecyclerView
    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    // ViewHolder for the individual items in the RecyclerView
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        TextView textViewName;

        //Constructor for the MessageViewHolder
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewName = itemView.findViewById(R.id.user_and_time);
        }
    }

    // Creates a new ViewHolder when needed
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            // Inflate the sender message layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_item, parent, false);
        } else {
            // Inflate the receiver message layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_message_item, parent, false);
        }
        return new MessageAdapter.MessageViewHolder(view);
    }

    // Returns the type of the item at the given position
    @Override
    public int getItemViewType(int position) {
        // Determine the type of the message based on the sender's ID
        Message message = messageList.get(position);
        if (message.getSenderId().equals(User.getSessionUser().getUsername())) {
            // Sender message
            return 1;
        } else {
            // Receiver message
            return 2;
        }
    }
}
