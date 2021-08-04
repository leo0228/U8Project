package com.u8.common;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class PromptDialog extends Dialog implements OnClickListener {
    private TextView dec1, dec2, dec3;

    private static OnPromptListener listener;

    public interface OnPromptListener {
        void onInitSDK();
    }

    private static SharedPreferences prefs;
    private static final String FIRST_APP = "fristapp";

    private static boolean isHulian2 = false;

    private boolean isReadPolicy = false;
    private boolean isReadUser = false;

    public static void init(Context context, OnPromptListener onPromptListener) {
        isHulian2 = context.getPackageName().equals("com.moyoi.zjtx.xb");
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
        setCancelable(false);

        Window window = getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setWindowAnimations(android.R.style.Animation_Dialog);
            window.setBackgroundDrawable(getContext().getResources().getDrawable((android.R.color.transparent)));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isHulian2) {
            setContentView(Util.getLayoutId(getContext(), "prompt_dialog_hulian2"));
        } else {
            setContentView(Util.getLayoutId(getContext(), "prompt_dialog"));
        }

        findViewById(Util.getId(getContext(), "prompt_agree")).setOnClickListener(this);
        findViewById(Util.getId(getContext(), "prompt_disagree")).setOnClickListener(this);

        dec1 = (TextView) findViewById(Util.getId(getContext(), "prompt_dec1"));
        dec2 = (TextView) findViewById(Util.getId(getContext(), "prompt_dec2"));
        dec3 = (TextView) findViewById(Util.getId(getContext(), "prompt_dec3"));

        initData();
        updateWindow();
    }

    private void initData() {
        String pckName = getContext().getPackageName();
        boolean isCbyx = pckName.contains("goc");//城堡英雄
        Log.e("PromptDialog", "isCbyx==" + isCbyx);

        if (!isCbyx && isHulian2) {
            String dec1_text = getContext().getResources().getString(Util.getStringId(getContext(), "prompt_dec1_hulian2"));
            String dec2_text = getContext().getResources().getString(Util.getStringId(getContext(), "prompt_dec2_hulian2"));
            String dec3_text = getContext().getResources().getString(Util.getStringId(getContext(), "prompt_dec3_hulian2"));
            dec1.setText(Html.fromHtml(dec1_text));
            dec2.setText(Html.fromHtml(dec2_text));
            dec3.setText(Html.fromHtml(dec3_text));
            return;
        }

        String name = "战箭天下";
        if (isCbyx) {
            name = "城堡英雄";
        }

        String dec1_text = getContext().getResources().getString(Util.getStringId(getContext(), "prompt_dec1"), name);
        dec1.setText(dec1_text);
        String dec2_text = getContext().getResources().getString(Util.getStringId(getContext(), "prompt_dec2"), name, name, name);
        dec2.setText(Html.fromHtml(dec2_text));

        String policy = "隐私政策";
        String user = "用户协议";
//        String policy = "<a href=\"https://img.moyoi.cn/privacypolicy.html\">《隐私政策》</a>";
//        String user = "<a href=\"https://img.moyoi.cn/UserAgreement.html\">《用户协议》</a>";
        String dec3_text = getContext().getResources().getString(Util.getStringId(getContext(), "prompt_dec3"), name, policy, user);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(dec3_text);

        //前0-28位字体加粗，开始包括，结束不包括
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        //隐私政策,用户协议 文字点击事件(默认加了下划线和颜色)
        ClickableSpan policySpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                isReadPolicy = true;
                CommonDialog.initCommon(getContext(), true);
            }
        };
        ClickableSpan userSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                isReadUser = true;
                CommonDialog.initCommon(getContext(), false);
            }
        };
        spannableStringBuilder.setSpan(policySpan, 28, 34, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(userSpan, 35, 41, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        //隐私政策,用户协议 字体蓝色
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLUE), 28, 34, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLUE), 35, 41, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        dec3.setText(spannableStringBuilder);
        dec3.setMovementMethod(LinkMovementMethod.getInstance());//使超链接可以跳转
    }

    private void updateWindow() {
        Window window = getWindow();
        if (window != null) {
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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == Util.getId(getContext(), "prompt_disagree")) {
            cancel();
            System.exit(0);
        } else if (v.getId() == Util.getId(getContext(), "prompt_agree")) {
            if (!isReadPolicy) {
                Toast.makeText(getContext(), "未阅读隐私政策", Toast.LENGTH_LONG).show();
            }
            if (!isReadUser) {
                Toast.makeText(getContext(), "未阅读用户协议", Toast.LENGTH_LONG).show();
            }

            if (isReadPolicy && isReadUser) {
                cancel();
                prefs.edit().putInt(FIRST_APP, 1).apply();
                listener.onInitSDK();
            }
        }
    }
}