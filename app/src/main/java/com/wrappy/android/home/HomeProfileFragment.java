package com.wrappy.android.home;

import javax.inject.Inject;

import android.support.v7.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.hbb20.CountryCodePicker;
import com.wrappy.android.ImageCropperActivity;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.Resource.Status;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.glide.GlideUtils;
import com.wrappy.android.common.utils.InputUtils;
import com.wrappy.android.server.account.body.request.AccountBody;
import com.wrappy.android.server.account.body.request.FileBody.Type;
import com.wrappy.android.xmpp.XMPPRepository;

import static android.app.Activity.RESULT_OK;

public class HomeProfileFragment extends SubFragment implements View.OnClickListener {

    @Inject
    NavigationManager mNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private HomeViewModel mHomeViewModel;

    private TextView mTextViewShowQR;
    private TextView mTextViewAboutWrappy;
    private TextView mTextViewSecurity;
    private Button mButtonLogout;

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

    private ImageView mImageViewChangePic;
    private ImageView mImageViewChangeBanner;
    private ImageView mImageViewPic;
    private ImageView mImageViewBanner;

    private CountryCodePicker mCountryCodePicker;

    private String mFrom;

    private BottomSheetDialog mBottomSheetDialog;

    private Button mButtonEditProfile;

    private AccountBody mAccountBody;

    private String mAvatarUrl;
    private String mBannerUrl;

