package com.youranemone.noticeboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.chat.ChatActivity;
import com.youranemone.noticeboard.map.MapActivity;
import com.youranemone.noticeboard.utils.MyConstants;

import java.util.concurrent.CompletableFuture;

public class ShowLayoutActivity extends AppCompatActivity {
    private TextView tvTitle, tvAddress, tvPrice, tvCat, tvDisc,
            tvUsername, tvUserMail, tvDateCreation, tvTelephone;
    private ImageView imMain, imAvatar;
    private Button chatBtn, callBtn;
    private UserParams senderUserParams;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String creatorUID;
    private String creatorAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_layout_activity);
        init();
    }

    private void init(){
        tvTitle = findViewById(R.id.tvShowTitle);
        tvDisc = findViewById(R.id.tvShowDisc);
        tvAddress = findViewById(R.id.tvAddressDisc);
        tvPrice = findViewById(R.id.tvPriceDisc);
        tvCat = findViewById(R.id.tvCatDisc);
        tvUsername = findViewById(R.id.tvUserName);
        tvUserMail = findViewById(R.id.tvEmailValue);
        tvDateCreation = findViewById(R.id.tvDateValue);
        tvTelephone = findViewById(R.id.tvTelephoneValue);
        imMain = findViewById(R.id.imMain);
        imAvatar = findViewById(R.id.imUserAvatar);
        chatBtn = findViewById(R.id.chatBtn);
        callBtn = findViewById(R.id.callBtn);
        if(getIntent() != null){
            Intent i = getIntent();
            tvTitle.setText(i.getStringExtra(MyConstants.TITLE));
            tvDisc.setText(i.getStringExtra(MyConstants.DISC));
            tvAddress.setText(i.getStringExtra(MyConstants.ADDRESS));
            tvPrice.setText(i.getStringExtra(MyConstants.PRICE));
            tvCat.setText(i.getStringExtra(MyConstants.CAT));
            tvUsername.setText(i.getStringExtra(MyConstants.USER_NAME));
            tvUserMail.setText(i.getStringExtra(MyConstants.USER_EMAIL));
            tvDateCreation.setText(i.getStringExtra(MyConstants.DATE));
            tvTelephone.setText(i.getStringExtra(MyConstants.USER_TELEPHONE));
            creatorUID = i.getStringExtra(MyConstants.UID);
            if(auth.getCurrentUser() == null || auth.getUid() == creatorUID){
                chatBtn.setVisibility(View.GONE);
                callBtn.setVisibility(View.GONE);
            }
            creatorAvatar = i.getStringExtra(MyConstants.USER_AVATAR);
            Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imMain);
            Picasso.get().load(i.getStringExtra(MyConstants.USER_AVATAR)).into(imAvatar);
        }
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ShowLayoutActivity.this, MapActivity.class);
                i.putExtra(MyConstants.TITLE,tvTitle.getText().toString());
                i.putExtra(MyConstants.ADDRESS,tvAddress.getText().toString());
                startActivity(i);
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserParams(auth.getUid()).thenAccept(user ->{
                    senderUserParams = user;
                    Intent i = new Intent(ShowLayoutActivity.this, ChatActivity.class);
                    i.putExtra(MyConstants.SENDER_EMAIL,senderUserParams.geteMail());
                    i.putExtra(MyConstants.SENDER_USERNAME,senderUserParams.getUsername());
                    i.putExtra(MyConstants.SENDER_UID,senderUserParams.getuID());
                    i.putExtra(MyConstants.SENDER_IMAGE_ID,senderUserParams.getImageId());
                    i.putExtra(MyConstants.RECEIVER_EMAIL,tvUserMail.getText());
                    i.putExtra(MyConstants.RECEIVER_USERNAME,tvUsername.getText());
                    i.putExtra(MyConstants.RECEIVER_UID,creatorUID);
                    i.putExtra(MyConstants.RECEIVER_IMAGE_ID,creatorAvatar);
                    startActivity(i);
                });
            }
        });
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