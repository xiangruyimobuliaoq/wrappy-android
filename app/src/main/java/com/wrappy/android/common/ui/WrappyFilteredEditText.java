package com.wrappy.android.common.ui;

import java.util.Arrays;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;

/**
 * Taken from https://stackoverflow.com/a/40533446.
 */

public class WrappyFilteredEditText extends TextInputEditText {

    private EmojiExcludeFilter mEmojiExcludeFilter;

    public WrappyFilteredEditText(Context context) {
        super(context);
    }

    public WrappyFilteredEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrappyFilteredEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private InputFilter getEmojiExcludeFilter() {
        if (mEmojiExcludeFilter == null) {
            mEmojiExcludeFilter = new EmojiExcludeFilter();
        }
        return mEmojiExcludeFilter;
    }

    /**
     * Implicitly called by super class
     * Adds emojiExcludeFilter if not added
     */
    @Override
    public void setFilters(InputFilter[] filters) {
        boolean added = false;
        for (InputFilter inputFilter : filters) {
            if (inputFilter == getEmojiExcludeFilter()) {
                added = true;
                break;
            }
        }
        if (!added) {
            filters = Arrays.copyOf(filters, filters.length + 1);
            filters[filters.length - 1] = getEmojiExcludeFilter();
        }
        super.setFilters(filters);
    }

    private static class EmojiExcludeFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder filtered = new StringBuilder();
            for (int i = start; i < end; i++) {
                char character = source.charAt(i);
                int type = Character.getType(character);
                if (!(type == Character.SURROGATE || type == Character.OTHER_SYMBOL)) {
                    filtered.append(character);
                }
            }
            // if filtered = original return null to avoid blocking IME suggestions
            return filtered.length() != (end - start) ? filtered.toString() : null;
        }
    }
}
