package com.u8.sdk;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vivo.ad.model.AdError;
import com.vivo.ad.nativead.NativeAdListener;
import com.vivo.ad.nativead.NativeResponse;
import com.vivo.mobilead.nativead.NativeAdParams;
import com.vivo.mobilead.nativead.VivoNativeAd;
import com.vivo.mobilead.unified.base.view.VivoNativeAdContainer;

import java.util.List;

import static com.u8.sdk.VivoSDK.TAG;

public class VivoNativeAdPopupWindow extends PopupWindow {

    private VivoNativeAd mVivoNativeAd;
    private NativeResponse mNativeResponse;
    private ViewGroup mAdContainer;

    private View contentView;

    private Context mContext;
    private U8AdParams mAdParams;
    private boolean mIsDebug;

    public VivoNativeAdPopupWindow(final Activity context, String posId, boolean isDebug, U8AdParams adParams) {
        super(context);

        mContext = context;
        mAdParams = adParams;
        mIsDebug = isDebug;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(getId("layout", "u8_native_dialog"), null);
        mAdContainer = (ViewGroup) contentView.findViewById(getId("id", "ll_content"));
        // 设置PopupWindow的View
        this.setContentView(contentView);
        // 设置PopupWindow弹出窗体是否能点击
        this.setFocusable(adParams.isClose());
        // 设置PopupWindow弹出窗体以外是否能点击
        this.setOutsideTouchable(adParams.isClose());
        // 刷新状态
        this.update();
        // 设置背景，实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        this.setBackgroundDrawable(dw);

        NativeAdParams.Builder builder = new NativeAdParams.Builder(posId);
        mVivoNativeAd = new VivoNativeAd(context, builder.build(), new NativeAdListener() {
            @Override
            public void onADLoaded(List<NativeResponse> nativeResponses) {
                showLog("onADLoaded");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始展示");

                if (nativeResponses != null && nativeResponses.size() > 0 && nativeResponses.get(0) != null) {

                    /**
                     * 释放前一个mNativeResponse对象资源
                     */
                    mNativeResponse = null;

                    /**
                     * 将广告“容器”置于可见状态，否则将无法产生有效曝光
                     */
                    if (View.VISIBLE != mAdContainer.getVisibility()) {
                        mAdContainer.setVisibility(View.VISIBLE);
                    }
                    /**
                     * 如果容器已经有广告、则移除
                     */
                    if (mAdContainer.getChildCount() > 0) {
                        mAdContainer.removeAllViews();
                    }

                    mNativeResponse = nativeResponses.get(0);
                    if (mNativeResponse.getMaterialMode() == NativeResponse.MODE_VIDEO) { //展示视频

                    } else if (mNativeResponse.getMaterialMode() == NativeResponse.MODE_UNKNOW) {   //展示无图片样式信息流广告

                    } else if (mNativeResponse.getMaterialMode() == NativeResponse.MODE_GROUP) {    //展示组图样式信息流广告

                    } else if (mNativeResponse.getMaterialMode() == NativeResponse.MODE_LARGE) {     //展示大图样式信息流广告

                    } else {
                        showTinyImageAd();  //展示小图样式信息流广告
                    }
                } else {
                    close();
                    Log.e(TAG, "返回广告列表为空");
                    U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "返回广告列表为空");
                }
            }

            @Override
            public void onNoAD(AdError adError) {
                close();
                Log.e(TAG, "onNoAD:" + adError);
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载失败");
            }

            @Override
            public void onClick(NativeResponse nativeResponse) {
                showLog("onClick");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击了");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "图片广告展示成功, 开始发放奖励!");
            }

        });
        mVivoNativeAd.loadAd();
    }

    public void show() {
        if (!this.isShowing()) {
            if (mAdParams != null) {
                int x = mAdParams.getX();
                int y = mAdParams.getY();
                showLog("x:" + x);
                showLog("y:" + y);

                if (Build.VERSION.SDK_INT < 24) {
                    this.showAsDropDown(contentView, x, y);
                } else {
                    this.showAtLocation(contentView, Gravity.NO_GRAVITY, x, y);
                }
            }
        }
    }

    public void close() {
        if (this.isShowing()) {
            this.dismiss();
        }

        mNativeResponse = null;

        if (null != mAdContainer && mAdContainer.getChildCount() > 0) {
            mAdContainer.removeAllViews();
            mAdContainer.setVisibility(View.GONE);
        }

        destroyAd();
    }

    private void destroyAd() {
        if (null != mVivoNativeAd) {
            mVivoNativeAd = null;
        }
    }

    private int getId(String defType, String paramString) {
        return mContext.getResources().getIdentifier(paramString, defType, mContext.getPackageName());
    }

    private void showTinyImageAd() {
        VivoNativeAdContainer vivoNativeAdContainer =
                (VivoNativeAdContainer) LayoutInflater.from(mContext).inflate(getId("layout", "layout_stream_tiny_image"), null);

        int width = mAdParams.getWidth();
        int height = mAdParams.getHeight();
        showLog("width:" + width);
        showLog("height:" + height);

        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(width, height);
        vivoNativeAdContainer.setLayoutParams(params);

        ImageView ivImage = vivoNativeAdContainer.findViewById(getId("id", "iv_image"));
        TextView tvTitle = vivoNativeAdContainer.findViewById(getId("id", "tv_title"));
        //设置广告图片
        Picasso.with(mContext).load(mNativeResponse.getImgUrl().get(0)).into(ivImage);
        //设置标题
        tvTitle.setText(mNativeResponse.getTitle());
        //必须添加广告logo 否者审核不通过
        renderAdLogoAndTag(vivoNativeAdContainer);
        //添加广告到视图树中
        mAdContainer.addView(vivoNativeAdContainer);
        mNativeResponse.registerView(vivoNativeAdContainer, null, null);
    }

    /**
     * 广告标记
     *
     * @param adView
     */
    private void renderAdLogoAndTag(View adView) {
        ImageView ivAdMarkLogo = adView.findViewById(getId("id", "iv_ad_mark_logo"));
        TextView tvAdMarkText = adView.findViewById(getId("id", "tv_ad_mark_text"));

        if (mNativeResponse.getAdLogo() != null) {
            ivAdMarkLogo.setVisibility(View.VISIBLE);
            tvAdMarkText.setVisibility(View.GONE);
            ivAdMarkLogo.setImageBitmap(mNativeResponse.getAdLogo());
        } else if (!TextUtils.isEmpty(mNativeResponse.getAdMarkUrl())) {
            ivAdMarkLogo.setVisibility(View.VISIBLE);
            tvAdMarkText.setVisibility(View.GONE);
            Picasso.with(mContext)
                    .load(mNativeResponse.getAdMarkUrl())
                    .into(ivAdMarkLogo);
        } else {
            String adMark;
            if (!TextUtils.isEmpty(mNativeResponse.getAdMarkText())) {
                adMark = mNativeResponse.getAdMarkText();
            } else if (!TextUtils.isEmpty(mNativeResponse.getAdTag())) {
                adMark = mNativeResponse.getAdTag();
            } else {
                adMark = "广告";
            }

            tvAdMarkText.setVisibility(View.VISIBLE);
            ivAdMarkLogo.setVisibility(View.GONE);
            tvAdMarkText.setText(adMark);
        }
    }

    private void showLog(String msg) {
        if (mIsDebug) {
            Log.i(TAG, msg);
        }
    }
}
