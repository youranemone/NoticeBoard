package com.youranemone.noticeboard.adapter;

import com.youranemone.noticeboard.NewPost;

import java.util.List;

public interface DataSender {
    public void onDataReceived(List<NewPost> listData);
}
