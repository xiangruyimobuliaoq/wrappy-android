package com.wrappy.android.common.utils;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.wrappy.android.BuildConfig;
import com.wrappy.android.R;
import com.wrappy.android.common.glide.GlideUtils;

public class InputUtils {

    public static void showSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void enableView(View view, boolean enable) {
        view.setEnabled(enable);
        view.setClickable(enable);
        view.setFocusable(enable);
        if (!(view instanceof Button)) {
            view.setFocusableInTouchMode(enable);
        }
    }


    /**
     * Adds a text filter that filters whitespaces from input.
     * For use in combination with android:inputType values
     * <p>
     * If only alphanumeric input is required, use android:digits instead.
     */
    public static void disableWhitespaceInput(EditText editText) {
        List<InputFilter> filters = new ArrayList<>(Arrays.asList(editText.getFilters()));
        filters.add((source, start, end, dest, dstart, dend) -> {
            StringBuilder filtered = new StringBuilder();
            for (int i = start; i < end; i++) {
                char character = source.charAt(i);
                if (!Character.isWhitespace(character)) {
                    filtered.append(character);
                }
            }
            return filtered.length() != (end - start) ? filtered.toString() : null;
        });
        editText.setFilters(filters.toArray(new InputFilter[]{}));
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Share Wrappy", text);
        clipboard.setPrimaryClip(clip);
    }

    public static String getAppVersionName(Context context) {
        String version = "";
        try {
            version += context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            if(BuildConfig.SERVER_URL_CORE_SERVICE.equals("https://wrappy-test.newsupplytech.com/")) {
                version += "d";
            }
        } catch (Exception e) {
            version += "1.0.0";
        }
        return version;
    }

    public static void loadAvatarImage(Context context, ImageView imageView, String url) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.signatureOf(
                        GlideUtils.createObjectKey(url))
                        .circleCrop()
                        .placeholder(R.drawable.avatar))
                .into(imageView);
    }

    public static void loadBannerImage(Context context, ImageView imageView, String url) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.signatureOf(
                        GlideUtils.createObjectKey(url))
                        .centerCrop())
                .into(imageView);
    }

    public static Intent createAppSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Formats given seconds to formatted time string.
     * <p>
     * Rules:<br/>
     * < 10 mins = display in <b>X seconds</b><br/>
     * >= 10 mins = display in <b>X minutes</b><br/>
     * >= 10 hours = display in <b>X hours Y mins</b><br/>
     * </p>
     *
     * @param stringBuilder cached StringBuilder, can be null
     * @param lockLeftSeconds time in seconds
     * @return formatted string
     */
    public static String formatLockedTime(@Nullable StringBuilder stringBuilder, long lockLeftSeconds) {
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        StringBuilder sb = stringBuilder;
        if (sb == null) {
            sb = new StringBuilder();
        } else {
            sb.setLength(0);
        }

        if (lockLeftSeconds > 3600) {
            hours = lockLeftSeconds / 3600;
            lockLeftSeconds -= hours * 3600;
            minutes = lockLeftSeconds / 60;
        } else if (lockLeftSeconds > 60) {
            minutes = lockLeftSeconds / 60;
        } else {
            seconds = lockLeftSeconds;
        }

        if (hours > 0) {
            sb.append(hours);

            if (hours == 1) {
                sb.append(" hour ");
            } else {
                sb.append(" hours ");
            }
        }

        if (minutes > 0 || (hours > 0 && minutes >= 0)) {
            sb.append(minutes);

            if (minutes == 1) {
                sb.append(" minute");
            } else {
                sb.append(" minutes");
            }
        }

        if (seconds > 0) {
            sb.append(seconds);

            if (seconds == 1) {
                sb.append(" second");
            } else {
                sb.append(" seconds");
            }
        }

        return sb.toString();
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[A-Z0-9a-z\\._+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}$");
        Matcher matcher = pattern.matcher(email);
        return matcher.find();
    }

    public static void setDarkStatusBarColor(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(uiOptions);
        activity.getWindow().setStatusBarColor(Color.BLACK);
    }

    public static void setLightStatusBarColor(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(uiOptions);
        activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));
    }

    public static Object genericInvokeMethod(Object obj, String methodName,
                                             Object... params) {
        int paramCount = params.length;
        Method method;
        Object requiredObj = null;
        Class<?>[] classArray = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            classArray[i] = params[i].getClass();
        }
        try {
            method = obj.getClass().getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
            method.setAccessible(false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return requiredObj;
    }
}
