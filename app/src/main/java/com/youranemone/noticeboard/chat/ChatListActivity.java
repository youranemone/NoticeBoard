package com.youranemone.noticeboard.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.youranemone.noticeboard.R;
import com.youranemone.noticeboard.adapter.RecentConversationsAdapter;
import com.youranemone.noticeboard.databinding.ActivityChatBinding;
import com.youranemone.noticeboard.databinding.ActivityChatListBinding;
import com.youranemone.noticeboard.databinding.ActivityMainBinding;
import com.youranemone.noticeboard.model.ChatMessage;
import com.youranemone.noticeboard.model.UserParams;
import com.youranemone.noticeboard.utils.MyConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatListActivity extends AppCompatActivity {

    private ActivityChatListBinding binding;
    private List<ChatMessage> conversations;
//    private String senderUid;
//    private String receiverUid;
//    List<UserParams> receivedUsersList = new ArrayList<>();
//    private UserParams senderUserParams;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore firestore;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        listenConversations();
    }

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations);
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
                    }else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(MyConstants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(MyConstants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(MyConstants.KEY_SENDER_UID);
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

//    private void getDataFormDb(){
//        UserParams receiverUserParams;
//        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
//        firebaseFirestore.collection(MyConstants.KEY_COLLECTION_CHAT)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        if(receiverUid == null){
//                            if(document.getString(MyConstants.KEY_SENDER_UID).equals(auth.getUid())){
//                                senderUid = auth.getUid();
//                                receiverUid = document.getString(MyConstants.KEY_RECEIVER_UID);
//                                getUserParams(receiverUid).thenAccept(receiverUser ->{
//                                   receivedUsersList.add(receiverUser);
//                                });
//                                if(senderUserParams == null){
//                                    getUserParams(senderUid).thenAccept(senderUser ->{
//                                        senderUserParams = senderUser;
//                                    });
//                                }
//                            }if(document.getString(MyConstants.KEY_RECEIVER_UID).equals(auth.getUid())){
//                                senderUid = auth.getUid();
//                                receiverUid = document.getString(MyConstants.KEY_SENDER_UID);
//                                getUserParams(receiverUid).thenAccept(receiverUser ->{
//                                    receivedUsersList.add(receiverUser);
//                                });
//                                if(senderUserParams == null){
//                                    getUserParams(senderUid).thenAccept(senderUser ->{
//                                        senderUserParams = senderUser;
//                                    });
//                                    receiverUid = document.getString(MyConstants.KEY_RECEIVER_UID);
//                                    getUserParams(receiverUid).thenAccept(receiverUser ->{
//                                        receivedUsersList.add(receiverUser);
//                                    });
//                                }
//                            }else{
//                                continue;
//                            }
//                        }else{
//                            if(document.getString(MyConstants.KEY_SENDER_UID).equals(auth.getUid()) &&
//                            document.get(MyConstants.KEY_RECEIVER_UID) != receiverUid){
//                                receiverUid = document.getString(MyConstants.KEY_RECEIVER_UID);
//                                getUserParams(receiverUid).thenAccept(receiverUser ->{
//                                    receivedUsersList.add(receiverUser);
//                                });
//                            }if(document.getString(MyConstants.KEY_RECEIVER_UID).equals(auth.getUid()) &&
//                                    document.get(MyConstants.KEY_SENDER_UID) != receiverUid){
//                                receiverUid = document.getString(MyConstants.KEY_RECEIVER_UID);
//                                getUserParams(receiverUid).thenAccept(receiverUser ->{
//                                    receivedUsersList.add(receiverUser);
//                                });
//                            }
//                        }
//                        // Обработка каждого документа здесь
//                        // Вы можете получить данные документа с помощью метода document.getData()
//                        // Например, String fieldValue = document.getString("field_name");
//                    }
//                });
//    }

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