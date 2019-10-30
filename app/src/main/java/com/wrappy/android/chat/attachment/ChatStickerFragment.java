package com.wrappy.android.chat.attachment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wrappy.android.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatStickerFragment extends Fragment implements View.OnClickListener {

    View.OnClickListener mOnClickListener;
    RecyclerView mRecyclerView;
    StickerListAdapter mStickerListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_stickers, container, false);

        mRecyclerView = view.findViewById(R.id.chat_stickers_recyclerview);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));

        mStickerListAdapter = new StickerListAdapter(Arrays.asList(getResources().getStringArray(R.array.chat_sticker_name)));
        mRecyclerView.setAdapter(mStickerListAdapter);

        return view;
    }


    @Override
    public void onClick(View v) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public class StickerListAdapter extends RecyclerView.Adapter<StickerListAdapter.StickerViewHolder> {

        List<String> mStickersList;

        public StickerListAdapter(List<String> stickersList) {
            mStickersList = stickersList;
        }

        @NonNull
        @Override
        public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.chat_item_sticker, parent, false);
            return new StickerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
            holder.bindTo(mStickersList.get(position));
        }

        @Override
        public int getItemCount() {
            return mStickersList.size();
        }

        public class StickerViewHolder extends RecyclerView.ViewHolder {

            ImageView imageViewSticker;
            View mItemView;

            public StickerViewHolder(View itemView) {
                super(itemView);
                imageViewSticker = itemView.findViewById(R.id.chat_sticker);
                mItemView = itemView;
            }

            void bindTo(String sticker) {
                Glide.with(getContext()).load("file:///android_asset/sticker/" + sticker).into(imageViewSticker);
                mItemView.setTag(sticker);
                mItemView.setOnClickListener(ChatStickerFragment.this);
            }
        }
    }

}
