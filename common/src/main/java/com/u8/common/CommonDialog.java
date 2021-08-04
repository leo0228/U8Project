package com.u8.common;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @Description u8
 * @Author Lu
 * @Date 2021/8/2 10:14
 * @Version: 1.0
 */
public class CommonDialog extends Dialog {

    private static boolean isPolicy = true;
    private WebView mWebView;
    private TextView mTitle;
    private ProgressBar mProgressBar;

    public CommonDialog(Context context) {
        super(context);

        setCanceledOnTouchOutside(false);
        setCancelable(false);

        Window window = getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setWindowAnimations(android.R.style.Animation_Toast);
            window.setBackgroundDrawable(getContext().getResources().getDrawable((android.R.color.transparent)));
        }
    }

    public static void initCommon(Context context, boolean policy) {
        isPolicy = policy;

        CommonDialog dialog = new CommonDialog(context);
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(Util.getLayoutId(getContext(), "prompt_common_dialog"));

        findViewById(Util.getId(getContext(), "common_back")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mTitle = findViewById(Util.getId(getContext(), "common_title"));
        mProgressBar = findViewById(Util.getId(getContext(), "common_progress"));
        mWebView = findViewById(Util.getId(getContext(), "common_webview"));
        //系统默认会通过手机浏览器打开网页，为了能够直接通过WebView显示网页，则必须设置
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //使用WebView加载显示url
                view.loadUrl(url);
                //返回true
                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    mProgressBar.setProgress(newProgress);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                mTitle.setText(title);
            }
        });

        //声明WebSettings子类
        WebSettings webSettings = mWebView.getSettings();
        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        //不使用缓存
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                if (isPolicy) {
                    mWebView.loadUrl("https://img.moyoi.cn/privacypolicy.html");
                } else {
                    mWebView.loadUrl("https://img.moyoi.cn/UserAgreement.html");
                }
            }
        });

        updateWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        //避免WebView内存泄露
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onStop();
    }

    private void updateWindow() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams wAttrs = window.getAttributes();
            int orientation = getContext().getResources().getConfiguration().orientation;
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                wAttrs.width = dm.widthPixels;
                wAttrs.height = dm.heightPixels;
            } else {
                wAttrs.width = dm.widthPixels;
                wAttrs.height = dm.heightPixels;
            }
            window.setAttributes(wAttrs);
        }
    }
}
