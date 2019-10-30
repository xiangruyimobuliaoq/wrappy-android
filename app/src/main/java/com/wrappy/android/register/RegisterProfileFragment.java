package com.wrappy.android.register;

import java.util.Date;
import javax.inject.Inject;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.wrappy.android.ImageCropperActivity;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;

import static android.app.Activity.RESULT_OK;

public class RegisterProfileFragment extends SubFragment implements OnClickListener, TextWatcher {

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    RegisterViewModel mRegisterViewModel;

    public final static String INDICATOR = "indicator";

    public int IMAGE_REQUEST_CODE = 0;

    private TextView mTextViewPicDialogCamera;
    private TextView mTextViewPicDialogGallery;
    private TextView mTextViewPicDialogStockGallery;
    private TextView mTextViewPicDialogDelete;
    private TextView mTextViewPicDialogCancel;

    private EditText mEditTextName;
    private EditText mEditTextUserId;
    private EditText mEditTextMobile;
    private EditText mEditTextEmail;

    private String mFrom;

    private Button mButtonNext;

    private ImageView mImageViewChangePic;
    private ImageView mImageViewChangeBanner;
    private ImageView mImageViewPic;
    private ImageView mImageViewBanner;

    private TextView mTextViewUserIdErrorMessage;
    private TextView mTextViewEmailErrorMessage;

    private BottomSheetDialog mBottomSheetDialog;

