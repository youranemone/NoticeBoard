package com.youranemone.noticeboard.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.DbManager;
import com.youranemone.noticeboard.EditActivity;
import com.youranemone.noticeboard.MainActivity;
import com.youranemone.noticeboard.NewPost;
import com.youranemone.noticeboard.R;
import com.youranemone.noticeboard.ShowLayoutActivity;
import com.youranemone.noticeboard.UserParams;
import com.youranemone.noticeboard.utils.MyConstants;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {

    private final List<NewPost> arrayPost;
    private final Context context;
    private final OnItemClickCustom onItemClickCustom;
    private DbManager dbManager;


    public PostAdapter(List<NewPost> arrayPost, Context context, PostAdapter.OnItemClickCustom onItemClickCustom) {
        this.arrayPost = arrayPost;
        this.context = context;
        this.onItemClickCustom = onItemClickCustom;
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ads, parent,false);
        return new ViewHolderData(view,onItemClickCustom);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {
        holder.setData(arrayPost.get(position));
    }

    @Override
    public int getItemCount() {
        return arrayPost.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView tvTitle;
        private final TextView tvPriceAdr;
        private final TextView tvDisc;
        private final TextView tvTotalViews;
        private final ImageView imAds;
        private final LinearLayout editLayout;
        private final ImageButton deleteButton;
        private final ImageButton editButton;
        private final OnItemClickCustom onItemClickCustom;

        private UserParams userParams = new UserParams();

        public ViewHolderData(@NonNull View itemView, OnItemClickCustom onItemClickCustom) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDisc = itemView.findViewById(R.id.tvDisc);
            tvPriceAdr = itemView.findViewById(R.id.tvPriceAdr);
            tvTotalViews = itemView.findViewById(R.id.tvTotalViews);
            imAds = itemView.findViewById(R.id.imAds);
            editLayout = itemView.findViewById(R.id.edit_layout);
            deleteButton = itemView.findViewById(R.id.imDelete);
            editButton = itemView.findViewById(R.id.imEditItem);
            this.onItemClickCustom = onItemClickCustom;
            itemView.setOnClickListener(this);

        }

        public void setData(NewPost post){
            if(post.getUid().equals(MainActivity.MAUTH)){
                editLayout.setVisibility(View.VISIBLE);
            }else{
                editLayout.setVisibility(View.GONE);
            }
            Picasso.get().load(post.getImageId()).into(imAds);
            tvTitle.setText(post.getTitle());
            String priceAdr;
            if(post.getCat().equals("Посуточная аренда")){
                priceAdr = "Цена: " + post.getPrice() + " руб./сутки" + "\n" + "Адрес: " + post.getAddress();
            }else{
                priceAdr = "Цена: " + post.getPrice() + " руб./месяц" + "\n" + "Адрес: " + post.getAddress();
            }
            tvPriceAdr.setText(priceAdr);
            tvTotalViews.setText(post.getTotal_views());
            String textDisc = null;
            if(post.getDisc().length() > 50) textDisc = post.getDisc().substring(0,60) + "...";
            else textDisc = post.getDisc();
            tvDisc.setText(textDisc);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteDialog(post, getAdapterPosition());
                }
            });
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, EditActivity.class);
                    i.putExtra(MyConstants.IMAGE_ID,post.getImageId());
                    i.putExtra(MyConstants.TITLE, post.getTitle());
                    i.putExtra(MyConstants.PRICE, post.getPrice());
                    i.putExtra(MyConstants.TIME,post.getTime());
                    i.putExtra(MyConstants.ADDRESS,post.getAddress());
                    i.putExtra(MyConstants.DISC,post.getDisc());
                    i.putExtra(MyConstants.STATUS,post.getStatus());
                    i.putExtra(MyConstants.KEY,post.getKey());
                    i.putExtra(MyConstants.UID,post.getUid());
                    i.putExtra(MyConstants.DATE,post.getDate());
                    i.putExtra(MyConstants.CAT,post.getCat());
                    i.putExtra(MyConstants.EDIT_STATE,true);
                    i.putExtra(MyConstants.TOTAL_VIEWS, post.getTotal_views());
                    context.startActivity(i);
                }
            });
        }

        @Override
        public void onClick(View view) {
            NewPost post = arrayPost.get(getAdapterPosition());
            dbManager.getUserParams(userParams, post.getUid());
            //getUserParams(post.getUid());
            dbManager.updateTotalViews(post);
            Intent i = new Intent(context, ShowLayoutActivity.class);
            i.putExtra(MyConstants.IMAGE_ID,post.getImageId());
            i.putExtra(MyConstants.TITLE, post.getTitle());
            i.putExtra(MyConstants.PRICE, post.getPrice());
            i.putExtra(MyConstants.ADDRESS,post.getAddress());
            i.putExtra(MyConstants.DISC,post.getDisc());
            i.putExtra(MyConstants.DATE,post.getDate());
            i.putExtra(MyConstants.CAT,post.getCat());
            i.putExtra(MyConstants.USER_EMAIL,userParams.geteMail());
            i.putExtra(MyConstants.USER_NAME,userParams.getUsername());
            i.putExtra(MyConstants.USER_TELEPHONE,userParams.getPhone_number());
            i.putExtra(MyConstants.USER_AVATAR,userParams.getImageId());
            context.startActivity(i);
            onItemClickCustom.onItemSelected(getAdapterPosition());
        }

//        private void setAllUserParams(String email, S){
//            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Доп параметры пользователя");
//
//            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    UserParams u =snapshot.getValue(UserParams.class);
//                    userParams = u;
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }

    }

    private void deleteDialog(final NewPost newPost, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_title);
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbManager.deleteItem(newPost);
                arrayPost.remove(position);
                notifyItemRemoved(position);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    public interface OnItemClickCustom{
        void onItemSelected(int position);
    }

    public void updateAdapter(List<NewPost> listData){
        arrayPost.clear();
        arrayPost.addAll(listData);
        notifyDataSetChanged();
    }

    public void setDbManager(DbManager dbManager){
        this.dbManager = dbManager;
    }
}
