package com.wrappy.android.common;


import android.view.View;

public interface ToolbarScreen {

    void setToolbarCustomView(View toolbarCustomView);

    void showToolbarBackButton(boolean show);

    void showToolbar(boolean show);

    void setToolbarTitle(String title);

    void setToolbarLogo(int resId);
}
