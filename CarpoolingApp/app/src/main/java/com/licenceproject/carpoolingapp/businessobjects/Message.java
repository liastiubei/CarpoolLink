package com.licenceproject.carpoolingapp.businessobjects;

import com.google.firebase.Timestamp;
/**
 * Class that defines a Message object
 */
public class Message {
    //Message ID
    private String id;
    //Message Text
    private String text;
    //Sender ID
    private String senderId;
    //Conversation ID
    private String conversationId;
    //Timestamp of the message
    private String timestamp;

    //Default constructor required for Firebase
    public Message(){
        //empty
    }

    //Main constructor for Message
    public Message(String id, String text, String senderId, String conversationId, String timestamp) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.timestamp = timestamp;
    }

    //Returns message id
    public String getId() {
        return id;
    }

    //Returns message text
    public String getText() {
        return text;
    }

    //Returns sender id
    public String getSenderId() {
        return senderId;
    }

    //Returns conversation id
    public String getConversationId() {
        return conversationId;
    }

    //Returns timestamp of the message
    public String getTimestamp() {
        return timestamp;
    }
}
