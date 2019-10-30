package com.wrappy.android.about;

import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;
import com.wrappy.android.common.utils.InputUtils;


public class AboutPageFragment extends SubFragment implements OnClickListener {

    @Inject
    AboutNavigationManager mAboutNavigationManager;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    AboutViewModel mAboutViewModel;

    private Button mButtonAboutWrappy;
    private Button mButtonFaq;
    private Button mButtonContactSupport;

    private TextView mTextViewVersion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_misc_about_page, container, false);
        mButtonAboutWrappy = view.findViewById(R.id.about_button_share_wrappy);
        mButtonFaq = view.findViewById(R.id.about_button_faq);
        mButtonContactSupport = view.findViewById(R.id.about_button_contact_support);
        mTextViewVersion = view.findViewById(R.id.about_textview_version);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getInjector().inject(this);
        mAboutViewModel = ViewModelProviders.of(getParentFragment(), mViewModelFactory).get(AboutViewModel.class);

        setToolbarTitle(getString(R.string.about_toolbar_title));

        mButtonAboutWrappy.setOnClickListener(this);
        mButtonFaq.setOnClickListener(this);
        mButtonContactSupport.setOnClickListener(this);

        mTextViewVersion.setText(InputUtils.getAppVersionName(getContext()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_button_share_wrappy:
                mAboutViewModel.getShareWrappyContent().observe(this, result -> {
                    showLoadingDialog(result);
                    switch (result.status) {
                        case SUCCESS:
                            InputUtils.copyToClipboard(getContext(), result.data);
                            showAlertDialog(getString(R.string.about_dialog_copy_content_success), null);
                            break;
                        case CLIENT_ERROR:
                        case SERVER_ERROR:
                            showAlertDialog(result.message, null);
                            break;
                    }
                });
                break;
            case R.id.about_button_faq:
                mAboutNavigationManager.showWebPage(
                        getString(R.string.about_faq_url),
                        getString(R.string.about_faq_title));
                break;
            case R.id.about_button_contact_support:
                mAboutNavigationManager.showWebPage(
                        getString(R.string.about_support_url),
                        getString(R.string.about_support_title));
                break;
        }
    }
}
