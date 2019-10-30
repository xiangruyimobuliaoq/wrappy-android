package com.wrappy.android.common.ui;


import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.hbb20.CountryCodePicker;
import com.wrappy.android.R;
import com.wrappy.android.common.Resource;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;

public abstract class AbstractSmsVerificationFragment extends SubFragment implements OnClickListener, OnFocusChangeListener, TextWatcher {
    private static final int SMS_CODE_LENGTH = 5;

    private TextView mTextViewResend;
    private TextView mTextViewValidateError;

    private EditText[] mEditTextCode;
    private int mCurrentCodeBox;

    private EditText mEditTextMobileNumber;

    private Button mButtonNext;

    private CountryCodePicker mCountryCodePicker;

    protected abstract void onActivityCreatedInternal();

    protected abstract String getToolbarTitle();

    protected abstract String getFullPhoneNumber();

    protected abstract LiveData<Resource<Boolean>> onValidateSmsCodeRequest(String smsCode);

    protected abstract LiveData<Resource<Boolean>> onResendSmsCodeRequest();

    protected abstract void onPhoneNumberChanged(String countryCode, String phoneNumber);

    protected abstract void onValidateSmsSuccess();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_reg_smsver_page, container, false);

        mTextViewResend = view.findViewById(R.id.reg_smsver_textview_resend);
        mTextViewValidateError = view.findViewById(R.id.reg_smsver_textview_error);
        mEditTextMobileNumber = view.findViewById(R.id.reg_smsver_edittext_mobile);

        mCountryCodePicker = view.findViewById(R.id.reg_smsver_country_code_picker);
        mCountryCodePicker.registerCarrierNumberEditText(mEditTextMobileNumber);

        view.findViewById(R.id.reg_smsver_mobile_edit_button).setOnClickListener(this);
        enableMobileNumberEditing(false);

        mEditTextCode = new EditText[SMS_CODE_LENGTH];
        for (int x = 0; x < mEditTextCode.length; x++) {
            mEditTextCode[x] = view.findViewById(getResources()
                    .getIdentifier("reg_smsver_edittext_auth" + (x + 1),
                            "id",
                            getContext().getPackageName()));
        }

        mButtonNext = view.findViewById(R.id.reg_smsver_button_done);
        InputUtils.enableView(mButtonNext, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onActivityCreatedInternal();

        mCountryCodePicker.setFullNumber(getFullPhoneNumber());

        setToolbarTitle(getToolbarTitle());

        mTextViewResend.setOnClickListener(this);

        for (int x = 0; x < mEditTextCode.length; x++) {
            mEditTextCode[x].setTag(x);
            mEditTextCode[x].setOnFocusChangeListener(this);
            mEditTextCode[x].addTextChangedListener(this);
            mEditTextCode[x].setCustomSelectionActionModeCallback(new Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }
            });
        }

        mButtonNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_smsver_button_done:
                setPhoneNumber();
                enableMobileNumberEditing(false);
                if (mCountryCodePicker.isValidFullNumber()) {
                    onValidateSmsCodeRequest(getSmsCodeText()).observe(this, result -> {
                        showLoadingDialog(result);
                        switch (result.status) {
                            case SUCCESS:
                                onValidateSmsSuccess();
                                break;
                            case CLIENT_ERROR:
                                showAlertDialog(result.message, null);
                                break;
                            case SERVER_ERROR:
                                // TODO: put dilaw error background
                                changeSMSBoxError();
                                mTextViewValidateError.setText(result.message);
                                break;
                        }
                    });
                } else {
                    mTextViewValidateError.setText(getString(R.string.error_invalid_phone_number));
                }
                break;
            case R.id.reg_smsver_textview_resend:
                setPhoneNumber();
                enableMobileNumberEditing(false);
                if (mCountryCodePicker.isValidFullNumber()) {
                    onResendSmsCodeRequest().observe(this, result -> {
                        showLoadingDialog(result);
                        switch (result.status) {
                            case CLIENT_ERROR:
                            case SERVER_ERROR:
                                showAlertDialog(result.message, null);
                                break;
                        }
                    });
                } else {
                    showAlertDialog(getString(R.string.error_invalid_phone_number), null);
                }
                break;
            case R.id.reg_smsver_mobile_edit_button:
                enableMobileNumberEditing(true);
                break;
        }
    }

    private String getSmsCodeText() {
        StringBuilder code = new StringBuilder();
        for (EditText e : mEditTextCode) {
            code.append(e.getText().toString());
        }
        return code.toString();
    }

    private void setPhoneNumber() {
        String countryCode = mCountryCodePicker.getSelectedCountryCodeWithPlus();
        String phoneNumber = mCountryCodePicker.getFullNumberWithPlus().replace(countryCode, "");
        onPhoneNumberChanged(countryCode, phoneNumber);
    }

    private void enableMobileNumberEditing(boolean enable) {
        InputUtils.enableView(mEditTextMobileNumber, enable);
        if (enable) {
            mEditTextMobileNumber.requestFocus();
            // force focus by emulating touch action
            mEditTextMobileNumber.dispatchTouchEvent(MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    mEditTextMobileNumber.getWidth(), 0, 0));
            mEditTextMobileNumber.dispatchTouchEvent(MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP,
                    mEditTextMobileNumber.getWidth(), 0, 0));
        } else {
            mEditTextMobileNumber.clearFocus();
        }
        mCountryCodePicker.setCcpClickable(enable);
    }

    private void changeSMSBoxError() {
        for(int x = 0; x < mEditTextCode.length; x++) {
            mEditTextCode[x].setBackgroundResource(R.drawable.reg_smsver_edittext_auth_error_bg);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        InputUtils.enableView(mButtonNext, getSmsCodeText().length() >= mEditTextCode.length);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 1) {
            mEditTextCode[mCurrentCodeBox].setText(
                    s.subSequence(s.length() - 1, s.length()));
            return;
        } else if (s.length() == 0) {
            return;
        }

        if (mCurrentCodeBox < mEditTextCode.length - 1) {
            mEditTextCode[mCurrentCodeBox + 1].requestFocus();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            enableMobileNumberEditing(false);
            mCurrentCodeBox = (int) v.getTag();
        }
    }
}
