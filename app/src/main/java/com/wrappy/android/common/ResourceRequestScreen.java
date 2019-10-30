package com.wrappy.android.common;


import android.content.DialogInterface;
import android.support.annotation.Nullable;

public interface ResourceRequestScreen {

    void showAlertDialog(String message, @Nullable DialogInterface.OnClickListener clickListener);

    void showLoadingDialog(Resource resource);
}
