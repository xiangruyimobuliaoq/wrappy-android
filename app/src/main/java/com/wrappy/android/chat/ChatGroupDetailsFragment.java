package com.wrappy.android.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import com.wrappy.android.ImageCropperActivity;
import com.wrappy.android.R;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.Resource.Status;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.glide.GlideUtils;
import com.wrappy.android.common.ui.WrappyFilteredEditText;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.db.entity.Chat;
import com.wrappy.android.db.entity.ChatAndBackground;
import com.wrappy.android.server.account.body.request.FileBody.Type;
import com.wrappy.android.xmpp.ContactManager;
import com.wrappy.android.xmpp.XMPPRepository;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static com.wrappy.android.chat.ChatFragment.KEY_JID;

public class ChatGroupDetailsFragment extends SubFragment implements View.OnClickListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    ChatNavigationManager mChatNavigationManager;

    ChatViewModel mChatViewModel;

    private ChatAndBackground mChat;

    private Button mButtonSave;

    private WrappyFilteredEditText mEditTextGroupName;

    private ImageView mImageViewAvatar;
    private ImageView mImageViewChangeAvatar;
    private ImageView mImageViewBanner;
    private ImageView mImageViewChangeBanner;

    private BottomSheetDialog mBottomSheetDialog;

    private TextView mTextViewPicDialogCamera;
    private TextView mTextViewPicDialogGallery;
    private TextView mTextViewPicDialogCancel;

    private String mFrom;
    private String mGroupFileId;
    private String mAvatarUrl;
    private String mBannerUrl;

    private LiveData<Resource<XMPPRepository.ConnectionStatus>> mConnectionStatus;

    public static ChatGroupDetailsFragment create(String chatJid) {
        ChatGroupDetailsFragment chatGroupDetailsFragment = new ChatGroupDetailsFragment();
        Bundle args = new Bundle();
        args.putString(KEY_JID, chatJid);
        chatGroupDetailsFragment.setArguments(args);
        return chatGroupDetailsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat_group_details_page, container, false);

        mButtonSave = view.findViewById(R.id.chat_group_details_button_save);

        mEditTextGroupName = view.findViewById(R.id.chat_group_details_edittext_name);

        mImageViewAvatar = view.findViewById(R.id.chat_group_details_imageview_profile_pic);
        mImageViewChangeAvatar = view.findViewById(R.id.chat_group_details_imageview_change_pic);
        mImageViewBanner = view.findViewById(R.id.chat_group_details_imageview_banner);
        mImageViewChangeBanner = view.findViewById(R.id.chat_group_details_imageview_change_banner);

        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = inflater.inflate(R.layout.frag_chat_group_details_dialog, null);

        mBottomSheetDialog.setContentView(sheetView);

        mTextViewPicDialogCamera = sheetView.findViewById(R.id.reg_profile_image_textview_take);
        mTextViewPicDialogGallery = sheetView.findViewById(R.id.reg_profile_image_textview_select);
        mTextViewPicDialogCancel = sheetView.findViewById(R.id.reg_profile_image_textview_cancel);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getInjector().inject(this);

        mChatViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ChatViewModel.class);
        mConnectionStatus = mChatViewModel.getConnectionStatus();

        String fullJid = getArguments().getString(KEY_JID);
        mGroupFileId = ContactManager.getUserName(fullJid);
        mChatViewModel.getChat(fullJid).observe(this, result -> {
            showLoadingDialog(result);
            switch(result.status) {
                case SUCCESS:
                    initChatGroupDetails(result.data);
                    setToolbarTitle(mChat.getChatName());
                    break;
            }
        });

        mAvatarUrl = mChatViewModel.getContactFileUrl(Type.FILE_TYPE_AVATAR, mGroupFileId, true);
        mBannerUrl = mChatViewModel.getContactFileUrl(Type.FILE_TYPE_BACKGROUND, mGroupFileId, true);
        loadAvatar(mImageViewAvatar);
        loadBackroundImage();

        mButtonSave.setOnClickListener(this);

        mImageViewChangeAvatar.setOnClickListener(this);
        mImageViewChangeBanner.setOnClickListener(this);

        mTextViewPicDialogCamera.setOnClickListener(this);
        mTextViewPicDialogGallery.setOnClickListener(this);
        mTextViewPicDialogCancel.setOnClickListener(this);
    }

    private void loadAvatar(ImageView target) {
        InputUtils.loadAvatarImage(getContext(),
                target,
                mAvatarUrl);
    }

    private void loadBackroundImage() {
        InputUtils.loadBannerImage(getContext(),
                mImageViewBanner,
                mBannerUrl);
    }

    public void initChatGroupDetails(ChatAndBackground chat) {
        mChat = chat;
        mEditTextGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0 && !s.toString().trim().isEmpty()) {
                    mButtonSave.setEnabled(true);
                } else {
                    mButtonSave.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.chat_group_details_button_save:
                if(mConnectionStatus.getValue().data== XMPPRepository.ConnectionStatus.AUTHENTICATED) {
                    mChatViewModel.setRoomName(mEditTextGroupName.getText().toString());

                    setToolbarTitle(mEditTextGroupName.getText().toString());

                    getFragmentManager().popBackStack();
                } else {
                    showAlertDialog(getString(R.string.no_internet_connection), null);
                }
                break;

            case R.id.chat_group_details_imageview_change_pic:
                mFrom = ImageCropperActivity.FROM_PROFILE;
                mBottomSheetDialog.show();
                break;

            case R.id.chat_group_details_imageview_change_banner:
                mFrom = ImageCropperActivity.FROM_BANNER;
                mBottomSheetDialog.show();
                break;

            case R.id.reg_profile_image_textview_take:
                startActivityForResult(ImageCropperActivity.createIntent(getContext(),
                        mFrom,
                        ImageCropperActivity.TYPE_CAMERA),
                        0);
                mBottomSheetDialog.dismiss();
                break;

            case R.id.reg_profile_image_textview_select:
                startActivityForResult(ImageCropperActivity.createIntent(getContext(),
                        mFrom,
                        ImageCropperActivity.TYPE_GALLERY),
                        0);
                mBottomSheetDialog.dismiss();
                break;

            case R.id.reg_profile_image_textview_cancel:
                mBottomSheetDialog.dismiss();
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if(mConnectionStatus.getValue().data== XMPPRepository.ConnectionStatus.AUTHENTICATED) {
                Uri imageUri = data.getData();
                if (mFrom.equals(ImageCropperActivity.FROM_PROFILE)) {
                    mChatViewModel.saveGroupFile(mGroupFileId, Type.FILE_TYPE_AVATAR, imageUri)
                            .observe(this, result -> {
                                showLoadingDialog(result);
                                if (result.status.equals(Status.SUCCESS)) {
                                    GlideUtils.updateKeyForUrl(mAvatarUrl);

                                    loadAvatar(mImageViewAvatar);
                                }
                            });
                } else if (mFrom.equals(ImageCropperActivity.FROM_BANNER)) {
                    mChatViewModel.saveGroupFile(mGroupFileId, Type.FILE_TYPE_BACKGROUND, imageUri)
                            .observe(this, result -> {
                                showLoadingDialog(result);
                                if (result.status.equals(Status.SUCCESS)) {
                                    GlideUtils.updateKeyForUrl(mBannerUrl);
                                    loadBackroundImage();
                                }
                            });
                }
            } else {
                showAlertDialog(getString(R.string.no_internet_connection), null);
            }
        }
    }
}
