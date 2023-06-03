package com.youranemone.noticeboard.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.youranemone.noticeboard.databinding.ItemContainerChatUserBinding;
import com.youranemone.noticeboard.listeners.ConversionListener;
import com.youranemone.noticeboard.model.ChatMessage;
import com.youranemone.noticeboard.model.ConversionModel;
import com.youranemone.noticeboard.model.UserParams;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerChatUserBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerChatUserBinding binding;

        ConversionViewHolder(ItemContainerChatUserBinding itemContainerChatUserBinding){
            super(itemContainerChatUserBinding.getRoot());
            binding = itemContainerChatUserBinding;
        }

        void setData(ChatMessage chatMessage){
            Picasso.get().load(chatMessage.conversionImage).into(binding.imageProfile);
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                ConversionModel model = new ConversionModel();
                model.setId(chatMessage.conversionId);
                model.setImage(chatMessage.conversionImage);
                model.setName(chatMessage.conversionName);
                model.setAdsTitle(chatMessage.adsTitle);
                model.setAdsAdress(chatMessage.adsAdress);
                model.setAdsImageId(chatMessage.adsImage);
                conversionListener.onConversionClicked(model);
            });
        }
    }
}
