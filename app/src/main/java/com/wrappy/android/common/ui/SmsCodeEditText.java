package com.wrappy.android.common.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;


public class SmsCodeEditText extends AppCompatEditText {
    public SmsCodeEditText(Context context) {
        super(context);
    }

    public SmsCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmsCodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onSelectionChanged(int start, int end) {

        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }

        super.onSelectionChanged(start, end);
    }
}