    private Uri mProfilePicFile;
    private Uri mBannerPicFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_reg_profile_page, container, false);

        mEditTextName = view.findViewById(R.id.reg_profile_edittext_name);
        mEditTextUserId = view.findViewById(R.id.reg_profile_edittext_user_id);
        mEditTextMobile = view.findViewById(R.id.reg_profile_edittext_mobile);
        mEditTextEmail = view.findViewById(R.id.reg_profile_edittext_email);

        mButtonNext = view.findViewById(R.id.reg_profile_button_next);

        mImageViewChangePic = view.findViewById(R.id.reg_profile_imageview_change_pic);
        mImageViewChangeBanner = view.findViewById(R.id.reg_profile_imageview_change_banner);
        mImageViewPic = view.findViewById(R.id.reg_profile_imageview_profile_pic);
        mImageViewBanner = view.findViewById(R.id.reg_profile_imageview_banner);

        mTextViewEmailErrorMessage = view.findViewById(R.id.reg_profile_email_error);
        mTextViewUserIdErrorMessage = view.findViewById(R.id.reg_profile_user_id_error);

        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = inflater.inflate(R.layout.frag_reg_profile_image_dialog, null);

        mBottomSheetDialog.setContentView(sheetView);

        mTextViewPicDialogCamera = sheetView.findViewById(R.id.reg_profile_image_textview_take);
        mTextViewPicDialogGallery = sheetView.findViewById(R.id.reg_profile_image_textview_select);
        mTextViewPicDialogStockGallery = sheetView.findViewById(R.id.reg_profile_image_textview_select_stock);
        mTextViewPicDialogDelete = sheetView.findViewById(R.id.reg_profile_image_textview_delete);
        mTextViewPicDialogCancel = sheetView.findViewById(R.id.reg_profile_image_textview_cancel);

        mTextViewPicDialogStockGallery.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mRegisterViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(RegisterViewModel.class);

        setToolbarTitle(getString(R.string.reg_profile_toolbar_title));

        mEditTextMobile.setText(getString(R.string.reg_smsver_mobile_phone_format,
                mRegisterViewModel.getCountryCode(),
                mRegisterViewModel.getPhoneNumber()));

        mButtonNext.setOnClickListener(this);
        mImageViewChangePic.setOnClickListener(this);
        mImageViewChangeBanner.setOnClickListener(this);

        mTextViewPicDialogCamera.setOnClickListener(this);
        mTextViewPicDialogGallery.setOnClickListener(this);
        mTextViewPicDialogStockGallery.setOnClickListener(this);
        mTextViewPicDialogDelete.setOnClickListener(this);
        mTextViewPicDialogCancel.setOnClickListener(this);

        mEditTextName.addTextChangedListener(this);
        mEditTextUserId.addTextChangedListener(this);
        mEditTextEmail.addTextChangedListener(this);

        mButtonNext.setOnClickListener(this);
        InputUtils.enableView(mButtonNext, false);
    }

    private void validateUsername() {
        mRegisterViewModel.validateUsername(mEditTextUserId.getText().toString())
                .observe(this, result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case CLIENT_ERROR:
                            showAlertDialog(result.message, null);
                            break;
                        case SERVER_ERROR:
                            mEditTextUserId.requestFocus();
                            mTextViewUserIdErrorMessage.setText(result.message);
                            break;
                        case SUCCESS:
                            validateEmail();
                            break;
                    }
                });
    }

    private void validateEmail() {
        String email = mEditTextEmail.getText().toString();
        if (InputUtils.isValidEmail(email)) {
            mRegisterViewModel.validateEmail(email)
                    .observe(this, result -> {
                        showLoadingDialog(result);
                        switch (result.status) {
                            case CLIENT_ERROR:
                                showAlertDialog(result.message, null);
                                break;
                            case SERVER_ERROR:
                                mEditTextEmail.requestFocus();
                                mTextViewEmailErrorMessage.setText(result.message);
                                break;
                            case SUCCESS:
                                createAccount();
                                break;
                        }
                    });
        } else {
            mTextViewEmailErrorMessage.setText(R.string.please_enter_valid_email);
        }
    }

    private void createAccount() {
        mRegisterViewModel.createAccount(mEditTextName.getText().toString(),
                mEditTextUserId.getText().toString(),
                mEditTextEmail.getText().toString(),
                mProfilePicFile,
                mBannerPicFile)
                .observe(this, result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case CLIENT_ERROR:
                        case SERVER_ERROR:
                            showAlertDialog(result.message, null);
                            break;
                        case SUCCESS:
                            login(result.data.extendedInfo.username, result.data.extendedInfo.clearPassword);
                            break;
                    }
                });
    }

    private void login(String username, String password) {
        mRegisterViewModel.login(username, password)
                .observe(this, result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case SUCCESS:
                            mRegisterViewModel.loginXMPP(username,password);
                            mRegisterViewModel.getConnectionStatus().observe(this, connectionStatus -> {
                                switch (connectionStatus.data) {
                                    // TODO: LOADING SCREEN
                                    case CONNECTED:
                                        break;
                                    case AUTHENTICATED:
                                        mNavigationManager.showHomePage(true);
                                        break;
                                    case RECONNECTING:
                                        break;
                                    case DISCONNECTED:
                                        break;
                                }
                            });
                            break;
                        case CLIENT_ERROR:
                        case SERVER_ERROR:
                            showAlertDialog(result.message, (dialog, which) -> {
                                dialog.dismiss();
                                mNavigationManager.showLoginPage();
                            });
                            break;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_profile_button_next:
                mTextViewUserIdErrorMessage.setText("");
                mTextViewEmailErrorMessage.setText("");
                if (mProfilePicFile != null) {
                    validateUsername();
                } else {
                    showAlertDialog(getResources().getString(R.string.please_choose_your_avatar), null);
                }
                break;
            case R.id.reg_profile_imageview_change_pic:
                mFrom = ImageCropperActivity.FROM_PROFILE;
//                mTextViewPicDialogStockGallery.setVisibility(View.VISIBLE);
                mTextViewPicDialogDelete.setVisibility(View.GONE);
                mBottomSheetDialog.show();
                break;

            case R.id.reg_profile_imageview_change_banner:
                mFrom = ImageCropperActivity.FROM_BANNER;
//                mTextViewPicDialogStockGallery.setVisibility(View.GONE);
                mTextViewPicDialogDelete.setVisibility(View.VISIBLE);
                InputUtils.enableView(mTextViewPicDialogDelete, mBannerPicFile != null);
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

            case R.id.reg_profile_image_textview_select_stock:
                startActivityForResult(ImageCropperActivity.createIntent(getContext(),
                        mFrom,
                        ImageCropperActivity.TYPE_STOCK),
                        0);
                mBottomSheetDialog.dismiss();
                break;

            case R.id.reg_profile_image_textview_delete:
                switch (mFrom) {
                    case ImageCropperActivity.FROM_PROFILE:
                        mProfilePicFile = null;
                        mImageViewPic.setImageDrawable(null);
                    case ImageCropperActivity.FROM_BANNER:
                        mBannerPicFile = null;
                        mImageViewBanner.setImageDrawable(null);
                        break;
                }
                mBottomSheetDialog.dismiss();
                break;

            case R.id.reg_profile_image_textview_cancel:
                mBottomSheetDialog.dismiss();
                break;

        }
    }

    private boolean checkFieldCompleted() {
        return mEditTextName.getText().toString().length() > 0 &&
                mEditTextUserId.getText().toString().length() >= 6 &&
                mEditTextEmail.getText().toString().length() > 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (mFrom.equals(ImageCropperActivity.FROM_PROFILE)) {
                mProfilePicFile = data.getData();
                Glide.with(this)
                        .load(data.getData())
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.avatar)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .into(mImageViewPic);
            } else if (mFrom.equals(ImageCropperActivity.FROM_BANNER)) {
                mBannerPicFile = data.getData();
                Glide.with(this)
                        .load(data.getData())
                        .apply(RequestOptions
                                .diskCacheStrategyOf(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .into(mImageViewBanner);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        InputUtils.enableView(mButtonNext, checkFieldCompleted());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
