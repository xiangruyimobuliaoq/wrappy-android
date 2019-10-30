package com.wrappy.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.steelkiwi.cropiwa.AspectRatio;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;
import com.steelkiwi.cropiwa.shape.CropIwaOvalShape;
import com.wrappy.android.common.utils.InputUtils;

public class ImageCropperActivity extends AppCompatActivity implements View.OnClickListener, CropIwaView.CropSaveCompleteListener, CropIwaView.ErrorListener {

    private Button mButtonCancel;
    private Button mButtonSubmit;

    private CropIwaView mCropIwaViewCropper;

    private String mType;
    private String mFrom;

    public static final String KEY_TYPE = "intent_indicator";
    public static final String KEY_FROM = "indicator";

    public static final String FROM_PROFILE = "profile";
    public static final String FROM_BANNER = "banner";
    public static final String FROM_BACKGROUND = "background";

    public static final String TYPE_CAMERA = "camera";
    public static final String TYPE_GALLERY = "gallery";
    public static final String TYPE_STOCK = "stock";

    private static final int REQUEST_PERMISSION = 0;
    private static final int REQUEST_ACTIVITY_CAMERA = 0;
    private static final int REQUEST_ACTIVITY_GALLERY = 1;
    private static final int REQUEST_ACTIVITY_STOCK = 2;

    private File mPictureFile;

    public static Intent createIntent(Context context, String from, String type) {
        Intent intent = new Intent(context, ImageCropperActivity.class);
        intent.putExtra(KEY_TYPE, type);
        intent.putExtra(KEY_FROM, from);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropper);
        setSupportActionBar(findViewById(R.id.image_cropper_toolbar));

        mButtonCancel = findViewById(R.id.image_cropper_button_cancel);
        mButtonCancel.setOnClickListener(this);

        mButtonSubmit = findViewById(R.id.image_cropper_button_submit);
        mButtonSubmit.setOnClickListener(this);

        mCropIwaViewCropper = findViewById(R.id.image_cropper_cropiwview_cropper);
        mCropIwaViewCropper.configureImage()
                .setMinScale(0.01f)
                .apply();
        mCropIwaViewCropper.setCropSaveCompleteListener(this);
        mCropIwaViewCropper.setErrorListener(this);

        mType = getIntent().getStringExtra(KEY_TYPE);
        mFrom = getIntent().getStringExtra(KEY_FROM);

        switch (mFrom) {
            case FROM_PROFILE:
                mCropIwaViewCropper.configureOverlay()
                        .setMinWidth(0)
                        .setMinHeight(0)
                        .setAspectRatio(new AspectRatio(1,1))
                        .setCropShape(new CropIwaOvalShape(mCropIwaViewCropper.configureOverlay()))
                        .apply();
                break;
            case FROM_BANNER:
                mCropIwaViewCropper.configureOverlay()
                        .setMinWidth(0)
                        .setMinHeight(0)
                        .setAspectRatio(new AspectRatio(360, 190))
                        .setCornerColor(R.color.colorPrimary)
                        .setCornerStrokeWidth(5)
                        .apply();
                break;

            case FROM_BACKGROUND:
                mCropIwaViewCropper.configureOverlay()
                        .setMinHeight(0)
                        .setMinWidth(0)
                        .setAspectRatio(new AspectRatio(90,160))
                        .apply();

        }

        mPictureFile = new File(getExternalCacheDir() + "/temp.png");

        List<String> requiredPermissions = new ArrayList<>();
        requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        requiredPermissions.add(Manifest.permission.CAMERA);
        Iterator<String> ite = requiredPermissions.iterator();
        while (ite.hasNext()) {
            if (ContextCompat.checkSelfPermission(this, ite.next())
                    == PackageManager.PERMISSION_GRANTED) {
                ite.remove();
            }
        }

        if (requiredPermissions.isEmpty()) {
            launchCameraOrPicker();
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[]{}), REQUEST_PERMISSION);
        }
    }

    private void launchCameraOrPicker() {
        if (mType.equals(TYPE_CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", mPictureFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(takePictureIntent, REQUEST_ACTIVITY_CAMERA);
        } else if (mType.equals(TYPE_GALLERY)) {
            Intent selectPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(selectPictureIntent, REQUEST_ACTIVITY_GALLERY);
        } else if (mType.equals(TYPE_STOCK)) {
            Intent intent = new Intent(this, StockImageChooserGallery.class);
            startActivityForResult(intent, REQUEST_ACTIVITY_STOCK);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean granted = grantResults.length > 0;
            for (int grantResult : grantResults) {
                granted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (granted) {
                launchCameraOrPicker();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_dialog_image_crop_title);
                builder.setMessage(R.string.permission_dialog_image_crop_desc);
                builder.setPositiveButton(R.string.permission_dialog_go_to_settings, (dialog, which) -> {
                    startActivity(InputUtils.createAppSettingsIntent(ImageCropperActivity.this));
                    setResult(RESULT_CANCELED);
                    finish();
                });
                builder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> {
                    dialog.dismiss();
                    setResult(RESULT_CANCELED);
                    finish();
                });
                builder.setCancelable(false);
                builder.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.e("Request_Code", String.valueOf(requestCode));
            switch (requestCode) {
                case REQUEST_ACTIVITY_CAMERA:
                    mCropIwaViewCropper.setImageUri(FileProvider.getUriForFile(this, getPackageName() + ".provider", mPictureFile));
                    break;
                case REQUEST_ACTIVITY_GALLERY:
                case REQUEST_ACTIVITY_STOCK:
                    if (data != null) {
                        mCropIwaViewCropper.setImageUri(data.getData());
                    } else {
                        finish();
                    }
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_cropper_button_submit:
                File folder = getCacheDir();
                if (mFrom.equals("profile")) {
                    File img = new File(folder, "profpic.png");
                    Uri uri = Uri.fromFile(img);
                    mCropIwaViewCropper.crop(new CropIwaSaveConfig.Builder(uri)
                            .setCompressFormat(Bitmap.CompressFormat.PNG)
                            .setSize(180, 180)
                            .setQuality(100)
                            .build());
                } else if (mFrom.equals("banner")) {
                    File img = new File(folder, "banner.png");
                    Uri uri = Uri.fromFile(img);
                    mCropIwaViewCropper.crop(new CropIwaSaveConfig.Builder(uri)
                            .setCompressFormat(Bitmap.CompressFormat.PNG)
                            .setSize(540, 285)
                            .setQuality(100)
                            .build());
                } else if (mFrom.equals("background")) {
                    File img = new File(folder, "background.png");
                    Uri uri = Uri.fromFile(img);
                    mCropIwaViewCropper.crop(new CropIwaSaveConfig.Builder(uri)
                            .setCompressFormat(Bitmap.CompressFormat.PNG)
                            .setSize(900, 1600)
                            .setQuality(100)
                            .build());
                }

                break;

            case R.id.image_cropper_button_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;

        }
    }

    @Override
    public void onCroppedRegionSaved(Uri bitmapUri) {
        setResult(RESULT_OK, new Intent().setData(bitmapUri));
        finish();
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }
}
