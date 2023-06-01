package com.youranemone.noticeboard.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.R;
import com.youranemone.noticeboard.model.UserParams;
import com.youranemone.noticeboard.databinding.ActivityChatBinding;
import com.youranemone.noticeboard.utils.MyConstants;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private UserParams receiverUser = new UserParams(),senderUser = new UserParams();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadUsersDetails();
    }

    private void loadUsersDetails(){
        if(getIntent() != null){
            Intent i = getIntent();
            binding.tvAdsTitle.setText(i.getStringExtra(MyConstants.TITLE));
            binding.tvAdsAdr.setText(i.getStringExtra(MyConstants.ADDRESS));
            Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(binding.adsImage);
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
    }
}