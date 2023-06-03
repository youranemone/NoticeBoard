package com.youranemone.noticeboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.adapter.DataSender;
import com.youranemone.noticeboard.adapter.PostAdapter;
import com.youranemone.noticeboard.chat.ChatListActivity;
import com.youranemone.noticeboard.model.UserParams;
import com.youranemone.noticeboard.model.NewPost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView nav_view;
    private MenuItem allAds, myAds, chatItem, calendarItem,
            settings, signUpItem, signInItem, signOutItem;
    private MenuItem adsCat, chatCat;
    private Menu menu;
    private ImageView avatar;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private TextView userEmail;
    private AlertDialog dialog;
    private Toolbar toolbar;
    private PostAdapter.OnItemClickCustom onItemClickCustom;
    private RecyclerView rcView;
    private PostAdapter postAdapter;
    private DataSender dataSender;
    private DbManager dbManager;
    private DatabaseReference dRef;
    private StorageReference sRef;
    public static String MAUTH = "";
    private String current_cat = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(current_cat.equals("my_ads")){
            dbManager.getMyAdsDataFromDb(mAuth.getUid());
        }else{
            dbManager.getAllAdsDataFromDb(mAuth.getUid());
        }
    }

    private void init(){

        setOnItemClickCustom();
        rcView = findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(this));
        List<NewPost> arrayPost = new ArrayList<>();
        postAdapter = new PostAdapter(arrayPost,this,onItemClickCustom);
        rcView.setAdapter(postAdapter);
        nav_view = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.toogle_open,R.string.toogle_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);
        userEmail = nav_view.getHeaderView(0).findViewById(R.id.tvEmail);
        mAuth = FirebaseAuth.getInstance();
        avatar = nav_view.getHeaderView(0).findViewById(R.id.Avatar);

        getDataDB();
        dbManager = new DbManager(dataSender, this);
        postAdapter.setDbManager(dbManager);
        menu = nav_view.getMenu();
        allAds = menu.findItem(R.id.id_all_ads);
        myAds = menu.findItem(R.id.id_my_ads);
        chatItem = menu.findItem(R.id.id_my_chat);
        calendarItem = menu.findItem(R.id.id_my_calendar);
        signUpItem = menu.findItem(R.id.id_sign_up);
        signInItem = menu.findItem(R.id.id_sign_in);
        signOutItem = menu.findItem(R.id.id_sign_out);
        settings = menu.findItem(R.id.id_settings);
        chatCat = menu.findItem(R.id.chatCat);
        adsCat = menu.findItem(R.id.adsCat);
    }

    private void getDataDB(){
        dataSender = new DataSender() {
            @Override
            public void onDataReceived(List<NewPost> listData) {
                Collections.reverse(listData);
                postAdapter.updateAdapter(listData);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserData();
    }

    private void setOnItemClickCustom(){
        onItemClickCustom = new PostAdapter.OnItemClickCustom() {
            @Override
            public void onItemSelected(int position) {
            }
        };
    }

    public void onClickEdit(View view){
        Intent i = new Intent(MainActivity.this,EditActivity.class);
        startActivity(i);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.id_all_ads:
                dbManager.getAllAdsDataFromDb(mAuth.getUid());
                current_cat = "all_ads";
                break;
            case R.id.id_my_ads:
                dbManager.getMyAdsDataFromDb(mAuth.getUid());
                current_cat = "my_ ads";
                break;
            case R.id.id_my_chat:
                Intent i = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(i);
                break;
            case R.id.id_my_calendar:

                break;
            case R.id.id_sign_up:
                signUpDialog(R.string.sign_up_title,R.string.sign_up_btn,0);
                break;
            case R.id.id_sign_in:
                signUpDialog(R.string.sign_in_title,R.string.sign_in_btn,1);
                break;
            case R.id.id_sign_out:
                signOut();
                getUserData();
                break;
        }
        return true;
    }

    private void signUpDialog(int title, int btnTitle, int index){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sign_up_layout,null);
        dialogBuilder.setView(dialogView);
        TextView titleTextView = dialogView.findViewById(R.id.tvAlertTitle);
        titleTextView.setText(title);
        Button btn = dialogView.findViewById(R.id.buttonSignUp);
        btn.setText(btnTitle);
        if(index == 0){
            EditText text = dialogView.findViewById(R.id.edUsername);
            text.setVisibility(View.VISIBLE);
            EditText editText = dialogView.findViewById(R.id.edTelephone);
            editText.setVisibility(View.VISIBLE);
            Button forgetBtn = dialogView.findViewById(R.id.buttonForgetP);
            forgetBtn.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btn.getLayoutParams();
            params.topToBottom = editText.getId();
            btn.setLayoutParams(params);
        }
        EditText edEmail = dialogView.findViewById(R.id.edEmail);
        EditText edPassword = dialogView.findViewById(R.id.edPassword);
        EditText edUsername = dialogView.findViewById(R.id.edUsername);
        EditText edTelephone = dialogView.findViewById(R.id.edTelephone);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(index == 0){
                    signUp(edEmail.getText().toString().trim(),edPassword.getText().toString(),edUsername.getText().toString(), edTelephone.getText().toString());
                }else{
                    signIn(edEmail.getText().toString().trim(),edPassword.getText().toString());
                }
                dialog.dismiss();
            }
        });
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private void signUp(String email, String password, String username, String telephone){
        if(!email.equals("") && !password.equals("")) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        getUserData();
                        setUserDopParams(email,username,telephone);
                        //getFirstAvatar(mAuth.getUid());
                    } else {
                        Log.d("MyLogMainActivity", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            Toast.makeText(this, "Email или password пустой!!", Toast.LENGTH_SHORT).show();
        }
    }
    private void signIn(String email, String password){
        if(!email.equals("") && !password.equals("")) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        getUserData();
                        //getFirstAvatar(mAuth.getUid());
                    } else {
                        Log.d("MyLogMainActivity", "signInWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    private void signOut(){
        mAuth.signOut();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                getUserData();
            }
        });
    }
    private void getUserData(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            userEmail.setText(currentUser.getEmail());
            MAUTH = mAuth.getUid();
            adsCat.setVisible(true);
            allAds.setVisible(true);
            myAds.setVisible(true);
            chatCat.setVisible(true);
            chatItem.setVisible(true);
            calendarItem.setVisible(true);
            signInItem.setVisible(false);
            signUpItem.setVisible(false);
            settings.setVisible(true);
            signOutItem.setVisible(true);
        }
        else {
            userEmail.setText(R.string.sign_in_or_sign_up);
            MAUTH = "";

            adsCat.setVisible(true);
            allAds.setVisible(false);
            myAds.setVisible(false);

            chatCat.setVisible(false);
            chatItem.setVisible(false);
            calendarItem.setVisible(false);
            signInItem.setVisible(true);
            signUpItem.setVisible(true);
            settings.setVisible(false);
            signOutItem.setVisible(false);
        }
    }

    private void setUserDopParams(String username, String telephone){
        dRef = FirebaseDatabase.getInstance().getReference("Доп параметры пользователя");
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getUid() != null){
            UserParams userParams = new UserParams();
            userParams.setImageId("Доработать потом");
            userParams.setUsername(username);
            userParams.setPhone_number(telephone);
            userParams.setuID(mAuth.getUid());

            dRef.child(mAuth.getUid()).setValue(userParams);
        }
    }

    private void setUserDopParams(String mail, String username, String telephone){
        dRef = FirebaseDatabase.getInstance().getReference("Доп параметры пользователя");
        mAuth = FirebaseAuth.getInstance();
        StorageReference imgRef = sRef.child("user-default-ico.jpg");
        imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imgUri = uri.toString();
                if(mAuth.getUid() != null){
                    UserParams userParams = new UserParams();
                    userParams.setImageId(imgUri);
                    userParams.setUsername(username);
                    userParams.setPhone_number(telephone);
                    userParams.seteMail(mail);
                    userParams.setuID(mAuth.getUid());

                    dRef.child(mAuth.getUid()).setValue(userParams);
                }
            }
        });
    }

    private void getFirstAvatar(String uid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Доп параметры пользователя");
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userAvatarPath = (String) snapshot.child("imageId").getValue();
                Picasso.get().load(userAvatarPath).into(avatar);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}