    private LiveData<Resource<XMPPRepository.ConnectionStatus>> mConnectionStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_home_profile_page, container, false);

        mTextViewShowQR = view.findViewById(R.id.home_profile_button_show_qr);
        mTextViewAboutWrappy = view.findViewById(R.id.home_profile_button_about_wrappy);
        mTextViewSecurity = view.findViewById(R.id.home_profile_button_security);
        mButtonLogout = view.findViewById(R.id.home_profile_button_logout);

        mImageViewChangePic = view.findViewById(R.id.home_profile_imageview_change_pic);
        mImageViewChangeBanner = view.findViewById(R.id.home_profile_imageview_change_banner);
        mImageViewPic = view.findViewById(R.id.home_profile_imageview_profile_pic);
        mImageViewBanner = view.findViewById(R.id.home_profile_imageview_banner);

        mTextViewNameHint = view.findViewById(R.id.home_profile_textview_name_hint);
        mTextViewNameHint.setVisibility(View.GONE);

        mTextViewUserIdHint = view.findViewById(R.id.home_profile_textview_user_id_hint);
        mTextViewUserIdHint.setVisibility(View.GONE);

        mEditTextName = view.findViewById(R.id.home_profile_edittext_name);
        mEditTextUserId = view.findViewById(R.id.home_profile_edittext_user_id);

        mEditTextMobile = view.findViewById(R.id.home_profile_edittext_mobile);
        mCountryCodePicker = view.findViewById(R.id.home_mobile_country_code_picker);
        mCountryCodePicker.registerCarrierNumberEditText(mEditTextMobile);

        mEditTextEmail = view.findViewById(R.id.home_profile_edittext_email);

        mButtonEditProfile = view.findViewById(R.id.home_profile_button_edit_profile);
        mButtonLogout = view.findViewById(R.id.home_profile_button_logout);

        InputUtils.enableView(mEditTextName, false);
        InputUtils.enableView(mEditTextUserId, false);
        InputUtils.enableView(mEditTextMobile, false);
        InputUtils.enableView(mEditTextEmail, false);

        mCountryCodePicker.setCcpClickable(false);
        mCountryCodePicker.setContentColor(mEditTextMobile.getCurrentTextColor());

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

        mHomeViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(HomeViewModel.class);
        mConnectionStatus = mHomeViewModel.getConnectionStatus();

        mHomeViewModel.getAccountInfo().observe(getViewLifecycleOwner(), result -> {
            if (result.data != null) {
                mAccountBody = result.data.getAccountBody();
                mEditTextName.setText(mAccountBody.firstName);
                mEditTextUserId.setText(mAccountBody.extendedInfo.username);
                mCountryCodePicker.setFullNumber(mAccountBody.mobilePhone);
                mEditTextEmail.setText(mAccountBody.email);
            }
        });

        mAvatarUrl = mHomeViewModel.getUserFileUrl(Type.FILE_TYPE_AVATAR);
        mBannerUrl = mHomeViewModel.getUserFileUrl(Type.FILE_TYPE_BACKGROUND);
        loadImages();

        mTextViewShowQR.setOnClickListener(this);
        mTextViewAboutWrappy.setOnClickListener(this);
        mTextViewSecurity.setOnClickListener(this);

        mButtonLogout.setOnClickListener(this);

        mImageViewChangePic.setOnClickListener(this);
        mImageViewChangeBanner.setOnClickListener(this);

        mButtonEditProfile.setOnClickListener(this);
        mButtonLogout.setOnClickListener(this);

        mTextViewPicDialogCamera.setOnClickListener(this);
        mTextViewPicDialogGallery.setOnClickListener(this);
        mTextViewPicDialogStockGallery.setOnClickListener(this);
        mTextViewPicDialogDelete.setOnClickListener(this);
        mTextViewPicDialogCancel.setOnClickListener(this);

    }

    private void loadImages() {
        loadAvatar();
        loadBackroundImage();
    }

    private void loadAvatar() {
        InputUtils.loadAvatarImage(getContext(),
                mImageViewPic,
                mAvatarUrl);
    }

    private void loadBackroundImage() {
        InputUtils.loadBannerImage(getContext(),
                mImageViewBanner,
                mBannerUrl);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                mHomeViewModel.uploadFile(Type.FILE_TYPE_BACKGROUND, Uri.EMPTY)
                        .observe(getViewLifecycleOwner(), result -> {
                            showLoadingDialog(result);
                            if (result.status.equals(Status.SUCCESS)) {
                                mImageViewBanner.setImageDrawable(null);
                            }
                        });
                mBottomSheetDialog.dismiss();
                break;
            case R.id.reg_profile_image_textview_cancel:
                mBottomSheetDialog.dismiss();
                break;
            case R.id.home_profile_button_edit_profile:
                mNavigationManager.showEditProfilePage(
                        mEditTextName.getText().toString(),
                        mEditTextUserId.getText().toString(),
                        mCountryCodePicker.getSelectedCountryCodeWithPlus(),
                        mCountryCodePicker.getFullNumberWithPlus().replace(mCountryCodePicker.getSelectedCountryCodeWithPlus(), ""),
                        mEditTextEmail.getText().toString(),
                        mAvatarUrl,
                        mBannerUrl);
                break;
            case R.id.home_profile_button_show_qr:
                mNavigationManager.showMyQRCodePage(mHomeViewModel.getUserId());
                break;
            case R.id.home_profile_button_about_wrappy:
                mNavigationManager.showAboutPage();
                break;
            case R.id.home_profile_button_security:
                mNavigationManager.showSecurityPage(
                        mAccountBody.extendedInfo.patternPassword,
                        mAccountBody.extendedInfo.patternPasswordFlag);
                break;

            case R.id.home_profile_button_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getString(R.string.confirm_logout));
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.dialog_ok), (dialog, which) -> {
                    //if(mConnectionStatus.getValue().data== XMPPRepository.ConnectionStatus.AUTHENTICATED) {
                        mHomeViewModel.logoutXMPP().observe(getViewLifecycleOwner(), result -> {
                            showLoadingDialog(result);
                            switch (result.status) {
                                case SUCCESS:
                                    dialog.dismiss();
                                    mHomeViewModel.logoutAccount();
                                    break;
                                case LOADING:
                                    break;
                                case CLIENT_ERROR:
                                    break;
                                case SERVER_ERROR:
                                    break;
                            }
                        });
                    //} else {
                    //    dialog.dismiss();
                    //    showAlertDialog(getString(R.string.no_internet_connection),null);
                    //}
                });
                builder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss());
                builder.show();
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (mFrom.equals(ImageCropperActivity.FROM_PROFILE)) {
                mHomeViewModel.uploadFile(Type.FILE_TYPE_AVATAR, imageUri)
                        .observe(getViewLifecycleOwner(), result -> {
                            showLoadingDialog(result);
                            if (result.status.equals(Status.SUCCESS)) {
                                GlideUtils.updateKeyForUrl(mAvatarUrl);
                                loadAvatar();
                            }
                        });
            } else if (mFrom.equals(ImageCropperActivity.FROM_BANNER)) {
                mHomeViewModel.uploadFile(Type.FILE_TYPE_BACKGROUND, imageUri)
                        .observe(getViewLifecycleOwner(), result -> {
                            showLoadingDialog(result);
                            if (result.status.equals(Status.SUCCESS)) {
                                GlideUtils.updateKeyForUrl(mBannerUrl);
                                loadBackroundImage();
                            }
                        });
            }
        }
    }
}
