package com.wrappy.android.common.ui;


import java.util.ArrayList;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.itsxtt.patternlock.PatternLockView;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;

public abstract class AbstractCreatePatternFragment extends SubFragment {
    private String mPatternToConfirm;

    private PatternLockView mPatternLockViewPattern;

    private TextView mTextViewLabel;
    private TextView mTextViewPatternError;

    protected abstract void onActivityCreatedInternal();

    protected abstract void onPatternConfirmed(String pattern);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_security_pattern_page, container, false);

        mPatternLockViewPattern = view.findViewById(R.id.sec_pattern_patternlockview_pattern);
        mTextViewLabel = view.findViewById(R.id.sec_pattern_textview_label);
        mTextViewPatternError = view.findViewById(R.id.sec_pattern_error_text);

        return view;
    }

    protected void setErrorText(String message) {
        mTextViewPatternError.setText(message);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onActivityCreatedInternal();

        mTextViewLabel.setText(R.string.enter_new_pattern);

        mPatternLockViewPattern.setOnPatternListener(new PatternLockView.OnPatternListener() {
            @Override
            public void onStarted() {
                mTextViewPatternError.setText("");
            }

            @Override
            public void onProgress(ArrayList<Integer> arrayList) {

            }

            @Override
            public boolean onComplete(ArrayList<Integer> arrayList) {
                if (arrayList.size() < 4) {
                    mTextViewPatternError.setText(R.string.pattern_error_incorrect);
                    return false;
                }
                String pattern = "";
                for (Integer i : arrayList) {
                    pattern += String.valueOf(i);
                }
                if (!TextUtils.isEmpty(mPatternToConfirm)) {
                    if (!mPatternToConfirm.equals(pattern)) {
                        mTextViewPatternError.setText(R.string.pattern_error_incorrect);
                        return false;
                    }
                    onPatternConfirmed(pattern);
                } else {
                    mTextViewLabel.setText(R.string.confirm_pattern);
                    mPatternToConfirm = pattern;
                }
                return true;
            }
        });
    }

    protected void reset() {
        mPatternToConfirm = "";
        mTextViewLabel.setText(R.string.enter_new_pattern);
        mTextViewPatternError.setText("");
    }

    @Override
    public boolean onBackPressed() {
        if (!TextUtils.isEmpty(mPatternToConfirm)) {
            reset();
            return true;
        }
        return false;
    }
}
