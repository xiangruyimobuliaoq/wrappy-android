package com.wrappy.android.common;

import android.content.DialogInterface;
import android.view.View;

import com.wrappy.android.di.FragmentInjector;

public class SubFragment extends WrappyFragment implements ToolbarScreen, Backable, ResourceRequestScreen {

    public FragmentInjector getInjector() {
        return getBaseFragment().getInjector();
    }

    private BaseFragment getBaseFragment() {
        return (BaseFragment) getParentFragment();
    }

    @Override
    public void showToolbarBackButton(boolean show) {
        getBaseFragment().showToolbarBackButton(show);
    }

    @Override
    public void setToolbarCustomView(View toolbarCustomView) {
        getBaseFragment().setToolbarCustomView(toolbarCustomView);
    }

    @Override
    public void showToolbar(boolean show) {
        getBaseFragment().showToolbar(show);
    }

    @Override
    public void setToolbarTitle(String title) {
        getBaseFragment().setToolbarTitle(title);
    }

    @Override
    public void setToolbarLogo(int resId) {
        getBaseFragment().setToolbarLogo(resId);
    }

    @Override
    public void showAlertDialog(String message, DialogInterface.OnClickListener clickListener) {
        getBaseFragment().showAlertDialog(message, clickListener);
    }

    @Override
    public void showLoadingDialog(Resource resource) {
        getBaseFragment().showLoadingDialog(resource);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
