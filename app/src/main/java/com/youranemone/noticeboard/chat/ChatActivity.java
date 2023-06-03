package com.youranemone.noticeboard.chat;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import com.youranemone.noticeboard.adapter.ChatAdapter;
import com.youranemone.noticeboard.model.ChatMessage;
import com.youranemone.noticeboard.model.UserParams;
import com.youranemone.noticeboard.databinding.ActivityChatBinding;
import com.youranemone.noticeboard.utils.MyConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private UserParams receiverUser = new UserParams(),senderUser = new UserParams();
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private String keyAds;
    private String adsImage;
    private SharedPreferences preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadUsersDetails();
        init();
        listenMessages();
    }

    private void init(){
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                receiverUser.getImageId(),
                senderUser.getuID()
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(MyConstants.KEY_SENDER_UID,senderUser.getuID());
        message.put(MyConstants.KEY_RECEIVER_UID,receiverUser.getuID());
        message.put(MyConstants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(MyConstants.KEY_ADS_CHAT,keyAds);
        message.put(MyConstants.KEY_TIMESTAMP,new Date());
        database.collection(MyConstants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId != null){
            updateConversation(binding.inputMessage.getText().toString());
        }else{
            HashMap<String,Object> conversation = new HashMap<>();
            conversation.put(MyConstants.KEY_SENDER_UID,senderUser.getuID());
            conversation.put(MyConstants.KEY_SENDER_NAME,senderUser.getUsername());
            conversation.put(MyConstants.KEY_SENDER_IMAGE, senderUser.getImageId());
            conversation.put(MyConstants.KEY_RECEIVER_UID, receiverUser.getuID());
            conversation.put(MyConstants.KEY_RECEIVER_NAME, receiverUser.getUsername());
            conversation.put(MyConstants.KEY_RECEIVER_IMAGE, receiverUser.getImageId());
            conversation.put(MyConstants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(MyConstants.KEY_CONVERSATION_ADS_IMAGE_ID,adsImage);
            conversation.put(MyConstants.KEY_CONVERSATION_ADS_TITLE,binding.tvAdsTitle.getText().toString());
            conversation.put(MyConstants.KEY_CONVERSATION_ADS_ADDRESS,binding.tvAdsAdr.getText().toString());
            conversation.put(MyConstants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }
        binding.inputMessage.setText(null);
    }

    private void listenMessages(){
        database.collection(MyConstants.KEY_COLLECTION_CHAT)
                .whereEqualTo(MyConstants.KEY_SENDER_UID,senderUser.getuID())
                .whereEqualTo(MyConstants.KEY_RECEIVER_UID, receiverUser.getuID())
                .addSnapshotListener(eventListener);
        database.collection(MyConstants.KEY_COLLECTION_CHAT)
                .whereEqualTo(MyConstants.KEY_SENDER_UID,receiverUser.getuID())
                .whereEqualTo(MyConstants.KEY_RECEIVER_UID,senderUser.getuID());
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{
        if(error != null){
            return;
        }if(value != null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderUID = documentChange.getDocument().getString(MyConstants.KEY_SENDER_UID);
                    chatMessage.receiverUID = documentChange.getDocument().getString(MyConstants.KEY_RECEIVER_UID);
                    chatMessage.message = documentChange.getDocument().getString(MyConstants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(MyConstants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(MyConstants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversationId == null) {
            checkForConversation();
        }
    };

    private void loadUsersDetails(){
        if(getIntent() != null){
            Intent i = getIntent();
            keyAds = i.getStringExtra(MyConstants.KEY);
            binding.tvAdsTitle.setText(i.getStringExtra(MyConstants.TITLE));
            binding.tvAdsAdr.setText(i.getStringExtra(MyConstants.ADDRESS));
            adsImage = i.getStringExtra(MyConstants.IMAGE_ID);
            Picasso.get().load(adsImage).into(binding.adsImage);
            senderUser.setuID(i.getStringExtra(MyConstants.SENDER_UID));
            senderUser.setUsername(i.getStringExtra(MyConstants.SENDER_USERNAME));
            senderUser.setPhone_number(null);
            senderUser.seteMail(i.getStringExtra(MyConstants.SENDER_EMAIL));
            senderUser.setImageId(i.getStringExtra(MyConstants.SENDER_IMAGE_ID));
            receiverUser.setuID(i.getStringExtra(MyConstants.RECEIVER_UID));
            receiverUser.setUsername(i.getStringExtra(MyConstants.RECEIVER_USERNAME));
            receiverUser.setPhone_number(null);
            receiverUser.seteMail(i.getStringExtra(MyConstants.RECEIVER_EMAIL));
            receiverUser.setImageId(i.getStringExtra(MyConstants.RECEIVER_IMAGE_ID));
            binding.textName.setText(receiverUser.getUsername());
        }
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDateTime (Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String,Object> conversation){
        database.collection(MyConstants.KEY_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());

    }

    private void updateConversation(String message){
        DocumentReference reference =
                database.collection(MyConstants.KEY_CONVERSATIONS).document(conversationId);
        reference.update(
                MyConstants.KEY_LAST_MESSAGE, message,
                MyConstants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversation(){
        if(chatMessages.size() != 0){
            checkForConversRemotely(
                    senderUser.getuID(),
                    receiverUser.getuID()
            );
            checkForConversRemotely(
                    receiverUser.getuID(),
                    senderUser.getuID()
            );
        }
    }

    private void checkForConversRemotely(String senderId, String receiverId){
        database.collection(MyConstants.KEY_CONVERSATIONS)
                .whereEqualTo(MyConstants.KEY_SENDER_UID,senderId)
                .whereEqualTo(MyConstants.KEY_RECEIVER_UID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };
}