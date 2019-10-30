package com.wrappy.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.wrappy.android.common.utils.AssetContentProvider;


public class StockImageChooserGallery extends AppCompatActivity implements OnClickListener {
    List<String> mImageList = Collections.emptyList();
    private int mSelectedIndex = Integer.MIN_VALUE;

    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_image_gallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.image_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        try {
            mImageList = Arrays.asList(getAssets().list("profile"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mRecyclerView.setAdapter(new ImageAdapter(mImageList, this));
    }

    private Uri getUriForAsset(String assetFileName) {
        return Uri.parse(AssetContentProvider.CONTENT_URI + "/profile/" + assetFileName);
    }

    @Override
    public void onClick(View v) {
        mSelectedIndex = (int) v.getTag();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(mSelectedIndex != Integer.MIN_VALUE);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stock_image_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_choose:
                Intent intent = new Intent();
                intent.setData(getUriForAsset(mImageList.get(mSelectedIndex)));
                setResult(RESULT_OK, intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        List<String> mFileNames = new ArrayList<>();
        OnClickListener mOnClickListener;

        ImageAdapter(List<String> fileNames, OnClickListener onClickListener) {
            mFileNames.addAll(fileNames);
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.stock_image_list_item, parent, false);
            return new ImageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            holder.itemView.setOnClickListener(mOnClickListener);
            holder.itemView.setTag(position);
            if (position == mSelectedIndex) {
                holder.checkMark.setVisibility(View.VISIBLE);
            } else {
                holder.checkMark.setVisibility(View.INVISIBLE);
            }
            Glide.with(StockImageChooserGallery.this)
                    .load(getUriForAsset(mFileNames.get(position)))
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return mFileNames.size();
        }
    }

    private class ImageViewHolder extends ViewHolder {
        View checkMark;
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            checkMark = itemView.findViewById(R.id.check);
            imageView = itemView.findViewById(R.id.image);
        }
    }
}
