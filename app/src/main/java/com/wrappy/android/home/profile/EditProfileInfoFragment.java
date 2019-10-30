package com.wrappy.android.home.profile;


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
import com.hbb20.CountryCodePicker;
import com.wrappy.android.ImageCropperActivity;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.glide.GlideUtils;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.server.account.body.request.FileBody.Type;

import static android.app.Activity.RESULT_OK;

public class EditProfileInfoFragment extends SubFragment implements OnClickListener, TextWatcher {
    @Inject
    EditProfileNavigationManager mEditProfileNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    EditProfileViewModel mEditProfileViewModel;

    private ImageView mImageViewChangePic;
    private ImageView mImageViewChangeBanner;
    private ImageView mImageViewPic;
    private ImageView mImageViewBanner;

    private TextView mTextViewPicDialogCamera;
    private TextView mTextViewPicDialogGallery;
    private TextView mTextViewPicDialogStockGallery;
    private TextView mTextViewPicDialogDelete;
    private TextView mTextViewPicDialogCancel;

    private TextView mTextViewNameHint;
    private TextView mTextViewUserIdHint;

    private EditText mEditTextName;
    private EditText mEditTextUserId;
    private EditText mEditTextMobile;
    private EditText mEditTextEmail;

    private CountryCodePicker mCountryCodePicker;

    private Button mButtonEditProfile;

    private BottomSheetDialog mBottomSheetDialog;

    private String mFrom;

