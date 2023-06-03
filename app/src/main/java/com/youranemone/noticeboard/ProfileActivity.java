package com.youranemone.noticeboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.databinding.ActivityChatBinding;
import com.youranemone.noticeboard.databinding.ActivityProfileBinding;
import com.youranemone.noticeboard.utils.MyConstants;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadUserData();
    }

    private void loadUserData(){
        if(getIntent() != null){
            Intent i = getIntent();
            binding.tvMailValue.setText(i.getStringExtra(MyConstants.USER_EMAIL));
            binding.tvName.setText(i.getStringExtra(MyConstants.USER_NAME));
            binding.tvPhoneValue.setText(i.getStringExtra(MyConstants.USER_TELEPHONE));
            Picasso.get().load(i.getStringExtra(MyConstants.USER_AVATAR)).into(binding.userAvatar);
        }
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
}