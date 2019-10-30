package com.wrappy.android.chat.gallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedListAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.LruCache;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.DefaultOnImageEventListener;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.DefaultOnStateChangedListener;
import com.wrappy.android.R;
import com.wrappy.android.chat.ChatViewModel;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.ui.WPRecyclerScrollMoreListener;
import com.wrappy.android.common.ui.WPRecyclerScrollMoreListener.OnLoadMoreListener;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.db.entity.MessageView;
import com.wrappy.android.xmpp.ChatManager;
import com.wrappy.android.xmpp.aws.AWSCertificate;

/**
 *
 */
public class ChatImageGalleryFragment extends SubFragment implements OnLoadMoreListener, OnClickListener {
    private static final String KEY_CHAT_JID = "chat_gallery_jid";
    private static final String KEY_CHAT_TYPE = "chat_gallery_type";
    private static final String KEY_MESSAGE_CREATED_AT = "chat_gallery_message_created_at";
    private static final String KEY_AWS_CERTIFICATE = "chat_gallery_aws_cert";

    private static final int REQUEST_PERMISSION_DOWNLOAD = 0;

    public static ChatImageGalleryFragment create(String chatJid,
                                                  String chatType,
                                                  Date messageCreatedAt,
                                                  AWSCertificate awsCertificate) {
        ChatImageGalleryFragment chat = new ChatImageGalleryFragment();
        Bundle args = new Bundle();
        args.putString(KEY_CHAT_JID, chatJid);
        args.putString(KEY_CHAT_TYPE, chatType);
        args.putSerializable(KEY_MESSAGE_CREATED_AT, messageCreatedAt);
        args.putSerializable(KEY_AWS_CERTIFICATE, awsCertificate);
        chat.setArguments(args);
        return chat;
    }

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    LruCache<String, String> mPresignedCache;

    private ChatViewModel mChatViewModel;

    private RecyclerView mRecyclerView;
    private SnapHelper mSnapHelper;

    private LinearLayoutManager mLinearLayoutManager;
    private PagingViewAdapter mPagingViewAdapter;


    private AWSCertificate mAWSCertificate;

    private boolean mIsLoadingMore;
    private boolean mIsInitialScroll;

    private String mChatId;
    private String mChatType;

    private MessageView mImageToDownload;
    private boolean mCanScroll = true;

