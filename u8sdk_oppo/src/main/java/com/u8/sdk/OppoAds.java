package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class OppoAds extends U8AdAdapter {
    public OppoAds(Activity context) {

    }

    private String[] supportedMethods = {"rewardAd", "insertAd", "nativeAd"};

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

    @Override
    public void rewardAd(String posId) {
        OppoSDK.getInstance().showRewardAd(posId);
    }

    @Override
    public void insertAd(String posId) {
        OppoSDK.getInstance().showInsertAd(posId);
    }

    @Override
    public void nativeAd(String posId, U8AdParams adParams) {
        OppoSDK.getInstance().showNativeAd(posId, adParams);
    }

    @Override
    public void nativeClose() {
        OppoSDK.getInstance().nativeClose();
    }
}
