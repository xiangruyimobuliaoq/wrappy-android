package com.wrappy.android.common;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.wrappy.android.MainActivity;
import com.wrappy.android.di.FragmentInjector;
import com.wrappy.android.di.FragmentProviderModule;

public class BaseFragment extends WrappyFragment implements ToolbarScreen, Backable, ResourceRequestScreen {

    private FragmentInjector mFragmentInjector;

    public FragmentInjector getInjector() {
        if (mFragmentInjector == null) {
            mFragmentInjector = ((MainActivity) getActivity()).getInjector()
                    .plusFragmentInjector(new FragmentProviderModule(this));
        }
        return mFragmentInjector;
    }

    public int getBackStackCount() {
        return getChildFragmentManager().getBackStackEntryCount();
    }

    protected void setToolbar(Toolbar toolbar) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    private ActionBar getToolbar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void setToolbarCustomView(View toolbarCustomView) {
        getToolbar().setCustomView(toolbarCustomView);
        getToolbar().setDisplayShowCustomEnabled(true);
        getToolbar().setDisplayShowTitleEnabled(false);
        getToolbar().setDisplayUseLogoEnabled(false);
    }

    @Override
    public void showToolbarBackButton(boolean show) {
        getToolbar().setDisplayShowHomeEnabled(show);
        getToolbar().setDisplayHomeAsUpEnabled(show);
    }

    @Override
    public void showToolbar(boolean show) {
        ActionBar toolbar = getToolbar();
        if (show) {
            toolbar.show();
        } else {
            toolbar.hide();
        }
    }

    @Override
    public void setToolbarTitle(String title) {
        getToolbar().setTitle(title);
        getToolbar().setDisplayShowCustomEnabled(false);
        getToolbar().setDisplayShowTitleEnabled(true);
        getToolbar().setDisplayUseLogoEnabled(false);
    }

    @Override
    public void setToolbarLogo(int resId) {
        getToolbar().setLogo(resId);
        getToolbar().setDisplayShowCustomEnabled(false);
        getToolbar().setDisplayShowTitleEnabled(false);
        getToolbar().setDisplayUseLogoEnabled(true);
    }

    @Override
    public void showAlertDialog(String message, DialogInterface.OnClickListener clickListener) {
        if (getActivity() instanceof ResourceRequestScreen) {
            ((ResourceRequestScreen) getActivity()).showAlertDialog(message, clickListener);
        }
    }

    @Override
    public void showLoadingDialog(Resource resource) {
        if (getActivity() instanceof ResourceRequestScreen) {
            ((ResourceRequestScreen) getActivity()).showLoadingDialog(resource);
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
