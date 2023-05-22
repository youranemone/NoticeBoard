package com.youranemone.noticeboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.utils.MyConstants;

public class ShowLayoutActivity extends AppCompatActivity {
    private TextView tvTitle, tvAddress, tvPrice, tvCat, tvDisc,
            tvUsername, tvUserMail, tvDateCreation, tvTelephone;
    private ImageView imMain, imAvatar;
    private Button chatBtn, callBtn;

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
            Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imMain);
            Picasso.get().load(i.getStringExtra(MyConstants.USER_AVATAR)).into(imAvatar);
        }
    }
}