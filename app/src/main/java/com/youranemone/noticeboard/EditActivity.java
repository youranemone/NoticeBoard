package com.youranemone.noticeboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.utils.MyConstants;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditActivity extends AppCompatActivity {
    private ImageView imItem;
    private StorageReference storageRef;
    private Uri uploadUri;
    private Spinner spinner;
    private DatabaseReference dRef;
    private FirebaseAuth mAuth;
    //Переменные для создания объявления
    private EditText edTitle, edPrice, edAddress, edDisc;
    private Boolean edit_state = false;
    private String temp_cat = "";
    private String temp_uid = "";
    private String temp_time = "";
    private String temp_key = "";
    private String temp_status = "";
    private String temp_date = "";
    private String temp_image_url = "";
    private String temp_total_views = "";
    private Boolean isImageUpdate = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        init();
    }

    private void init(){
        edTitle = findViewById(R.id.edTitle);
        edPrice = findViewById(R.id.edPrice);
        edAddress = findViewById(R.id.edAddress);
        edDisc = findViewById(R.id.edDiscription);
        spinner = findViewById(R.id.spType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.edit_act_type,
                R.layout.custom_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        storageRef = FirebaseStorage.getInstance().getReference("Images");
        imItem = findViewById(R.id.imItem);
        getMyIntent();
    }

    private void getMyIntent(){
        if(getIntent() != null){
            Intent i = getIntent();
            edit_state = i.getBooleanExtra(MyConstants.EDIT_STATE, false);
            if(edit_state){
                setDataAds(i);
            }
        }
    }

    private void setDataAds(Intent i){
        edTitle.setText(i.getStringExtra(MyConstants.TITLE));
        edAddress.setText(i.getStringExtra(MyConstants.ADDRESS));
        edPrice.setText(i.getStringExtra(MyConstants.PRICE));
        edDisc.setText(i.getStringExtra(MyConstants.DISC));
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(i.getStringExtra(MyConstants.CAT));
        spinner.setSelection(position);
        Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imItem);
        temp_cat = i.getStringExtra(MyConstants.CAT);
        temp_uid = i.getStringExtra(MyConstants.UID);
        temp_time = i.getStringExtra(MyConstants.TIME);
        temp_key = i.getStringExtra(MyConstants.KEY);
        temp_date = i.getStringExtra(MyConstants.DATE);
        temp_status = i.getStringExtra(MyConstants.STATUS);
        temp_image_url = i.getStringExtra(MyConstants.IMAGE_ID);
        temp_total_views = i.getStringExtra(MyConstants.TOTAL_VIEWS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10 && data != null && data.getData() != null){
            if(resultCode == RESULT_OK){
                imItem.setImageURI(data.getData());
                isImageUpdate = true;
            }
        }
    }

    private void uploadImage(){
        Bitmap bitmap = ((BitmapDrawable)imItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mref = storageRef.child(System.currentTimeMillis() + "_image");
        UploadTask up = mref.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                assert uploadUri != null;
                savePost();
                Toast.makeText(EditActivity.this, "Upload done!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    public void onClickImage(View view) {
        getImage();
    }

    public void onClickSavePost (View view){
        if(!edit_state){
            uploadImage();
        }else{
            if(isImageUpdate){
                uploadUpdateImage();
            }else{
                updatePost();
            }
        }
    }

    private void getImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,10);
    }

    private void savePost(){
        dRef = FirebaseDatabase.getInstance().getReference(spinner.getSelectedItem().toString());
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getUid() != null){
            NewPost post = new NewPost();
            String key = dRef.push().getKey();

            post.setImageId(uploadUri.toString());
            post.setTitle(edTitle.getText().toString());
            post.setAddress(edAddress.getText().toString());
            post.setPrice(edPrice.getText().toString());
            post.setDisc(edDisc.getText().toString());
            post.setStatus("Active");
            post.setTime(String.valueOf(System.nanoTime()));
            post.setUid(mAuth.getUid());
            String timeStamp = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
            post.setDate(timeStamp);
            post.setTotal_views("0");
            post.setKey(key);
            post.setCat(spinner.getSelectedItem().toString());

            if(key != null) dRef.child(key).child("anuncio").setValue(post);
        }
    }

    private void updatePost(){
        dRef = FirebaseDatabase.getInstance().getReference(temp_cat);
        NewPost post = new NewPost();

        post.setImageId(temp_image_url);
        post.setTitle(edTitle.getText().toString());
        post.setAddress(edAddress.getText().toString());
        post.setPrice(edPrice.getText().toString());
        post.setDisc(edDisc.getText().toString());
        post.setStatus(temp_status);
        post.setTime(temp_time);
        post.setUid(temp_uid);
        post.setDate(temp_date);
        post.setKey(temp_key);
        post.setCat(temp_cat);
        post.setTotal_views(temp_total_views);
        dRef.child(temp_key).child("anuncio").setValue(post);

    }

    private void uploadUpdateImage(){
        Bitmap bitmap = ((BitmapDrawable)imItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mref = FirebaseStorage.getInstance().getReferenceFromUrl(temp_image_url);
        UploadTask up = mref.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                assert uploadUri != null;
                temp_image_url = uploadUri.toString();
                updatePost();
                Toast.makeText(EditActivity.this, "Upload done!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}
