package com.zjtx.prompt;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class PromptDialog extends Dialog implements OnClickListener {
    private TextView dec1, dec2, dec3;

    private static OnPromptListener listener;

    public interface OnPromptListener {
        void onInitSDK();
    }

    private static SharedPreferences prefs;
    private static final String FIRST_APP = "fristapp";

    private static boolean isHulian2 = false;

    public static void init(Context context, OnPromptListener onPromptListener) {
        isHulian2 = context.getPackageName().equals("com.moyoi.zjtx.xb") ? true : false;
        Log.e("PromptDialog", "isHulian2==" + isHulian2);

        listener = onPromptListener;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int firstApp = prefs.getInt(FIRST_APP, 0);
        Log.e("PromptDialog", "firstApp==" + firstApp);
        if (firstApp == 0) {
            // 如果值为0，就是为第一次启动
            PromptDialog dialog = new PromptDialog(context);
            dialog.show();
        } else {
            listener.onInitSDK();
        }
    }

    private PromptDialog(Context context) {
        super(context);

        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setWindowAnimations(android.R.style.Animation_Dialog);
        window.setBackgroundDrawable(getContext().getResources().getDrawable((android.R.color.transparent)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isHulian2) {
            setContentView(getLayoutId(getContext(), "prompt_dialog_hulian2"));
        } else {
            setContentView(getLayoutId(getContext(), "prompt_dialog"));
        }

        findViewById(getId(getContext(), "prompt_agree")).setOnClickListener(this);
        findViewById(getId(getContext(), "prompt_disagree")).setOnClickListener(this);

        dec1 = (TextView) findViewById(getId(getContext(), "prompt_dec1"));
        dec2 = (TextView) findViewById(getId(getContext(), "prompt_dec2"));
        dec3 = (TextView) findViewById(getId(getContext(), "prompt_dec3"));
        dec3.setOnClickListener(this);

        initData();
        updateWindow();
    }

    @SuppressWarnings("deprecation")
    private void initData() {
        String app_name = getContext().getResources().getString(getStringId(getContext(), "app_name"));
        String paramString1 = "";
        String paramString2 = "";
        String paramString3 = "";

        if (app_name.equals("战箭天下") && isHulian2) {
            paramString1 = "prompt_dec1_hulian2";
            paramString2 = "prompt_dec2_hulian2";
            paramString3 = "prompt_dec3_hulian2";
        } else if (app_name.equals("战箭天下")) {
            paramString1 = "prompt_dec1_zjtx";
            paramString2 = "prompt_dec2_zjtx";
            paramString3 = "prompt_dec3_zjtx";
        } else if (app_name.equals("城堡英雄")) {
            paramString1 = "prompt_dec1_cbyx";
            paramString2 = "prompt_dec2_cbyx";
            paramString3 = "prompt_dec3_cbyx";
        }

        String dec1_text = getContext().getResources().getString(getStringId(getContext(), paramString1));
        String dec2_text = getContext().getResources().getString(getStringId(getContext(), paramString2));
        String dec3_text = getContext().getResources().getString(getStringId(getContext(), paramString3));
        dec1.setText(Html.fromHtml(String.format(dec1_text)));
        dec2.setText(Html.fromHtml(String.format(dec2_text)));
        dec3.setText(Html.fromHtml(String.format(dec3_text)));
    }

    private void updateWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams wAttrs = window.getAttributes();
        int orientation = getContext().getResources().getConfiguration().orientation;
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            wAttrs.width = (int) (dm.widthPixels * 0.7f);
            wAttrs.height = (int) (dm.heightPixels * 0.85f);
        } else {
            wAttrs.width = (int) (dm.widthPixels * 0.8f);
            wAttrs.height = (int) (dm.heightPixels * 0.6f);
        }
        window.setAttributes(wAttrs);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == getId(getContext(), "prompt_disagree")) {
            cancel();
            System.exit(0);
        } else if (v.getId() == getId(getContext(), "prompt_agree")) {
            prefs.edit().putInt(FIRST_APP, 1).commit();
            listener.onInitSDK();
            cancel();
        } else if (v.getId() == getId(getContext(), "prompt_dec3")) {
            intoWebView();
        }
    }

    private void intoWebView() {
        Uri uri = Uri.parse("https://img.moyoi.cn/privacypolicy.html");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        getContext().startActivity(intent);

        // WebView webView = new WebView(getContext());
        // webView.getSettings().setJavaScriptEnabled(true);
        // webView.setWebViewClient(new WebViewClient());
        // webView.loadUrl("https://img.moyoi.cn/privacypolicy.htm");
    }

    @Override
    public void onBackPressed() {

    }

    private static int getLayoutId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "layout", paramContext.getPackageName());
    }

    private static int getId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "id", paramContext.getPackageName());
    }

    private static int getStringId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "string", paramContext.getPackageName());
    }
}