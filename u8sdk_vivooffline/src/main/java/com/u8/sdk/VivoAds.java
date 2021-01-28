package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class VivoAds extends U8AdAdapter {

    public VivoAds(Activity context) {

    }

    private String[] supportedMethods = {"rewardAd", "insertAd", "nativeAd"};

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

    @Override
    public void rewardAd(String posId) {
        VivoSDK.getInstance().showRewardAd(posId);
    }

    @Override
    public void insertAd(String posId) {
        VivoSDK.getInstance().showInsertAd(posId);
    }

    @Override
    public void nativeAd(String posId, U8AdParams adParams) {
        VivoSDK.getInstance().showNativeAd(posId,adParams);
    }

    @Override
    public void nativeClose() {
        VivoSDK.getInstance().nativeClose();
    }
}
