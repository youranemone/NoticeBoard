package com.youranemone.noticeboard.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.youranemone.noticeboard.R;
import com.youranemone.noticeboard.UserParams;
import com.youranemone.noticeboard.databinding.ActivityChatBinding;
import com.youranemone.noticeboard.utils.MyConstants;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private UserParams receiverUser,senderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void loadUsersDetails(){
        if(getIntent() != null){
            Intent i = getIntent();
            senderUser.setuID(MyConstants.SENDER_UID);
            senderUser.setUsername(MyConstants.SENDER_USERNAME);
            senderUser.setPhone_number(null);
            senderUser.seteMail(MyConstants.SENDER_EMAIL);
            senderUser.setImageId(MyConstants.SENDER_IMAGE_ID);
            receiverUser.setuID(MyConstants.RECEIVER_UID);
            receiverUser.setUsername(MyConstants.RECEIVER_USERNAME);
            receiverUser.setPhone_number(null);
            receiverUser.seteMail(MyConstants.RECEIVER_EMAIL);
            receiverUser.setImageId(MyConstants.RECEIVER_IMAGE_ID);
        }
    }
}