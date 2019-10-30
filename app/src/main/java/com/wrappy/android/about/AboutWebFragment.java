package com.wrappy.android.about;

import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wrappy.android.R;
import com.wrappy.android.common.SubFragment;


public class AboutWebFragment extends SubFragment {
    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_TITLE = "extra_title";

    private WebView mWebView;
    private boolean mWebWindowClosed;

    public static AboutWebFragment create(String url, String title) {
        AboutWebFragment f = new AboutWebFragment();
        Bundle b = new Bundle();
        b.putString(EXTRA_URL, url);
        b.putString(EXTRA_TITLE, title);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_misc_web, container, false);
        mWebView = view.findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        // TODO: Remove once SSL is properly implemented
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (error.getPrimaryError() != SslError.SSL_UNTRUSTED) {
                    handler.cancel();
                } else {
                    handler.proceed();
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onCloseWindow(WebView window) {
                mWebWindowClosed = true;
                getActivity().onBackPressed();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showToolbarBackButton(true);
        setToolbarTitle(getArguments().getString(EXTRA_TITLE));
        mWebView.loadUrl(getArguments().getString(EXTRA_URL));
    }

    @Override
    public boolean onBackPressed() {
        if (!mWebWindowClosed && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }
}
