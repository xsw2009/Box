package com.github.tvbox.osc.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.tvbox.osc.server.ControlManager;

public class WebUiActivity extends Activity {

    private WebView mWebView;
    private FrameLayout mContainer;
    private ProgressBar mProgress;
    private TextView mError;
    private LinearLayout mErrorBox;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        mContainer = new FrameLayout(this);
        mContainer.setBackgroundColor(Color.BLACK);

        mProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                (int) (getResources().getDisplayMetrics().density * 3));
        mProgress.setVisibility(View.GONE);
        mContainer.addView(mProgress, pp);

        mWebView = new WebView(this);
        WebSettings s = mWebView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setBackgroundColor(Color.BLACK);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mProgress.setVisibility(View.GONE);
                if (failingUrl != null && failingUrl.startsWith("http")) {
                    mErrorBox.setVisibility(View.VISIBLE);
                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    mProgress.setVisibility(View.VISIBLE);
                    mProgress.setProgress(newProgress);
                } else {
                    mProgress.setVisibility(View.GONE);
                }
            }
        });
        mContainer.addView(mWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        mErrorBox = new LinearLayout(this);
        mErrorBox.setOrientation(LinearLayout.VERTICAL);
        mErrorBox.setGravity(17);
        final float density = getResources().getDisplayMetrics().density;
        mError = new TextView(this);
        mError.setTextColor(Color.WHITE);
        mError.setTextSize(18);
        mError.setText("页面加载失败\n请按遥控器「菜单」键返回原生界面");
        mErrorBox.addView(mError, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mErrorBox.setVisibility(View.GONE);
        mErrorBox.setPadding((int) (density * 40), (int) (density * 40), (int) (density * 40), (int) (density * 40));
        mContainer.addView(mErrorBox, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(mContainer);

        try {
            ControlManager.init(getApplicationContext());
            ControlManager.get().startServer();

            mWebView.requestFocus();
            mWebView.setFocusable(true);
            mWebView.setFocusableInTouchMode(true);
            mWebView.loadUrl("http://127.0.0.1:" + com.github.tvbox.osc.server.RemoteServer.serverPort + "/");
        } catch (Throwable t) {
            t.printStackTrace();
            goNative();
        }
    }

    private void goNative() {
        Intent it = new Intent(this, HomeActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(it);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            goNative();
            return true;
        }
        if (mWebView != null && !mWebView.isFocused()) {
            mWebView.requestFocus();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mWebView != null) {
            mWebView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroy();
    }
}