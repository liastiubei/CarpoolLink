package com.licenceproject.carpoolingapp.mainmenu;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.licenceproject.carpoolingapp.R;
import com.licenceproject.carpoolingapp.businessobjects.Conversation;
import com.licenceproject.carpoolingapp.businessobjects.ConversationAdapter;
import com.licenceproject.carpoolingapp.businessobjects.Passenger;
import com.licenceproject.carpoolingapp.businessobjects.Ride;
import com.licenceproject.carpoolingapp.businessobjects.User;
import com.licenceproject.carpoolingapp.messaging.MessageThreadActivity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//Fragment for the conversation list
public class MessagesFragment extends Fragment {
    //listview of the conversations
    private ListView listViewConversations;
    //Database reference for the conversations collection
    private CollectionReference conversationsRef;
    //Database reference for the passengers collection
    private CollectionReference passengersRef;
    //Database reference for the rides collection
    private CollectionReference rideRef;
    //conversation adapter for the list of conversations
    private ConversationAdapter conversationAdapter;

    public MessagesFragment() {
        // Required empty public constructor
    }

    //create a new MessagesFragment
    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        listViewConversations = rootView.findViewById(R.id.listViewConversations);

        // Get a reference to the "conversations" collection in Firestore
        conversationsRef = FirebaseFirestore.getInstance().collection("conversations");
        passengersRef = FirebaseFirestore.getInstance().collection("passengers");
        rideRef = FirebaseFirestore.getInstance().collection("rides");

        getConversations();


        // Set a click listener for conversation items in the ListView
        listViewConversations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected conversation
                Conversation conversation = (Conversation) parent.getItemAtPosition(position);

                passengersRef.document(conversation.getPassengerId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot != null && documentSnapshot.exists()){

                            MessageThreadActivity.setPassenger(new Passenger(documentSnapshot.getId(), documentSnapshot.getString("userID"), documentSnapshot.getString("rideID"), documentSnapshot.getString("firstName"), documentSnapshot.getString("receivedRating"), documentSnapshot.getString("gaveRating")));
                            rideRef.document(documentSnapshot.getString("rideID")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot snapshot) {
                                    if(snapshot != null &&snapshot.exists()){
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                            Gson gson = new Gson();
                                            String startDateString = snapshot.getString("startDate");
                                            Date startDate = new Date();
                                            try {
                                                startDate = sdf.parse(startDateString);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                            String startLocationJson = snapshot.getString("startLocation");
                                            String endLocationJson = snapshot.getString("endLocation");
                                            LatLng startLocation = gson.fromJson(startLocationJson, LatLng.class);
                                            LatLng endLocation = gson.fromJson(endLocationJson, LatLng.class);

                                            Ride ride = new Ride(snapshot.getId(), snapshot.getString("driverID"), snapshot.getString("driverFirstName"),
                                                    Integer.parseInt(snapshot.getString("nrPassengers")), Integer.parseInt(snapshot.getString("currentNrOfPassengers")), startLocation, endLocation, startDate,
                                                    snapshot.getString("price"), snapshot.getString("currency"));
                                            MessageThreadActivity.setRide(ride);
                                            openMessageThread();
                                        }

                                        }
                                }
                            });
                        }
                    }
                });

            }
        });

        return rootView;
    }

    //Function for getting and setting up the conversations
    private void getConversations() {
        // Retrieve conversation data from Firestore
        conversationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Conversation> conversations = new ArrayList<>();

                    for (DocumentSnapshot document : task.getResult()) {
                        Conversation conversation = new Conversation(document.getId(), document.getString("driverId"), document.getString("passengerId"), document.getString("passengerUserId"));

                        // Check if the conversation involves the current user as either a passenger or a driver
                        if(conversation.getDriverId().equals(User.getSessionUser().getUsername())||conversation.getPassengerUserId().equals(User.getSessionUser().getUsername())){
                            conversations.add(conversation);
                        }
                    }

                    conversationAdapter= new ConversationAdapter(getContext(), conversations);
                    listViewConversations.setAdapter(conversationAdapter);
                } else {
                    // Handle any errors that occur while retrieving data
                    Log.e("MessagesFragment", "Failed to retrieve conversations: " + task.getException().getMessage());
                }
            }
        });
    }

    //Opens the message thread of the conversation
    private void openMessageThread(){
        // Open the message thread activity and pass the conversation details
        Intent intent = new Intent(getActivity(), MessageThreadActivity.class);
        startActivity(intent);
    }
}
