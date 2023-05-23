package com.youranemone.noticeboard;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.youranemone.noticeboard.adapter.DataSender;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private Context context;
    private Query myQuery;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private FirebaseDatabase db;
    private FirebaseStorage fs;
    private int cat_ads_counter = 0;
    private String[] category_ads = {"Посуточная аренда", "Аренда на длительный срок"};

    public DbManager(DataSender dataSender, Context context) {
        this.dataSender = dataSender;
        this.context = context;
        newPostList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        fs = FirebaseStorage.getInstance();
    }

    public void updateTotalViews(final NewPost newPost){
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(newPost.getCat());
        int total_views;
        try{
            total_views = Integer.parseInt(newPost.getTotal_views());

        }catch (NumberFormatException e){
            total_views = 0;
        }
        total_views++;
        dRef.child(newPost.getKey()).child("anuncio/total_views").setValue(String.valueOf(total_views));
    }

    public void getDataFromDb(String path){
        //if(newPostList.size() > 0) newPostList.clear();
        DatabaseReference dbRef = db.getReference(path);
        myQuery = dbRef.orderByChild("anuncio/time");
        readDataUpdate();
    }

    public void getMyAdsDataFromDb(String uid){
        if(newPostList.size() > 0) newPostList.clear();
        DatabaseReference dbRef = db.getReference(category_ads[0]);
        myQuery = dbRef.orderByChild("anuncio/uid").equalTo(uid);
        readMyAdsData(uid);
        cat_ads_counter++;
    }

    public void getAllAdsDataFromDb(String uid){
        if(newPostList.size() > 0) newPostList.clear();
        DatabaseReference dbRef = db.getReference(category_ads[0]);
        myQuery = dbRef.orderByChild("anuncio/uid");
        readAllAdsData(uid);
        cat_ads_counter++;
    }

    public void readDataUpdate(){
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(newPostList.size() > 0) newPostList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    NewPost newPost = ds.child("anuncio").getValue(NewPost.class);
                    newPostList.add(newPost);
                }
                dataSender.onDataReceived(newPostList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void readMyAdsData(final String uid){
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    NewPost newPost = ds.child("anuncio").getValue(NewPost.class);
                    newPostList.add(newPost);
                }
                if(cat_ads_counter > 1){
                    dataSender.onDataReceived(newPostList);
                    newPostList.clear();
                    cat_ads_counter = 0;
                }else{
                    DatabaseReference dbRef = db.getReference(category_ads[cat_ads_counter]);
                    myQuery = dbRef.orderByChild("anuncio/uid").equalTo(uid);
                    readMyAdsData(uid);
                    cat_ads_counter++;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void readAllAdsData(String uid){
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    NewPost newPost = ds.child("anuncio").getValue(NewPost.class);
                    if(!newPost.getUid().equals(uid)){
                        newPostList.add(newPost);
                    }
                }
                if(cat_ads_counter > 1){
                    dataSender.onDataReceived(newPostList);
                    newPostList.clear();
                    cat_ads_counter = 0;
                }else{
                    DatabaseReference dbRef = db.getReference(category_ads[cat_ads_counter]);
                    myQuery = dbRef.orderByChild("anuncio/uid");
                    readAllAdsData(uid);
                    cat_ads_counter++;
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteItem(final NewPost newPost){
        StorageReference sRef = fs.getReferenceFromUrl(newPost.getImageId());
        sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DatabaseReference dbRef = db.getReference(newPost.getCat());
                dbRef.child(newPost.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Объявление удалено успешно!", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Ошибка при удалении!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Ошибка при удалении, картинка не была удалена!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
