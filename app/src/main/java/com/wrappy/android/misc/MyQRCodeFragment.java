package com.wrappy.android.misc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.wrappy.android.NavigationManager;
import com.wrappy.android.R;
import com.wrappy.android.common.BaseFragment;
import com.wrappy.android.common.zxing.WrappyQRCaptureActivity;
import com.wrappy.android.contact.ContactFragment;

import javax.inject.Inject;

public class MyQRCodeFragment extends BaseFragment implements View.OnClickListener {
    private static final int QR_SIZE_DP = 250; //dp

    @Inject
    NavigationManager mNavigationManager;

    private static final String KEY_JID = "JID";

    private Button mButtonReader;

    private Toolbar mToolbar;

    private ImageView mImageViewQRCode;

    private String mUserID;

    private int mQrSizeInPixels;

    public static MyQRCodeFragment create(String userJid) {
        MyQRCodeFragment myQRCodeFragment = new MyQRCodeFragment();
        Bundle args = new Bundle();
        args.putString(KEY_JID, userJid);
        myQRCodeFragment.setArguments(args);
        return myQRCodeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_misc_my_qr, container, false);

        mButtonReader = view.findViewById(R.id.my_qr_button_reader);

        mToolbar = view.findViewById(R.id.qr_toolbar);
        mImageViewQRCode = view.findViewById(R.id.my_qr_imageview_qr);

        mQrSizeInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, QR_SIZE_DP, getResources().getDisplayMetrics());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);

        setToolbar(mToolbar);
        setToolbarTitle(getString(R.string.qr_title));
        showToolbarBackButton(true);

        mButtonReader.setOnClickListener(this);

        mUserID = getArguments().getString(KEY_JID);

        generateQRCode();
    }


    private void generateQRCode(){

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(mUserID, BarcodeFormat.QR_CODE, mQrSizeInPixels, mQrSizeInPixels);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            mImageViewQRCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // QR Code parsing
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            mNavigationManager.showContactPage(ContactFragment.TYPE_ADD, result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {

            case R.id.my_qr_button_reader:
                IntentIntegrator.forSupportFragment(this)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        .setBeepEnabled(false)
                        .setOrientationLocked(true)
                        .setPrompt("")
                        .setCaptureActivity(WrappyQRCaptureActivity.class)
                        .initiateScan();
                break;

        }
    }
}
