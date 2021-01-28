package com.u8.sdk;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.heytap.msp.mobad.api.ad.NativeTempletAd;
import com.heytap.msp.mobad.api.listener.INativeTempletAdListener;
import com.heytap.msp.mobad.api.params.INativeTempletAdView;
import com.heytap.msp.mobad.api.params.NativeAdError;
import com.heytap.msp.mobad.api.params.NativeAdSize;

import java.util.List;

import static com.u8.sdk.OppoSDK.TAG;

public class OppoNativeAdPopupWindow extends PopupWindow {

    private NativeTempletAd mNativeTempletAd;
    private INativeTempletAdView mINativeTempletAdView;
    private ViewGroup mAdContainer;
    private View contentView;

    private Context mContext;
    private U8AdParams mAdParams;
    private boolean mIsDebug;

    public OppoNativeAdPopupWindow(final Activity context, String posId, boolean isDebug, U8AdParams adParams) {
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

        int width = adParams.getWidth();
        int height = adParams.getHeight();
        showLog("width:" + width);
        showLog("height:" + height);

        NativeAdSize nativeAdSize = new NativeAdSize.Builder()
                .setWidthInDp(px2dp(width))
                .setHeightInDp(px2dp(height))
                .build();
        mNativeTempletAd = new NativeTempletAd(context, posId, nativeAdSize, new INativeTempletAdListener() {
            @Override
            public void onAdSuccess(List<INativeTempletAdView> iNativeTempletAdViewList) {
                showLog("onAdSuccess size=" + (null != iNativeTempletAdViewList ? iNativeTempletAdViewList.size() : "null"));
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_LOADED, "广告加载完成，开始展示");

                if (null != iNativeTempletAdViewList && iNativeTempletAdViewList.size() > 0) {
                    /**
                     * 释放前一个INativeTempletAdView对对象资源
                     */
                    if (null != mINativeTempletAdView) {
                        mINativeTempletAdView.destroy();
                    }
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
                    /**
                     * 获取广告View
                     */
                    mINativeTempletAdView = iNativeTempletAdViewList.get(0);
                    View adView = mINativeTempletAdView.getAdView();
                    if (null != adView) {
                        /**
                         * 添加广告View到广告“容器”
                         */
                        mAdContainer.addView(adView);
                        /**
                         * 调用render方法渲染广告
                         */
                        mINativeTempletAdView.render();
                    }
                }
            }

            @Override
            public void onAdFailed(NativeAdError nativeAdError) {
                close();
                Log.e(TAG, "onAdFailed nativeAdError=" + (null != nativeAdError ? nativeAdError.toString() : "null"));
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_FAILED, "广告加载或展示失败");
            }

            @Override
            public void onAdClick(INativeTempletAdView iNativeTempletAdView) {
                showLog("onAdClick iNativeTempletAdView=" + (null != iNativeTempletAdView ? iNativeTempletAdView : "null"));
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLICKED, "广告被点击");
            }

            @Override
            public void onAdShow(INativeTempletAdView iNativeTempletAdView) {
                showLog("onAdShow iNativeTempletAdView=" + (null != iNativeTempletAdView ? iNativeTempletAdView : "null"));
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_SHOW, "开始展示广告");
            }

            @Override
            public void onAdClose(INativeTempletAdView iNativeTempletAdView) {
                showLog("onAdClose iNativeTempletAdView=" + (null != iNativeTempletAdView ? iNativeTempletAdView : "null"));
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_CLOSED, "广告被关闭");
                U8SDK.getInstance().onResult(U8Code.CODE_ADS_COMPLETE, "广告展示完成");

                /**
                 *当广告模板中的关闭按钮被点击时，广告将不再展示。INativeTempletAdView也会被Destroy，不再可用
                 */
                if (null != mAdContainer && mAdContainer.getChildCount() > 0) {
                    mAdContainer.removeAllViews();
                    mAdContainer.setVisibility(View.GONE);
                }

                if (null != mNativeTempletAd) {
                    mNativeTempletAd.destroyAd();
                    mNativeTempletAd = null;
                }

                close();
            }

            @Override
            public void onRenderSuccess(INativeTempletAdView iNativeTempletAdView) {
                showLog("onRenderSuccess iNativeTempletAdView=" + (null != iNativeTempletAdView ? iNativeTempletAdView : "null"));
            }

            @Override
            public void onRenderFailed(NativeAdError nativeAdError, INativeTempletAdView iNativeTempletAdView) {
                Log.e(TAG, "onRenderFailed nativeAdError=" + (null != nativeAdError ? nativeAdError.toString() : "null") + ",iNativeTempletAdView=" + (null != iNativeTempletAdView ? iNativeTempletAdView : "null"));
            }
        });
        mNativeTempletAd.loadAd();
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

        destroyAd();
    }

    private void destroyAd() {
        if (null != mNativeTempletAd) {
            mNativeTempletAd.destroyAd();
            mNativeTempletAd = null;
        }
    }

    private int getId(String defType, String paramString) {
        return mContext.getResources().getIdentifier(paramString, defType, mContext.getPackageName());
    }

    private int px2dp(float pxValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    private void showLog(String msg) {
        if (mIsDebug) {
            Log.i(TAG, msg);
        }
    }
}