    private Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_chat_image_gallery, container, false);
        mRecyclerView = v.findViewById(R.id.recycler_view);

        View downloadButton = v.findViewById(R.id.download);
        downloadButton.setOnClickListener(this);

        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return mCanScroll && super.canScrollHorizontally();
            }
        };
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
        return v;
    }

    @Override
    protected int getFragmentSoftInputMode() {
        return WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mChatViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(ChatViewModel.class);
        mAWSCertificate = mChatViewModel.getAWSCertificate();

        mChatId = getArguments().getString(KEY_CHAT_JID);
        mChatType = getArguments().getString(KEY_CHAT_TYPE);

        Date date = (Date) getArguments().getSerializable(KEY_MESSAGE_CREATED_AT);

        mPagingViewAdapter = new PagingViewAdapter();
        mRecyclerView.setAdapter(mPagingViewAdapter);

        mRecyclerView.addOnScrollListener(new WPRecyclerScrollMoreListener(mLinearLayoutManager, this));
        mRecyclerView.setRecyclerListener(holder -> {
            ((GalleryViewHolder) holder).scaleImageView.recycle();
        });

        mChatViewModel.getImagePosition(mChatId, date).observe(getViewLifecycleOwner(), (position) -> {
            mChatViewModel.getImageMessages(mChatId).observe(getViewLifecycleOwner(), (pagedList) -> {
                mPagingViewAdapter.submitList(pagedList);
                if (!mIsInitialScroll) {
                    mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
                    mIsInitialScroll = true;
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = grantResults.length > 0;
        for (int grantResult : grantResults) {
            granted &= grantResult == PackageManager.PERMISSION_GRANTED;
        }

        if (granted) {
            saveImage(mImageToDownload);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.permission_dialog_image_crop_title);
            builder.setMessage(R.string.permission_dialog_image_crop_desc);
            builder.setPositiveButton(R.string.permission_dialog_go_to_settings, (dialog, which) -> {
                startActivity(InputUtils.createAppSettingsIntent(getContext()));
            });
            builder.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                dialog.dismiss();
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view:
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
                break;
            case R.id.download:
                View snappedView = mSnapHelper.findSnapView(mLinearLayoutManager);
                mImageToDownload = mPagingViewAdapter.getCurrentList()
                        .get(mLinearLayoutManager.getPosition(snappedView));

                List<String> requiredPermissions = new ArrayList<>();
                requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                Iterator<String> ite = requiredPermissions.iterator();
                while (ite.hasNext()) {
                    if (ContextCompat.checkSelfPermission(getContext(), ite.next())
                            == PackageManager.PERMISSION_GRANTED) {
                        ite.remove();
                    }
                }

                if (requiredPermissions.isEmpty()) {
                    saveImage(mImageToDownload);
                } else {
                    requestPermissions(requiredPermissions.toArray(new String[]{}), REQUEST_PERMISSION_DOWNLOAD);
                }
                break;
        }
    }

    private void saveImage(MessageView messageView) {
        AsyncTask.execute(() -> {
            boolean isError = false;
            try {
                mMainThreadHandler.post(() -> showLoadingDialog(Resource.loading(null)));
                FutureTarget<File> futureTarget = Glide.with(this)
                        .downloadOnly()
                        .load(mPresignedCache.get(messageView.getMessageId()))
                        .submit();
                File file = futureTarget.get();

                String resultUrl = null;
                if (file != null) {
                    resultUrl = saveImageToGallery(file,
                            messageView.getMessageId(),
                            messageView.getMessageSubject());

                    Glide.with(this).clear(futureTarget);
                }

                isError = resultUrl == null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                isError = true;
            } catch (ExecutionException e) {
                e.printStackTrace();
                isError = true;
            } finally {
                final boolean tmpError = isError;
                mMainThreadHandler.post(() -> {
                    showLoadingDialog(Resource.success(null));
                    showDownloadResultToast(tmpError);
                });
            }
        });
    }

    @WorkerThread
    private String saveImageToGallery(@NonNull File source, String title, String description) {
        long now = System.currentTimeMillis();
        ContentResolver cr = getContext().getContentResolver();

        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DESCRIPTION, description);
        values.put(Images.Media.MIME_TYPE, "image/png");
        values.put(Images.Media.DATE_ADDED, now);
        values.put(Images.Media.DATE_TAKEN, now);

        Uri url = null;
        String stringUrl = null;

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try (InputStream in = new FileInputStream(source)) {
                try (OutputStream out = cr.openOutputStream(url)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    private void showDownloadResultToast(boolean isError) {
        View layout = getLayoutInflater().inflate(R.layout.image_download_toast, mRecyclerView, false);
        ImageView imageView = layout.findViewById(R.id.icon);
        TextView text = layout.findViewById(R.id.text);
        if (isError) {
            imageView.setImageResource(R.drawable.icon_download_failed);
            text.setText(R.string.gallery_download_failed);
        } else {
            imageView.setImageResource(R.drawable.icon_download_success);
            text.setText(R.string.gallery_download_success);
        }

        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        InputUtils.setDarkStatusBarColor(getActivity());
    }

    @Override
    public void onDetach() {
        InputUtils.setLightStatusBarColor(getActivity());
        mChatViewModel.setGalleryShown(false);
        super.onDetach();
    }

    @Override
    public void onLoadMore(int page, int total) {
        if (mIsLoadingMore) {
            return;
        }
        mIsLoadingMore = true;

        MessageView message = mPagingViewAdapter.getCurrentList()
                .get(mPagingViewAdapter.getCurrentList().size() - 1);

        // paging might not be finished yet
        if (message == null) {
            mIsLoadingMore = false;
            mMainThreadHandler.post(() -> onLoadMore(page, total));
        } else {
            String lastMessageId = message.getArchiveId() == null ? "" : message.getArchiveId();

            LiveData<Boolean> loadLiveData = mChatViewModel.loadMoreImages(mChatId,
                    mChatType,
                    lastMessageId);

            loadLiveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean finished) {
                    mIsLoadingMore = false;
                    loadLiveData.removeObserver(this);
                }
            });
        }
    }

    @Override
    public int getMessagesCount() {
        return mPagingViewAdapter.getItemCount();
    }

    private class PagingViewAdapter extends PagedListAdapter<MessageView, GalleryViewHolder> {

        PagingViewAdapter() {
            super(DIFF_CALLBACK);
        }

        @NonNull
        @Override
        public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.chat_gallery_item, parent, false);
            return new GalleryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
            MessageView galleryImage = getItem(position);

            holder.bindTo(galleryImage);
        }
    }

    private static DiffUtil.ItemCallback<MessageView> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MessageView>() {

                @Override
                public boolean areItemsTheSame(MessageView oldItem, MessageView newItem) {
                    return oldItem.getMessageId().equals(newItem.getMessageId());
                }

                @Override
                public boolean areContentsTheSame(MessageView oldItem, MessageView newItem) {
                    return oldItem.getMessageText().equals(newItem.getMessageText());
                }
            };

    private class GalleryViewHolder extends RecyclerView.ViewHolder {
        private SubsamplingScaleImageView scaleImageView;
        private View progressBar;

        GalleryViewHolder(View itemView) {
            super(itemView);
            scaleImageView = itemView.findViewById(R.id.image_view);
            scaleImageView.setOnClickListener(ChatImageGalleryFragment.this);
            scaleImageView.setMinimumDpi(1);
            scaleImageView.setMaximumDpi(1);
            scaleImageView.setMinimumTileDpi(160);
            scaleImageView.setQuickScaleEnabled(false);
            scaleImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
            scaleImageView.setOnStateChangedListener(new DefaultOnStateChangedListener() {
                @Override
                public void onScaleChanged(float newScale, int origin) {
                    mCanScroll = Math.abs(newScale - scaleImageView.getMinScale()) < 0.001f;
                }
            });
            progressBar = itemView.findViewById(R.id.progress);
        }

        public void bindTo(MessageView messageView) {
            // reset views
            progressBar.setVisibility(View.VISIBLE);
            scaleImageView.recycle();
            scaleImageView.invalidate();

            // paging might not be finished yet
            if (messageView == null) {
                return;
            }

            String imageUrl = mPresignedCache.get(messageView.getMessageId());
            if (TextUtils.isEmpty(imageUrl)) {
                AsyncTask.execute(() -> {
                    String presignedUrl = ChatManager.getPresigned(messageView.getMessageText(), mAWSCertificate);
                    mPresignedCache.put(messageView.getMessageId(), presignedUrl);
                    mMainThreadHandler.post(() -> loadImage(scaleImageView, presignedUrl));
                });
            } else {
                loadImage(scaleImageView, imageUrl);
            }
        }

        private void loadImage(SubsamplingScaleImageView imageView, String imageUrl) {
            Glide.with(imageView)
                    .downloadOnly()
                    .load(imageUrl)
                    .into(new CustomViewTarget<SubsamplingScaleImageView, File>(imageView) {
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        }

                        @Override
                        public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                            imageView.setImage(ImageSource.uri(Uri.fromFile(resource))
                                    .tilingEnabled());
                            imageView.setOnImageEventListener(new DefaultOnImageEventListener() {
                                @Override
                                public void onImageLoaded() {
                                    progressBar.setVisibility(View.GONE);
                                    imageView.setOnImageEventListener(null);
                                }
                            });
                        }

                        @Override
                        protected void onResourceCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }
}
