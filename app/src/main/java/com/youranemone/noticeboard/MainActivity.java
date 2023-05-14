package com.youranemone.noticeboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.youranemone.noticeboard.adapter.DataSender;
import com.youranemone.noticeboard.adapter.PostAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView nav_view;
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
    public static String MAUTH = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
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

        getDataDB();
        dbManager = new DbManager(dataSender, this);
        dbManager.getDataFromDb("Аренда на длительный срок");
        postAdapter.setDbManager(dbManager);



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
            case R.id.id_my_ads:
                dbManager.getMyAdsDataFromDb(mAuth.getUid());
                break;
            case R.id.id_my_favourite_ads:
                Toast.makeText(this, "Pressed favourite ads", Toast.LENGTH_LONG).show();
                break;
            case R.id.id_my_long_ads:
                dbManager.getDataFromDb("Аренда на длительный срок");
                break;
            case R.id.id_my_days_ads:
                dbManager.getDataFromDb("Посуточная аренда");
                break;
            case R.id.id_my_chat:

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
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(index == 0){
                    signUp(edEmail.getText().toString().trim(),edPassword.getText().toString());
                }else{
                    signIn(edEmail.getText().toString().trim(),edPassword.getText().toString());
                }
                dialog.dismiss();
            }
        });
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private void signUp(String email, String password){
        if(!email.equals("") && !password.equals("")) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        getUserData();
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
        getUserData();
    }
    private void getUserData(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            userEmail.setText(currentUser.getEmail());
            MAUTH = mAuth.getUid();
        }
        else{
            userEmail.setText(R.string.sign_in_or_sign_up);
            MAUTH = "";
        }
    }

}