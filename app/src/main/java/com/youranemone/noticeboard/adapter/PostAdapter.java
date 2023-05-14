package com.youranemone.noticeboard.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.DbManager;
import com.youranemone.noticeboard.MainActivity;
import com.youranemone.noticeboard.NewPost;
import com.youranemone.noticeboard.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {

    private List<NewPost> arrayPost;
    private Context context;
    private OnItemClickCustom onItemClickCustom;
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

        private TextView tvTitle, tvPriceAdr, tvDisc;
        private ImageView imAds;
        private LinearLayout editLayout;
        private ImageButton deleteButton;
        private OnItemClickCustom onItemClickCustom;

        public ViewHolderData(@NonNull View itemView, OnItemClickCustom onItemClickCustom) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDisc = itemView.findViewById(R.id.tvDisc);
            tvPriceAdr = itemView.findViewById(R.id.tvPriceAdr);
            imAds = itemView.findViewById(R.id.imAds);
            editLayout = itemView.findViewById(R.id.edit_layout);
            deleteButton = itemView.findViewById(R.id.imDelete);
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
            String textDisc = null;
            if(post.getDisc().length() > 50) textDisc = post.getDisc().substring(0,50) + "...";
            else textDisc = post.getDisc();
            tvDisc.setText(textDisc);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    deleteDialog(post, getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View view) {
            onItemClickCustom.onItemSelected(getAdapterPosition());
        }
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
        public void onItemSelected(int position);
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
