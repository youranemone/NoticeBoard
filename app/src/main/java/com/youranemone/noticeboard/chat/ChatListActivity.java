package com.youranemone.noticeboard.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.youranemone.noticeboard.adapter.RecentConversationsAdapter;
import com.youranemone.noticeboard.databinding.ActivityChatListBinding;
import com.youranemone.noticeboard.listeners.ConversionListener;
import com.youranemone.noticeboard.model.ChatMessage;
import com.youranemone.noticeboard.model.ConversionModel;
import com.youranemone.noticeboard.model.UserParams;
import com.youranemone.noticeboard.utils.MyConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatListActivity extends AppCompatActivity implements ConversionListener {

    private ActivityChatListBinding binding;
    private List<ChatMessage> conversations;
    private UserParams senderUserParams;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore firestore;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        init();
        listenConversations();
    }

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.dialogsRecyclerView.setAdapter(conversationsAdapter);
        firestore = FirebaseFirestore.getInstance();

    }

    private void listenConversations(){
        firestore.collection(MyConstants.KEY_CONVERSATIONS)
                .whereEqualTo(MyConstants.KEY_SENDER_UID, auth.getUid())
                .addSnapshotListener(eventListener);
        firestore.collection(MyConstants.KEY_CONVERSATIONS)
                .whereEqualTo(MyConstants.KEY_RECEIVER_UID, auth.getUid())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange: value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(MyConstants.KEY_SENDER_UID);
                    String receiverId = documentChange.getDocument().getString(MyConstants.KEY_RECEIVER_UID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderUID = senderId;
                    chatMessage.receiverUID = receiverId;
                    if(auth.getUid().equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(MyConstants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(MyConstants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(MyConstants.KEY_RECEIVER_UID);
                        chatMessage.adsTitle = documentChange.getDocument().getString(MyConstants.KEY_CONVERSATION_ADS_TITLE);
                        chatMessage.adsAdress = documentChange.getDocument().getString(MyConstants.KEY_CONVERSATION_ADS_ADDRESS);
                        chatMessage.adsImage = documentChange.getDocument().getString(MyConstants.KEY_CONVERSATION_ADS_IMAGE_ID);
                    }else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(MyConstants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(MyConstants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(MyConstants.KEY_SENDER_UID);
                        chatMessage.adsTitle = documentChange.getDocument().getString(MyConstants.KEY_CONVERSATION_ADS_TITLE);
                        chatMessage.adsAdress = documentChange.getDocument().getString(MyConstants.KEY_CONVERSATION_ADS_ADDRESS);
                        chatMessage.adsImage = documentChange.getDocument().getString(MyConstants.KEY_CONVERSATION_ADS_IMAGE_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(MyConstants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(MyConstants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(MyConstants.KEY_SENDER_UID);
                        String receiverId = documentChange.getDocument().getString(MyConstants.KEY_RECEIVER_UID);
                        if(conversations.get(i).senderUID.equals(senderId) && conversations.get(i).receiverUID.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(MyConstants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(MyConstants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.dialogsRecyclerView.smoothScrollToPosition(0);
            binding.dialogsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    @Override
    public void onConversionClicked(ConversionModel model) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        getUserParams(auth.getUid()).thenAccept(senderUser ->{
            senderUserParams = senderUser;
            intent.putExtra(MyConstants.SENDER_USERNAME,senderUserParams.getUsername());
            intent.putExtra(MyConstants.SENDER_UID,senderUserParams.getuID());
            intent.putExtra(MyConstants.SENDER_IMAGE_ID,senderUserParams.getImageId());
            intent.putExtra(MyConstants.TITLE,model.getAdsTitle());
            intent.putExtra(MyConstants.ADDRESS,model.getAdsAdress());
            intent.putExtra(MyConstants.IMAGE_ID,model.getAdsImageId());
            intent.putExtra(MyConstants.RECEIVER_USERNAME,model.getName());
            intent.putExtra(MyConstants.RECEIVER_UID,model.getId());
            intent.putExtra(MyConstants.RECEIVER_IMAGE_ID,model.getImage());
            startActivity(intent);
        });
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }



    private CompletableFuture<UserParams> getUserParams(String uid){
        CompletableFuture<UserParams> future = new CompletableFuture<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Доп параметры пользователя/" + uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserParams user = snapshot.getValue(UserParams.class);
                future.complete(user);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }
}