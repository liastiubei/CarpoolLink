package com.licenceproject.carpoolingapp.messaging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Conversation;
import com.licenceproject.carpoolingapp.businessobjects.Message;
import com.licenceproject.carpoolingapp.businessobjects.MessageAdapter;
import com.licenceproject.carpoolingapp.businessobjects.Passenger;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.factoryclasses.ErrorHandlingAppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//class for the implementation of the actual messenger
public class MessageThreadActivity extends ErrorHandlingAppCompatActivity {
    //recycler view for messages
    private RecyclerView recyclerViewMessages;
    //input for message
    private EditText editTextMessage;
    //button to send the message
    private Button buttonSendMessage;
    //database collection reference for the conversation
    private CollectionReference conversationsRef;
    //database collection reference for the messages
    private CollectionReference messagesRef;
    //conversation id
    private String conversationId;
    //message adapter
    private MessageAdapter messageAdapter;
    //list of messages
    private List<Message> messageList;
    //the passenger of the ride for the conversation
    private static Passenger passenger;
    //the ride for the conversation
    private static Ride ride;

    //sets the passenger for the conversation
    public static void setPassenger(Passenger p) {
        passenger = p;
    }

    //sets the ride for the conversation
    public static void setRide(Ride r) {
        ride = r;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_thread);
        messageList = new ArrayList<>();
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);

        // Get a reference to the "conversations" collection in Firestore
        conversationsRef = FirebaseFirestore.getInstance().collection("conversations");

        // Retrieve the conversation document from Firestore or create a new one
        getOrCreateConversation();

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    //function for sending the message
    private void sendMessage(){
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Create a new message object
            String messageId = messagesRef.document().getId();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Calendar calendar = Calendar.getInstance();
            Message message = new Message(messageId, messageText, User.getSessionUser().getUsername(),
                    conversationId, dateFormat.format(calendar.getTime()));

            // Save the message to Firestore
            messagesRef.document(messageId).set(message)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Clear the message input field
                                editTextMessage.setText("");
                            } else {
                                // Handle the error while saving the message
                                Log.e("MessageThreadActivity", "Failed to send message: " +
                                        task.getException().getMessage());
                            }
                        }
                    });
        }
    }

    // Retrieve the conversation document from Firestore or create a new one
    private void getOrCreateConversation() {
        // Create a query to find the conversation between the passenger and driver
        Query query = conversationsRef
                .whereEqualTo("passengerId", passenger.getPassengerId())
                .whereEqualTo("driverId", ride.getDriverId());

        // Execute the query
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        // Existing conversation found
                        DocumentSnapshot document = snapshot.getDocuments().get(0);
                        conversationId = document.getId();
                        Conversation conversation = document.toObject(Conversation.class);
                        conversation.setId(document.getId());
                        setUpUI();
                    } else {
                        // Conversation not found, create a new one
                        createNewConversation();
                    }
                } else {
                    // Handle any errors that occur while retrieving data
                    Log.e("MessageThreadActivity", "Failed to retrieve conversation: " + task.getException().getMessage());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        passenger = null;
        ride = null;
        super.onDestroy();
    }

    // Create a new conversation in the database
    private void createNewConversation() {
        // Create a new Conversation object with necessary data
        Conversation conversation = new Conversation();
        conversation.setPassengerId(passenger.getPassengerId());
        conversation.setDriverId(ride.getDriverId());
        conversation.setPassengerUserId(passenger.getUserId());

        // Add the conversation to the "conversations" collection in Firestore
        conversationsRef.add(conversation).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    // Conversation created successfully
                    DocumentReference conversationRef = task.getResult();
                    conversationId = conversationRef.getId();
                    setUpUI();
                } else {
                    // Handle the error while creating the conversation
                    Log.e("MessageThreadActivity", "Failed to create conversation: " + task.getException().getMessage());
                }
            }
        });
    }

    //function to set up the message adapter, get the messages from the database and add a listener for any updates
    private void setUpUI() {
        // Get a reference to the "messages" collection in Firestore
        messagesRef = FirebaseFirestore.getInstance().collection("messages");

        // Set up a query to retrieve the messages for the current conversation in chronological order
        Query query = messagesRef.whereEqualTo("conversationId", conversationId).orderBy("timestamp");

        // Create a message adapter
        messageAdapter = new MessageAdapter(messageList);

        // Set up the RecyclerView with a LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        // Listen for changes in the query
        query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle any errors that occur while listening for updates
                    Log.e("MessageThreadActivity", "Failed to listen for messages: " + e.getMessage());
                    return;
                }

                // Clear the message list
                messageList.clear();

                // Iterate through the document snapshots and add messages to the list
                for (QueryDocumentSnapshot snapshot : snapshots) {
                    Message message = snapshot.toObject(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }

                // Notify the adapter that the data has changed
                messageAdapter.notifyDataSetChanged();

                if (messageList.size() > 0) {
                    // Scroll to the bottom of the RecyclerView
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                }
            }
        });

        // Retrieve the initial set of messages
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Clear the message list before adding new messages
                    messageList.clear();

                    // Iterate through the document snapshots and add messages to the list
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        Message message = snapshot.toObject(Message.class);
                        if (message != null) {
                            messageList.add(message);
                        }
                    }

                    // Notify the adapter that the data has changed
                    messageAdapter.notifyDataSetChanged();

                    if (messageList.size() > 0) {
                        // Scroll to the bottom of the RecyclerView
                        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                    }
                } else {
                    // Handle any errors that occur while retrieving data
                    Log.e("MessageThreadActivity", "Failed to retrieve messages: " + task.getException().getMessage());
                }
            }
        });
    }
}