    private Uri mProfilePicFile;
    private Uri mBannerPicFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_misc_edit_profile, container, false);

        mImageViewChangePic = view.findViewById(R.id.home_profile_imageview_change_pic);
        mImageViewChangeBanner = view.findViewById(R.id.home_profile_imageview_change_banner);
        mImageViewPic = view.findViewById(R.id.home_profile_imageview_profile_pic);
        mImageViewBanner = view.findViewById(R.id.home_profile_imageview_banner);
        mImageViewChangePic = view.findViewById(R.id.home_profile_imageview_change_pic);
        mImageViewChangeBanner = view.findViewById(R.id.home_profile_imageview_change_banner);

        mTextViewNameHint = view.findViewById(R.id.home_profile_textview_name_hint);
        mTextViewNameHint.setVisibility(View.VISIBLE);

        mTextViewUserIdHint = view.findViewById(R.id.home_profile_textview_user_id_hint);
        mTextViewUserIdHint.setVisibility(View.VISIBLE);

        mEditTextName = view.findViewById(R.id.home_profile_edittext_name);
        mEditTextUserId = view.findViewById(R.id.home_profile_edittext_user_id);
        mEditTextMobile = view.findViewById(R.id.home_profile_edittext_mobile);
        mEditTextEmail = view.findViewById(R.id.home_profile_edittext_email);

        mCountryCodePicker = view.findViewById(R.id.home_mobile_country_code_picker);
        mCountryCodePicker.registerCarrierNumberEditText(mEditTextMobile);

        mButtonEditProfile = view.findViewById(R.id.home_profile_button_edit_profile);
        mButtonEditProfile.setText(getString(R.string.update));

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
        mEditProfileViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(EditProfileViewModel.class);

        mEditTextName.setText(mEditProfileViewModel.getName());
        mEditTextUserId.setText(mEditProfileViewModel.getUserId());
        mEditTextEmail.setText(mEditProfileViewModel.getEmail());

        mCountryCodePicker.setFullNumber(mEditProfileViewModel.getCountryCode() + mEditProfileViewModel.getPhoneNumber());

        if (mEditProfileViewModel.getProfileUrl().equals(
                mEditProfileViewModel.getUserFileUrl(Type.FILE_TYPE_AVATAR))) {
            InputUtils.loadAvatarImage(getContext(),
                    mImageViewPic,
                    mEditProfileViewModel.getUserFileUrl(Type.FILE_TYPE_AVATAR));
        } else {
            Glide.with(this)
                    .load(mEditProfileViewModel.getProfileUrl())
                    .apply(RequestOptions
                            .circleCropTransform()
                            .placeholder(R.drawable.avatar)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true))
                    .into(mImageViewPic);
        }

        if (mEditProfileViewModel.getBannerUrl().equals(
                mEditProfileViewModel.getUserFileUrl(Type.FILE_TYPE_BACKGROUND))) {
            InputUtils.loadBannerImage(getContext(),
                    mImageViewBanner,
                    mEditProfileViewModel.getUserFileUrl(Type.FILE_TYPE_BACKGROUND));
        } else {
            Glide.with(this)
                    .load(mEditProfileViewModel.getBannerUrl())
                    .apply(RequestOptions
                            .diskCacheStrategyOf(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true))
                    .into(mImageViewBanner);
        }

        mButtonEditProfile.setOnClickListener(this);

        mImageViewChangePic.setOnClickListener(this);
        mImageViewChangeBanner.setOnClickListener(this);

        mTextViewPicDialogCamera.setOnClickListener(this);
        mTextViewPicDialogGallery.setOnClickListener(this);
        mTextViewPicDialogStockGallery.setOnClickListener(this);
        mTextViewPicDialogDelete.setOnClickListener(this);
        mTextViewPicDialogCancel.setOnClickListener(this);

        mEditTextName.addTextChangedListener(this);
        mEditTextMobile.addTextChangedListener(this);
        mEditTextEmail.addTextChangedListener(this);

        InputUtils.enableView(mEditTextUserId, false);
        InputUtils.enableView(mButtonEditProfile, checkFields());

        if (mEditProfileViewModel.isSmsVerified()) {
            updateUserProfile();
        }
    }

    private boolean checkFields() {
        boolean isComplete = mEditTextName.getText().toString().length() > 0 &&
                mEditTextMobile.getText().toString().length() > 0 &&
                mEditTextEmail.getText().toString().length() > 0;

        boolean hasChanged =
                checkEmailChanged()
                        || checkNameChanged()
                        || checkMobilePhoneChanged()
                        || mBannerPicFile != null
                        || mProfilePicFile != null;

        return isComplete && hasChanged;
    }

    private boolean checkNameChanged() {
        return !mEditTextName.getText().toString().equals(mEditProfileViewModel.getName());
    }

    private boolean checkEmailChanged() {
        return !mEditTextEmail.getText().toString().equals(mEditProfileViewModel.getEmail());
    }

    private boolean checkMobilePhoneChanged() {
        return !mCountryCodePicker.getSelectedCountryCodeWithPlus().equals(mEditProfileViewModel.getCountryCode())
                || !mCountryCodePicker.getFullNumberWithPlus().replace(mCountryCodePicker.getSelectedCountryCodeWithPlus(), "").equals(mEditProfileViewModel.getPhoneNumber());
    }

    private void validateEmail() {
        String email = mEditTextEmail.getText().toString();
        if (mEditProfileViewModel.getEmail().equals(email)) {
            validateMobileNumber();
        } else {
            if (InputUtils.isValidEmail(email)) {
                mEditProfileViewModel.validateEmail(email).observe(this,
                        result -> {
                            showLoadingDialog(result);
                            switch (result.status) {
                                case SUCCESS:
                                    mEditProfileViewModel.setEmail(email);
                                    validateMobileNumber();
                                    break;
                                case CLIENT_ERROR:
                                case SERVER_ERROR:
                                    showAlertDialog(result.message, null);
                                    break;
                            }
                        });
            } else {
                showAlertDialog(getString(R.string.please_enter_valid_email), null);
            }
        }
    }

    private void validateMobileNumber() {
        String countryCode = mCountryCodePicker.getSelectedCountryCodeWithPlus();
        String phoneNumber = mCountryCodePicker.getFullNumberWithPlus().replace(countryCode, "");
        if (mEditProfileViewModel.getCountryCode().equals(countryCode)
                && mEditProfileViewModel.getPhoneNumber().equals(phoneNumber)) {
            updateUserProfile();
        } else {
            if (mCountryCodePicker.isValidFullNumber()) {
                mEditProfileViewModel.validatePhoneAndSendSmsCode(countryCode, phoneNumber)
                        .observe(this, response -> {
                            showLoadingDialog(response);
                            switch (response.status) {
                                case SUCCESS:
                                    mEditProfileNavigationManager.showEditProfileSmsVerificationPage(countryCode, phoneNumber);
                                    break;
                                case SERVER_ERROR:
                                case CLIENT_ERROR:
                                    showAlertDialog(response.message, null);
                                    break;
                            }
                        });
            } else {
                showAlertDialog(getString(R.string.error_invalid_phone_number), null);
            }
        }
    }

    private void updateUserProfile() {
        mEditProfileViewModel.updateAccount(
                mEditTextName.getText().toString(),
                mEditTextEmail.getText().toString(),
                mCountryCodePicker.getFullNumberWithPlus(),
                mProfilePicFile,
                mBannerPicFile)
                .observe(this, result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case SUCCESS:
                            if (mProfilePicFile != null) {
                                GlideUtils.updateKeyForUrl(mEditProfileViewModel.getUserFileUrl(Type.FILE_TYPE_AVATAR));
                            }
                            if (mBannerPicFile != null) {
                                GlideUtils.updateKeyForUrl(mEditProfileViewModel.getUserFileUrl(Type.FILE_TYPE_BACKGROUND));
                            }
                            getActivity().onBackPressed();
                            break;
                        case CLIENT_ERROR:
                        case SERVER_ERROR:
                            showAlertDialog(result.message, null);
                            break;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_profile_button_edit_profile:
                mEditProfileViewModel.setName(mEditTextName.getText().toString());
                validateEmail();
                break;
            case R.id.home_profile_imageview_change_pic:
                mFrom = ImageCropperActivity.FROM_PROFILE;
//                mTextViewPicDialogStockGallery.setVisibility(View.VISIBLE);
                mTextViewPicDialogDelete.setVisibility(View.GONE);
                mBottomSheetDialog.show();
                break;

            case R.id.home_profile_imageview_change_banner:
                mFrom = ImageCropperActivity.FROM_BANNER;
//                mTextViewPicDialogStockGallery.setVisibility(View.GONE);
                mTextViewPicDialogDelete.setVisibility(View.VISIBLE);
                InputUtils.enableView(mTextViewPicDialogDelete, mImageViewBanner.getDrawable() != null);
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
                mBannerPicFile = Uri.EMPTY;
                mImageViewBanner.setImageDrawable(null);
                InputUtils.enableView(mButtonEditProfile, checkFields());
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
            if (mFrom.equals(ImageCropperActivity.FROM_PROFILE)) {
                mProfilePicFile = data.getData();
                mEditProfileViewModel.setProfileUrl(String.valueOf(mProfilePicFile));
                Glide.with(this)
                        .load(data.getData())
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.avatar)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .into(mImageViewPic);
            } else if (mFrom.equals(ImageCropperActivity.FROM_BANNER)) {
                mBannerPicFile = data.getData();
                mEditProfileViewModel.setBannerUrl(String.valueOf(mBannerPicFile));
                Glide.with(this)
                        .load(data.getData())
                        .apply(RequestOptions
                                .diskCacheStrategyOf(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .centerCrop())
                        .into(mImageViewBanner);
            }
            InputUtils.enableView(mButtonEditProfile, checkFields());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        InputUtils.enableView(mButtonEditProfile, checkFields());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
